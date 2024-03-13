package io.github.mighten.winter.io;


import io.github.mighten.winter.io.entities.PropertyEntity;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.*;
import java.util.function.Function;

public class PropertyResolver {

    /**
     * store all properties
     */
    Map<String, String> properties = new HashMap<>();

    /**
     *  convert String to a corresponding type
     */
    Map<Class<?>, Function<String, Object>> converters = new HashMap<>();


    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     *  parse properties:
     *  1. "winter.name"
     *  2. "${winter.app.name}",
     *      usage: `@Value("${winter.app.name}")`
     *  3. "${winter.app.name:winter}",
     *      usage: `@Value("${app.title:Summer}")`
     * @param properties
     */
    public PropertyResolver(Properties properties) {
        this.properties.putAll(System.getenv());
        Set<String> propertyNames = properties.stringPropertyNames();
        for (String name : propertyNames)
            this.properties.put(name, properties.getProperty(name));

        this.registerConverters();
    }

    /**
     * Dump current properties form PropertyResolver (Debugging purposes only)
     */
    public void dumpAllProperties() {
        List<String> keys = new ArrayList<>(this.properties.keySet());
        Collections.sort(keys);
        logger.debug("===== Dump all properties from `PropertyResolver` =====");
        for (String key : keys)
            logger.debug("{} --> {}", key, this.properties.get(key) );
        logger.debug("=======================================================");
    }


    /**
     * check if a property is cached into `this.properties`
     * @param key querying key for a property
     * @return
     */
    public boolean containsProperty(String key) {
        return this.properties.containsKey(key);
    }

    /**
     * read property expression and get property value or default value
     * @param propertyExpression
     * @return
     */
    @Nullable
    public String getProperty(String propertyExpression) {
        PropertyEntity property = parsePropertyExpression(propertyExpression);
        if (property != null) {
            if (property.defaultValue() != null)
                return getPropertyOrDefault(property.key(), property.defaultValue());
            else
                return getRequiredProperty(property.key());
        }

        String propertyValue = this.properties.get(propertyExpression);
        if (propertyValue != null)
            propertyValue = parseValue(propertyValue);
        return propertyValue;
    }


    /**
     * get the property value, or assign the property with default value in the expression
     * @param key
     * @param defaultValue
     * @return
     */
    public String getPropertyOrDefault(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? parseValue(defaultValue) : value;
    }

    @Nullable
    public <T> T getProperty(String key, Class<T> targetType) {
        String value = getProperty(key);
        if (value == null)
            return null;
        return convert(targetType, value);
    }


    /**
     * get converted object from property expression
     * @param key property name
     * @param targetType class object
     * @param defaultValue default value
     * @return
     */
    public <T> T getConvertedPropertyOrDefault(String key, Class<T> targetType, T defaultValue) {
        String value = getProperty(key);
        if (value == null)
            return defaultValue;
        return convert(targetType, value);
    }


    /**
     * Get property value without default value
     * @param key
     * @return
     */
    public String getRequiredProperty(String key) {
        String value = getProperty(key);
        return Objects.requireNonNull(value, "Property '" + key + "' not found.");
    }


    /**
     * Get property value without default value, and cast type
     * @param key the name for the key
     * @param targetType the target type of cast result
     * @return the converted object
     */
    public <T> T getRequiredProperty(String key, Class<T> targetType) {
        T value = getProperty(key, targetType);
        return Objects.requireNonNull(value, "Property '" + key + "' not found.");
    }


    /**
     * convert value into type specified by clazz
     * @param clazz the class object for the target type, e.g., Integer.class
     * @param value the string to be converted
     * @param <T> T is any target it can cast, or null if unspecified
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> T convert(Class<?> clazz, String value) {
        Function<String, Object> converter = this.converters.get(clazz);
        if (converter == null)
            throw new IllegalArgumentException("Unsupported value type: " + clazz.getName());
        return (T) converter.apply(value);
    }


    String parseValue(String value) {
        PropertyEntity property = parsePropertyExpression(value);
        if (property == null)
            return value; // failed to convert
        if (property.defaultValue() != null)
            return getPropertyOrDefault(property.key(), property.defaultValue());
        return getRequiredProperty(property.key()); // if failed, throw IllegalArgumentException
    }


    /**
     * parse ${keyName} or ${keyName : defaultValue}
     * @param key
     * @return
     */
    private PropertyEntity parsePropertyExpression(String key) {
        if (key.startsWith("${") && key.endsWith("}")) {
            int n = key.indexOf(':');
            if (n == (-1)) {
                // no default value, i.e., ${key}
                String k = assertNotEmpty(key.substring(2, key.length() - 1));
                return new PropertyEntity(k, null);
            } else {
                // has default value, i.e., ${key:default}
                String k = assertNotEmpty(key.substring(2, n));
                return new PropertyEntity(k, key.substring(n + 1, key.length() - 1));
            }
        }
        return null;
    }


    /**
     * make sure the key is not empty, otherwise, throw `IllegalArgumentException`
     * @param key
     * @return exactly the same input of the original `key`
     */
    private String assertNotEmpty(String key) {
        if (key.isEmpty())
            throw new IllegalArgumentException("Invalid key: " + key);
        return key;
    }


    /**
     * register converters,
     *     so that `converters` can cast any String object to commonly used object.
     *  e.g.,
     *      int num = 123;
     *      String str = "123";
     *      int num = converters.get(str);
     *      String str = converters.get(num)
     */
    private void registerConverters() {
        converters.put(String.class, s -> s);

        converters.put(boolean.class, s -> Boolean.parseBoolean(s));
        converters.put(Boolean.class, s -> Boolean.valueOf(s));

        converters.put(byte.class, s -> Byte.parseByte(s));
        converters.put(Byte.class, s -> Byte.valueOf(s));

        converters.put(short.class, s -> Short.parseShort(s));
        converters.put(Short.class, s -> Short.valueOf(s));

        converters.put(int.class, s -> Integer.parseInt(s));
        converters.put(Integer.class, s -> Integer.valueOf(s));

        converters.put(long.class, s -> Long.parseLong(s));
        converters.put(Long.class, s -> Long.valueOf(s));

        converters.put(float.class, s -> Float.parseFloat(s));
        converters.put(Float.class, s -> Float.valueOf(s));

        converters.put(double.class, s -> Double.parseDouble(s));
        converters.put(Double.class, s -> Double.valueOf(s));

        converters.put(LocalDate.class, s -> LocalDate.parse(s));
        converters.put(LocalTime.class, s -> LocalTime.parse(s));
        converters.put(LocalDateTime.class, s -> LocalDateTime.parse(s));
        converters.put(ZonedDateTime.class, s -> ZonedDateTime.parse(s));
        converters.put(Duration.class, s -> Duration.parse(s));
        converters.put(ZoneId.class, s -> ZoneId.of(s));
    }

}
