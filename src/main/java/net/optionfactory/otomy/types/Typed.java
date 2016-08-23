/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.optionfactory.otomy.types;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Encapsulates a Java {@link java.lang.reflect.Type}, providing access to
 * {@link #getSuperType() supertypes}, {@link #getInterfaces() interfaces}, and
 * {@link #getGeneric(int...) generic parameters} along with the ability to
 * ultimately {@link #resolve() resolve} to a {@link java.lang.Class}.
 *
 * <p>
 * {@code ResolvableTypes} may be obtained from {@link #forField(Field) fields},
 * {@link #forMethodParameter(Method, int) method parameters},
 * {@link #forMethodReturnType(Method) method returns} or
 * {@link #class_(Class) classes}. Most methods on this class will themselves
 * return {@link Typed}s, allowing easy navigation. For example:
 * <pre class="code">
 * private HashMap&lt;Integer, List&lt;String&gt;&gt; myMap;
 *
 * public void example() { ResolvableType t =
 * ResolvableType.forField(getClass().getDeclaredField("myMap"));
 * t.getSuperType(); // AbstractMap&lt;Integer, List&lt;String&gt;&gt;
 * t.asMap(); // Map&lt;Integer, List&lt;String&gt;&gt;
 * t.getGeneric(0).resolve(); // Integer t.getGeneric(1).resolve(); // List
 * t.getGeneric(1); // List&lt;String&gt; t.resolveGeneric(1, 0); // String }
 * </pre>
 *
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 4.0
 * @see #forField(Field)
 * @see #forMethodParameter(Method, int)
 * @see #forMethodReturnType(Method)
 * @see #forConstructorParameter(Constructor, int)
 * @see #class_(Class)
 * @see #type(Type)
 * @see #forInstance(Object)
 * @see ResolvableTypeProvider
 */
public class Typed {

    /**
     * {@code ResolvableType} returned when no value is available. {@code NONE}
     * is used in preference to {@code null} so that multiple method calls can
     * be safely chained.
     */
    public static final Typed NONE = new Typed(null, null, null, 0);

    private static final Typed[] EMPTY_TYPES_ARRAY = new Typed[0];

    private static final Map<Typed, Typed> CACHE = new ConcurrentHashMap<>(256);

    /**
     * The underlying Java type being managed (only ever {@code null} for
     * {@link #NONE}).
     */
    final Type type;

    /**
     * Optional provider for the type.
     */
    private final TypeProvider typeProvider;

    /**
     * The {@code VariableResolver} to use or {@code null} if no resolver is
     * available.
     */
    final Typed resolutionContext;

    /**
     * The component type for an array or {@code null} if the type should be
     * deduced.
     */
    private final Typed componentType;

    /**
     * Copy of the resolved value.
     */
    private final Class<?> resolved;

    private final Integer hash;

    private Typed superType;

    private Typed[] interfaces;

    private Typed[] generics;

    /**
     * Private constructor used to create a new {@link ResolvableType} for cache
     * key purposes, with no upfront resolution.
     */
    private Typed(Type type, TypeProvider typeProvider, Typed resolutionContext) {
        this.type = type;
        this.typeProvider = typeProvider;
        this.resolutionContext = resolutionContext;
        this.componentType = null;
        this.resolved = null;
        this.hash = calculateHashCode();
    }

    /**
     * Private constructor used to create a new {@link ResolvableType} for cache
     * value purposes, with upfront resolution and a pre-calculated hash.
     *
     * @since 4.2
     */
    private Typed(Type type, TypeProvider typeProvider, Typed resolutionContext, Integer hash) {
        this.type = type;
        this.typeProvider = typeProvider;
        this.resolutionContext = resolutionContext;
        this.componentType = null;
        this.resolved = resolveClass();
        this.hash = hash;
    }

    /**
     * Private constructor used to create a new {@link ResolvableType} for
     * uncached purposes, with upfront resolution but lazily calculated hash.
     */
    private Typed(
            Type type, TypeProvider typeProvider, Typed resolutionContext, Typed componentType) {

        this.type = type;
        this.typeProvider = typeProvider;
        this.resolutionContext = resolutionContext;
        this.componentType = componentType;
        this.resolved = resolveClass();
        this.hash = null;
    }

    /**
     * Private constructor used to create a new {@link ResolvableType} on a
     * {@link Class} basis. Avoids all {@code instanceof} checks in order to
     * create a straight {@link Class} wrapper.
     *
     * @since 4.2
     */
    private Typed(Class<?> sourceClass) {
        this.resolved = (sourceClass != null ? sourceClass : Object.class);
        this.type = this.resolved;
        this.typeProvider = null;
        this.resolutionContext = null;
        this.componentType = null;
        this.hash = null;
    }

    /**
     * Return the underling Java {@link Type} being managed. With the exception
     * of the {@link #NONE} constant, this method will never return
     * {@code null}.
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Determine whether this {@code ResolvableType} is assignable from the
     * specified other type.
     * <p>
     * Attempts to follow the same rules as the Java compiler, considering
     * whether both the {@link #resolve() resolved} {@code Class} is
     * {@link Class#isAssignableFrom(Class) assignable from} the given type as
     * well as whether all {@link #getGenerics() generics} are assignable.
     *
     * @param other the type to be checked against (as a {@code ResolvableType})
     * @return {@code true} if the specified other type can be assigned to this
     * {@code ResolvableType}; {@code false} otherwise
     */
    public boolean isAssignableFrom(Typed other) {
        return isAssignableFrom(other, null);
    }

    private boolean isAssignableFrom(Typed other, Map<Type, Type> matchedBefore) {
        if (other == null) {
            throw new IllegalArgumentException("ResolvableType must not be null");
        }

        // If we cannot resolve types, we are not assignable
        if (this == NONE || other == NONE) {
            return false;
        }

        //sbi: uhm?
        if (type instanceof Class && other.type instanceof Class) {
            return ((Class) this.type).isAssignableFrom((Class) other.type);
        }

        // Deal with array by delegating to the component type
        if (isArray()) {
            return (other.isArray() && getComponentType().isAssignableFrom(other.getComponentType()));
        }

        if (matchedBefore != null && matchedBefore.get(this.type) == other.type) {
            return true;
        }

        // Deal with wildcard bounds
        WildcardBounds ourBounds = WildcardBounds.get(this);
        WildcardBounds typeBounds = WildcardBounds.get(other);

        // In the from X is assignable to <? extends Number>
        if (typeBounds != null) {
            return (ourBounds != null && ourBounds.isSameKind(typeBounds)
                    && ourBounds.isAssignableFrom(typeBounds.getBounds()));
        }

        // In the form <? extends Number> is assignable to X...
        if (ourBounds != null) {
            return ourBounds.isAssignableFrom(other);
        }

        // Main assignability check about to follow
        boolean exactMatch = (matchedBefore != null);  // We're checking nested generic variables now...
        boolean checkGenerics = true;
        Class<?> ourResolved = null;
        if (this.type instanceof TypeVariable) {
            TypeVariable<?> variable = (TypeVariable<?>) this.type;
            // Try default variable resolution
            if (this.resolutionContext != null) {
                Typed resolved = this.resolutionContext.resolveVariable(variable);
                if (resolved != null) {
                    ourResolved = resolved.resolve();
                }
            }
            if (ourResolved == null) {
                // Try variable resolution against target type
                if (other.resolutionContext != null) {
                    Typed resolved = other.resolutionContext.resolveVariable(variable);
                    if (resolved != null) {
                        ourResolved = resolved.resolve();
                        checkGenerics = false;
                    }
                }
            }
            if (ourResolved == null) {
                // Unresolved type variable, potentially nested -> never insist on exact match
                exactMatch = false;
            }
        }
        if (ourResolved == null) {
            ourResolved = resolve(Object.class);
        }
        Class<?> otherResolved = other.resolve(Object.class);

        // We need an exact type match for generics
        // List<CharSequence> is not assignable from List<String>
        if (exactMatch ? !ourResolved.equals(otherResolved) : !Types.isAssignable(ourResolved, otherResolved)) {
            return false;
        }

        if (checkGenerics) {
            // Recursively check each generic
            Typed[] ourGenerics = getGenerics();
            Typed[] typeGenerics = other.as(ourResolved).getGenerics();
            if (ourGenerics.length != typeGenerics.length) {
                return false;
            }
            if (matchedBefore == null) {
                matchedBefore = new IdentityHashMap<>(1);
            }
            matchedBefore.put(this.type, other.type);
            for (int i = 0; i < ourGenerics.length; i++) {
                if (!ourGenerics[i].isAssignableFrom(typeGenerics[i], matchedBefore)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Return {@code true} if this type resolves to a Class that represents an
     * array.
     *
     * @see #getComponentType()
     */
    public boolean isArray() {
        if (this == NONE) {
            return false;
        }
        return (((this.type instanceof Class && ((Class<?>) this.type).isArray()))
                || this.type instanceof GenericArrayType || resolveType().isArray());
    }

    /**
     * Return the ResolvableType representing the component type of the array or
     * {@link #NONE} if this type does not represent an array.
     *
     * @see #isArray()
     */
    public Typed getComponentType() {
        if (this == NONE) {
            return NONE;
        }
        if (this.componentType != null) {
            return this.componentType;
        }
        if (this.type instanceof Class) {
            Class<?> componentType = ((Class<?>) this.type).getComponentType();
            return Typed.type(componentType, this.resolutionContext);
        }
        if (this.type instanceof GenericArrayType) {
            return Typed.type(((GenericArrayType) this.type).getGenericComponentType(), this.resolutionContext);
        }
        return resolveType().getComponentType();
    }

    /**
     * Return this type as a {@link Typed} of the specified class.
     * Searches {@link #getSuperType() supertype} and
     * {@link #getInterfaces() interface} hierarchies to find a match, returning
     * {@link #NONE} if this type does not implement or extend the specified
     * class.
     *
     * @param type the required class type
     * @return a {@link Typed} representing this object as the
     * specified type, or {@link #NONE} if not resolvable as that type
     * @see #asCollection()
     * @see #asMap()
     * @see #getSuperType()
     * @see #getInterfaces()
     */
    public Typed as(Class<?> type) {
        if (this == NONE) {
            return NONE;
        }
        if (Objects.equals(resolve(), type)) {
            return this;
        }
        for (Typed interfaceType : getInterfaces()) {
            Typed interfaceAsType = interfaceType.as(type);
            if (interfaceAsType != NONE) {
                return interfaceAsType;
            }
        }
        return getSuperType().as(type);
    }

    /**
     * Return a {@link Typed} representing the direct supertype of this
     * type. If no supertype is available this method returns {@link #NONE}.
     *
     * @see #getInterfaces()
     */
    public Typed getSuperType() {
        Class<?> resolved = resolve();
        if (resolved == null || resolved.getGenericSuperclass() == null) {
            return NONE;
        }
        if (this.superType == null) {
            this.superType = Typed.type(resolved.getGenericSuperclass(), this);
        }
        return this.superType;
    }

    /**
     * Return a {@link Typed} array representing the direct interfaces
     * implemented by this type. If this type does not implement any interfaces
     * an empty array is returned.
     *
     * @see #getSuperType()
     */
    public Typed[] getInterfaces() {
        Class<?> resolved = resolve();
        final Type[] genericInterfaces = resolved.getGenericInterfaces();
        if (resolved == null || genericInterfaces == null || genericInterfaces.length == 0) {
            return EMPTY_TYPES_ARRAY;
        }
        if (this.interfaces == null) {
            this.interfaces = types(resolved.getGenericInterfaces(), this);
        }
        return this.interfaces;
    }

    /**
     * Return {@code true} if this type contains generic parameters.
     *
     * @see #getGeneric(int...)
     * @see #getGenerics()
     */
    public boolean hasGenerics() {
        return (getGenerics().length > 0);
    }

    /**
     * Determine whether the underlying type has any unresolvable generics:
     * either through an unresolvable type variable on the type itself or
     * through implementing a generic interface in a raw fashion, i.e. without
     * substituting that interface's type variables. The result will be
     * {@code true} only in those two scenarios.
     */
    public boolean hasUnresolvableGenerics() {
        if (this == NONE) {
            return false;
        }
        Typed[] generics = getGenerics();
        for (Typed generic : generics) {
            if (generic.isUnresolvableTypeVariable() || generic.isWildcardWithoutBounds()) {
                return true;
            }
        }
        Class<?> resolved = resolve();
        if (resolved != null) {
            for (Type genericInterface : resolved.getGenericInterfaces()) {
                if (genericInterface instanceof Class) {
                    if (class_((Class<?>) genericInterface).hasGenerics()) {
                        return true;
                    }
                }
            }
            return getSuperType().hasUnresolvableGenerics();
        }
        return false;
    }

    /**
     * Determine whether the underlying type is a type variable that cannot be
     * resolved through the associated variable resolver.
     */
    private boolean isUnresolvableTypeVariable() {
        if (this.type instanceof TypeVariable) {
            if (this.resolutionContext == null) {
                return true;
            }
            TypeVariable<?> variable = (TypeVariable<?>) this.type;
            Typed resolved = this.resolutionContext.resolveVariable(variable);
            if (resolved == null || resolved.isUnresolvableTypeVariable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether the underlying type represents a wildcard without
     * specific bounds (i.e., equal to {@code ? extends Object}).
     */
    private boolean isWildcardWithoutBounds() {
        if (this.type instanceof WildcardType) {
            WildcardType wt = (WildcardType) this.type;
            if (wt.getLowerBounds().length == 0) {
                Type[] upperBounds = wt.getUpperBounds();
                if (upperBounds.length == 0 || (upperBounds.length == 1 && Object.class == upperBounds[0])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return a {@link Typed} representing the generic parameter for
     * the given indexes. Indexes are zero based; for example given the type
     * {@code Map<Integer, List<String>>}, {@code getGeneric(0)} will access the
     * {@code Integer}. Nested generics can be accessed by specifying multiple
     * indexes; for example {@code getGeneric(1, 0)} will access the
     * {@code String} from the nested {@code List}. For convenience, if no
     * indexes are specified the first generic is returned.
     * <p>
     * If no generic is available at the specified indexes {@link #NONE} is
     * returned.
     *
     * @param indexes the indexes that refer to the generic parameter (may be
     * omitted to return the first generic)
     * @return a {@link Typed} for the specified generic or
     * {@link #NONE}
     * @see #hasGenerics()
     * @see #getGenerics()
     * @see #resolveGeneric(int...)
     * @see #resolveGenerics()
     */
    public Typed getGeneric(int... indexes) {
        try {
            if (indexes == null || indexes.length == 0) {
                return getGenerics()[0];
            }
            Typed generic = this;
            for (int index : indexes) {
                generic = generic.getGenerics()[index];
            }
            return generic;
        } catch (IndexOutOfBoundsException ex) {
            return NONE;
        }
    }

    /**
     * Return an array of {@link Typed}s representing the generic
     * parameters of this type. If no generics are available an empty array is
     * returned. If you need to access a specific generic consider using the
     * {@link #getGeneric(int...)} method as it allows access to nested generics
     * and protects against {@code IndexOutOfBoundsExceptions}.
     *
     * @return an array of {@link Typed}s representing the generic
     * parameters (never {@code null})
     * @see #hasGenerics()
     * @see #getGeneric(int...)
     * @see #resolveGeneric(int...)
     * @see #resolveGenerics()
     */
    public Typed[] getGenerics() {
        if (this == NONE) {
            return EMPTY_TYPES_ARRAY;
        }
        if (this.generics == null) {
            if (this.type instanceof Class) {
                Class<?> typeClass = (Class<?>) this.type;
                Type[] typeParameters = typeClass.getTypeParameters();
                this.generics = types(typeParameters, this.resolutionContext);
            } else if (this.type instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) this.type).getActualTypeArguments();
                Typed[] generics = new Typed[actualTypeArguments.length];
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    generics[i] = Typed.type(actualTypeArguments[i], this.resolutionContext);
                }
                this.generics = generics;
            } else {
                this.generics = resolveType().getGenerics();
            }
        }
        return this.generics;
    }

    /**
     * Resolve this type to a {@link java.lang.Class}, returning {@code null} if
     * the type cannot be resolved. This method will consider bounds of
     * {@link TypeVariable}s and {@link WildcardType}s if direct resolution
     * fails; however, bounds of {@code Object.class} will be ignored.
     *
     * @return the resolved {@link Class}, or {@code null} if not resolvable
     * @see #resolve(Class)
     * @see #resolveGeneric(int...)
     * @see #resolveGenerics()
     */
    public Class<?> resolve() {
        return resolve(null);
    }

    /**
     * Resolve this type to a {@link java.lang.Class}, returning the specified
     * {@code fallback} if the type cannot be resolved. This method will
     * consider bounds of {@link TypeVariable}s and {@link WildcardType}s if
     * direct resolution fails; however, bounds of {@code Object.class} will be
     * ignored.
     *
     * @param fallback the fallback class to use if resolution fails (may be
     * {@code null})
     * @return the resolved {@link Class} or the {@code fallback}
     * @see #resolve()
     * @see #resolveGeneric(int...)
     * @see #resolveGenerics()
     */
    public Class<?> resolve(Class<?> fallback) {
        return (this.resolved != null ? this.resolved : fallback);
    }

    private Class<?> resolveClass() {
        if (this.type instanceof Class || this.type == null) {
            return (Class<?>) this.type;
        }
        if (this.type instanceof GenericArrayType) {
            Class<?> resolvedComponent = getComponentType().resolve();
            return (resolvedComponent != null ? Array.newInstance(resolvedComponent, 0).getClass() : null);
        }
        return resolveType().resolve();
    }

    /**
     * Resolve this type by a single level, returning the resolved value or
     * {@link #NONE}.
     * <p>
     * Note: The returned {@link Typed} should only be used as an
     * intermediary as it cannot be serialized.
     */
    Typed resolveType() {
        if (this.type instanceof ParameterizedType) {
            return Typed.type(((ParameterizedType) this.type).getRawType(), this.resolutionContext);
        }
        if (this.type instanceof WildcardType) {
            Type resolved = resolveBounds(((WildcardType) this.type).getUpperBounds());
            if (resolved == null) {
                resolved = resolveBounds(((WildcardType) this.type).getLowerBounds());
            }
            return Typed.type(resolved, this.resolutionContext);
        }
        if (this.type instanceof TypeVariable) {
            TypeVariable<?> variable = (TypeVariable<?>) this.type;
            // Try default variable resolution
            if (this.resolutionContext != null) {
                Typed resolved = this.resolutionContext.resolveVariable(variable);
                if (resolved != null) {
                    return resolved;
                }
            }
            // Fallback to bounds
            return Typed.type(resolveBounds(variable.getBounds()), this.resolutionContext);
        }
        return NONE;
    }

    private Type resolveBounds(Type[] bounds) {
        if (bounds == null || bounds.length == 0 || Object.class == bounds[0]) {
            return null;
        }
        return bounds[0];
    }

    private Typed resolveVariable(TypeVariable<?> variable) {
        if (this.type instanceof TypeVariable) {
            return resolveType().resolveVariable(variable);
        }
        if (this.type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) this.type;
            TypeVariable<?>[] variables = resolve().getTypeParameters();
            for (int i = 0; i < variables.length; i++) {
                if (Objects.equals(variables[i].getName(), variable.getName())) {
                    Type actualType = parameterizedType.getActualTypeArguments()[i];
                    return Typed.type(actualType, this.resolutionContext);
                }
            }
            if (parameterizedType.getOwnerType() != null) {
                return Typed.type(parameterizedType.getOwnerType(), this.resolutionContext).resolveVariable(variable);
            }
        }
        if (this.resolutionContext != null) {
            return this.resolutionContext.resolveVariable(variable);
        }
        return null;
    }

    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) {
            return true;
        }
        if (!(rhs instanceof Typed)) {
            return false;
        }

        final Typed other = (Typed) rhs;
        return Objects.equals(this.type, other.type)
                && Objects.equals(this.typeProvider, other.typeProvider)
                && Objects.equals(this.resolutionContext, other.resolutionContext)
                && Objects.equals(this.componentType, other.componentType);
    }

    @Override
    public int hashCode() {
        return (this.hash != null ? this.hash : calculateHashCode());
    }

    public int calculateHashCode() {
        int hash = 7;
        hash = 59 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 59 * hash + (this.typeProvider != null ? this.typeProvider.hashCode() : 0);
//        hash = 59 * hash + Objects.hashCode(this.resolutionContext);
//        hash = 59 * hash + Objects.hashCode(this.componentType);
        return hash;
    }

    /**
     * Return a String representation of this type in its fully resolved form
     * (including any generic parameters).
     */
    @Override
    public String toString() {
        if (isArray()) {
            return getComponentType() + "[]";
        }
        if (this.resolved == null) {
            return "?";
        }
        if (this.type instanceof TypeVariable) {
            TypeVariable<?> variable = (TypeVariable<?>) this.type;
            if (this.resolutionContext == null || this.resolutionContext.resolveVariable(variable) == null) {
                // Don't bother with variable boundaries for toString()...
                // Can cause infinite recursions in case of self-references
                return "?";
            }
        }
        StringBuilder result = new StringBuilder(this.resolved.getName());
        if (hasGenerics()) {
            result.append('<');
            result.append(Arrays.stream(getGenerics()).map(Typed::toString).collect(Collectors.joining(", ")));
            result.append('>');
        }
        return result.toString();
    }

    // Factory methods
    /**
     * Return a {@link Typed} for the specified {@link Class}, using
     * the full generic type information for assignability checks. For example:
     * {@code ResolvableType.forClass(MyArrayList.class)}.
     *
     * @param sourceClass the source class ({@code null} is semantically
     * equivalent to {@code Object.class} for typical use cases here}
     * @return a {@link Typed} for the specified class
     * @see #forClass(Class, Class)
     * @see #forClassWithGenerics(Class, Class...)
     */
    public static Typed class_(Class<?> sourceClass) {
        return new Typed(sourceClass);
    }

    /**
     * Return a {@link Typed} for the specified {@link Field} with a
     * given implementation.
     * <p>
     * Use this variant when the class that declares the field includes generic
     * parameter variables that are satisfied by the implementation type.
     *
     * @param field the source field
     * @param implementationType the implementation type
     * @return a {@link Typed} for the specified field
     * @see #forField(Field)
     */
    public static Typed field(Field field, Typed implementationType) {
        if (field == null) {
            throw new IllegalArgumentException("Field must not be null");
        }
        implementationType = (implementationType == null ? NONE : implementationType);
        Typed owner = implementationType.as(field.getDeclaringClass());
        return type(null, new FieldTypeProvider(field), owner);
    }

    /**
     * Return a {@link Typed} for the specified {@link Method} return
     * type. Use this variant when the class that declares the method includes
     * generic parameter variables that are satisfied by the implementation
     * class.
     *
     * @param method the source for the method return type
     * @param implementationClass the implementation class
     * @return a {@link Typed} for the specified method return
     * @see #forMethodReturnType(Method)
     */
    public static Typed returnType(Method method, Typed implementationClass) {
        if (method == null) {
            throw new IllegalArgumentException("Method must not be null");
        }
        Typed owner = implementationClass.as(method.getDeclaringClass());
        return type(null, new MethodReturnTypeProvider(method, implementationClass), owner);
    }

    /**
     * Return a {@link Typed} for the specified {@link Method}
     * parameter with a given implementation. Use this variant when the class
     * that declares the method includes generic parameter variables that are
     * satisfied by the implementation class.
     *
     * @param method the source method (must not be {@code null})
     * @param parameterIndex the parameter index
     * @param implementationClass the implementation class
     * @return a {@link Typed} for the specified method parameter
     * @see #forMethodParameter(Method, int, Class)
     * @see #forMethodParameter(MethodParameter)
     */
    public static Typed parameter(Method method, int parameterIndex, Typed implementationClass) {
        if (method == null) {
            throw new IllegalArgumentException("Method must not be null");
        }
        Typed owner = implementationClass.as(method.getDeclaringClass());
        return type(null, new MethodParameterTypeProvider(method, implementationClass, parameterIndex), owner);
    }
    
    private static Typed[] types(Type[] types, Typed owner) {
        Typed[] result = new Typed[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = Typed.type(types[i], owner);
        }
        return result;
    }

    /**
     * Return a {@link Typed} for the specified {@link Type}. Note: The
     * resulting {@link Typed} may not be {@link Serializable}.
     *
     * @param type the source type or {@code null}
     * @return a {@link Typed} for the specified {@link Type}
     * @see #forType(Type, ResolvableType)
     */
    public static Typed type(Type type) {
        return type(type, null, null);
    }

    /**
     * Return a {@link Typed} for the specified {@link Type} backed by
     * a given {@link VariableResolver}.
     *
     * @param type the source type or {@code null}
     * @param resolutionContext the variable resolver or {@code null}
     * @return a {@link Typed} for the specified {@link Type} and
     * {@link VariableResolver}
     */
    static Typed type(Type type, Typed resolutionContext) {
        return type(type, null, resolutionContext);
    }

    /**
     * Return a {@link Typed} for the specified {@link Type} backed by
     * a given {@link VariableResolver}.
     *
     * @param type the source type or {@code null}
     * @param typeProvider the type provider or {@code null}
     * @param resolutionContext the variable resolver or {@code null}
     * @return a {@link Typed} for the specified {@link Type} and
     * {@link VariableResolver}
     */
    static Typed type(Type type, TypeProvider typeProvider, Typed resolutionContext) {
        if (type == null && typeProvider != null) {
            type = typeProvider.getType();
        }
        if (type == null) {
            return NONE;
        }

        // For simple Class references, build the wrapper right away -
        // no expensive resolution necessary, so not worth caching...
        if (type instanceof Class) {
            return new Typed(type, typeProvider, resolutionContext, (Typed) null);
        }

        // Purge empty entries on access since we don't have a clean-up thread or the like.
        //sbi: cache.purgeUnreferencedEntries();
        // Check the cache - we may have a ResolvableType which has been resolved before...
        Typed key = new Typed(type, typeProvider, resolutionContext);
        Typed resolvableType = CACHE.get(key);
        if (resolvableType == null) {
            resolvableType = new Typed(type, typeProvider, resolutionContext, key.hash);
            CACHE.put(resolvableType, resolvableType);
        }
        return resolvableType;
    }

    public Field[] getFields() {
        return resolve().getFields();
    }

    public Method[] getMethods() {
        return resolve().getMethods();
    }

    public Optional<Constructor> constructor() {
        try {
            return Optional.of(resolve().getConstructor());
        } catch (NoSuchMethodException | SecurityException ex) {
            return Optional.empty();
        }
    }
}
