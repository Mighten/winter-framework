package io.github.mighten.winter.io;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


public class ResourceResolverTest {

    /**
     * Unit Test for scanning classes in folders
     */
    @Test
    public void scanClass() {
        String testBasePackageName = "io.github.mighten.scan";
        var resourceResolver = new ResourceResolver(testBasePackageName);
        List<String> scannedResults = resourceResolver.scan(resource -> {
            String name = resource.name();
            if (name.endsWith(".class")) {
                // if name = io\github\mighten\scan\custom\level2\Level2Class.class
                //   return: io.github.mighten.scan.custom.level2.Level2Class
                return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
            }
            return null;
        });
        Collections.sort(scannedResults);

        System.out.println("===== Enter Unit Test: scanning path /io/github/mighten/scan/custom/* =====");
        System.out.println(scannedResults);

        String[] correctAnswer = new String[]{
                // list of some scan classes:
                "io.github.mighten.scan.custom.Level1Class",
                "io.github.mighten.scan.custom.level2.Level2Class",
        };
        for (String clazz : correctAnswer) {
            assertTrue(scannedResults.contains(clazz));
        }
        System.out.println("===== Leave Unit Test: scanning path /io/github/mighten/scan/custom/* =====");
        return ;
    }

    /**
     * Unit Test for scanning classes in jars
     */
    // TODO: unit test for jar scanning
    @Test
    public void scanJar() {

    }

}
