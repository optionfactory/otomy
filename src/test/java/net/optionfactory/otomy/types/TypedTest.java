package net.optionfactory.otomy.types;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author rferranti
 */
public class TypedTest {

    public static class Box<T> {

        public T value;

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

    }

    public static class Source {

        public Box<Box<Integer>> wrapped;

        public Box<Box<Integer>> getWrapped() {
            return wrapped;
        }

        public void setWrapped(Box<Box<Integer>> wrapped) {
            this.wrapped = wrapped;
        }

    }

    public static class Target {

        public Box<Box<Integer>> wrapped;

        public Box<Box<Integer>> getWrapped() {
            return wrapped;
        }

        public void setWrapped(Box<Box<Integer>> wrapped) {
            this.wrapped = wrapped;
        }

    }

    @Test
    public void fieldsTypeResolution() throws Exception {
        Typed wrapped = Typed.field(Source.class.getField("wrapped"), Typed.class_(Source.class));
        Typed value1 = Typed.field(Box.class.getField("value"), wrapped);
        Typed value2 = Typed.field(Box.class.getField("value"), value1);
        Assert.<Class<?>>assertEquals(Integer.class, value2.resolve());
    }

    @Test
    public void accessorsTypeResolution() throws Exception {
        Typed wrapped = Typed.returnType(Source.class.getMethod("getWrapped"), Typed.class_(Source.class));
        Typed value1 = Typed.returnType(Box.class.getMethod("getValue"), wrapped);
        Typed value2 = Typed.returnType(Box.class.getMethod("getValue"), value1);
        Assert.<Class<?>>assertEquals(Integer.class, value2.resolve());

    }

    @Test
    public void mutatorsTypeResolution() throws Exception {
        Typed wrapped = Typed.parameter(Target.class.getMethod("setWrapped", Box.class), 0, Typed.class_(Target.class));
        Typed value1 = Typed.parameter(Box.class.getMethod("setValue", Object.class), 0, wrapped);
        Typed value2 = Typed.parameter(Box.class.getMethod("setValue", Object.class), 0, value1);
        Assert.<Class<?>>assertEquals(Integer.class, value2.resolve());
    }

    public static class BeanWithMap {

        public Map<String, Map<String, Integer>> field;
    }

    @Test
    public void nested() throws Exception {
        Typed field = Typed.field(BeanWithMap.class.getField("field"), Typed.class_(BeanWithMap.class));
        Assert.assertEquals("java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Integer>>", field.toString());
    }

    public static class AssignableFst extends HashMap<String, Integer> {

    }

    public static class AssignableSnd extends HashMap<String, Integer> {

    }

    public static class Unassignable extends HashMap<String, String> {

    }

    @Test
    public void assignableThroughBaseWhenSameTypeArguments() {
        Typed fst = Typed.class_(AssignableFst.class).as(HashMap.class);
        Typed snd = Typed.class_(AssignableSnd.class).as(HashMap.class);
        Assert.assertTrue(fst.isAssignableFrom(snd));
    }

    @Test
    public void notAssignableThroughBaseWhenTypeArgumentsDiffer() {
        Typed fst = Typed.class_(AssignableFst.class).as(HashMap.class);
        Typed una = Typed.class_(Unassignable.class).as(HashMap.class);
        Assert.assertFalse(fst.isAssignableFrom(una));
    }

}
