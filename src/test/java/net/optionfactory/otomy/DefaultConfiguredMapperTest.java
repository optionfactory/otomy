package net.optionfactory.otomy;

import java.util.List;
import net.optionfactory.otomy.converters.CachingInspector;
import net.optionfactory.otomy.converters.strategies.Strategies;
import net.optionfactory.otomy.types.Typed;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author rferranti
 */
public class DefaultConfiguredMapperTest {

    public static class SimpleBean {

        private static SimpleBean of(String value) {
            SimpleBean b = new SimpleBean();
            b.a = value;
            return b;
        }
        public String a;
    }

    private final Mapper mapper = new TypedMapper(new CachingInspector(), Strategies.defaults(), TypedMapper.Tracing.Disabled);
    public final List<SimpleBean> TARGET_TYPE = null;
    @Test
    public void objectArrayToList() throws NoSuchFieldException {
        SimpleBean[] source = new SimpleBean[]{
            SimpleBean.of("1"),
            SimpleBean.of("2")
        };

        List<SimpleBean> got = (List<SimpleBean>)mapper.map(source, Typed.field(DefaultConfiguredMapperTest.class.getField("TARGET_TYPE"), Typed.class_(DefaultConfiguredMapperTest.class)));
        Assert.assertTrue(got instanceof List);
        Assert.assertEquals(2, got.size());
        Assert.assertEquals("1", got.get(0).a);
        Assert.assertEquals("2", got.get(1).a);
    }
}
