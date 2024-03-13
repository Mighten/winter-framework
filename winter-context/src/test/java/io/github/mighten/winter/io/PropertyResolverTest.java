package io.github.mighten.winter.io;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyResolverTest {

    /**
     * Unit test for resolving property expression without default value
     */
    @Test
    public void testGetRequiredProperty() {
        Properties properties = new Properties();
        properties.setProperty("winter.application.name", "winter-context");
        properties.setProperty("winter.application.version", "1.0.0");

        PropertyResolver propertyResolver = new PropertyResolver(properties);
        assertEquals("winter-context", propertyResolver.getProperty("${winter.application.name}"));
        assertEquals("1.0.0", propertyResolver.getProperty("winter.application.version"));
    }

    /**
     * Unit test for resolving property expression with default value
     */
    @Test
    public void testPropertyGetOrDefault() {
        PropertyResolver propertyResolver = new PropertyResolver(new Properties() );
        assertEquals(1024, propertyResolver.getProperty("${coding-day:1024}", int.class));
        assertEquals("/not-exist", propertyResolver.getProperty("${app.path:${app.home:${ENV_NOT_EXIST:/not-exist}}}"));
    }
}
