package io.github.mighten.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import io.github.mighten.winter.utils.YamlUtils;
import org.junit.jupiter.api.Test;


public class YamlUtilsTest {

    /**
     * read application.yml
     */
    @Test
    public void testLoadYaml() {
        Map<String, Object> configs = YamlUtils.loadYamlAsPlainMap("/application.yml");
        for (String key : configs.keySet()) {
            Object value = configs.get(key);
            System.out.println(key + ": " + value + " (" + value.getClass() + ")");
        }
        assertEquals("winter-context", configs.get("winter.application.name") );
        assertEquals("1.0.0", configs.get("winter.application.version"));
        assertNull(configs.get("winter.application.port"));
    }
}
