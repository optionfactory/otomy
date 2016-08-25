package net.optionfactory.otomy.converters;

import java.util.Date;
import net.optionfactory.otomy.TypedMapper;
import net.optionfactory.otomy.converters.strategies.Strategies;
import org.junit.Assert;
import org.junit.Test;

public class DatesTest {

    @Test
    public void mapDateToDate() {
        TypedMapper mapper = new TypedMapper(new CachingInspector(), Strategies.defaults(), TypedMapper.Tracing.Disabled);
        final Date gave = new Date();
        Date got = mapper.map(gave, Date.class);
        Assert.assertEquals(gave, got);
    }

    @Test
    public void mapDateToSqlDate() {
        TypedMapper mapper = new TypedMapper(new CachingInspector(), Strategies.defaults(), TypedMapper.Tracing.Disabled);
        final Date gave = new Date();
        java.sql.Date got = mapper.map(gave, java.sql.Date.class);
        Assert.assertEquals(gave, got);
        Assert.assertTrue(got instanceof java.sql.Date);
    }

    @Test
    public void mapLongToDate() {
        TypedMapper mapper = new TypedMapper(new CachingInspector(), Strategies.defaults(), TypedMapper.Tracing.Disabled);
        final long gave = new Date().getTime();
        Date got = mapper.map(gave, Date.class);
        Assert.assertEquals(gave, got.getTime());
    }

    @Test
    public void mapLongToSqlDate() {
        TypedMapper mapper = new TypedMapper(new CachingInspector(), Strategies.defaults(), TypedMapper.Tracing.Disabled);
        final long gave = new Date().getTime();
        java.sql.Date got = mapper.map(gave, java.sql.Date.class);
        Assert.assertEquals(gave, got.getTime());
    }
}
