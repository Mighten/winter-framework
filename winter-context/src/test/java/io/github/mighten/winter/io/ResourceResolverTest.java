package io.github.mighten.winter.io;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
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
                "io.github.mighten.scan.custom.AnnotationScan",
                "io.github.mighten.scan.custom.Level1Class",
                "io.github.mighten.scan.custom.level2.Level2Class",
        };
        for (String clazz : correctAnswer) {
            assertTrue(scannedResults.contains(clazz));
        }
        System.out.println("===== Leave Unit Test: scanning path /io/github/mighten/scan/custom/* =====");
    }

    /**
     * Unit Test for scanning classes in jars
     */
    @Test
    public void scanJar() {
        String packageName = Test.class.getPackageName();
        ResourceResolver resourceResolver = new ResourceResolver(packageName);
        List<String> scannedResults = resourceResolver.scan(resource -> {
            String name = resource.name();
            if (name.endsWith(".class")) {
                return name.substring(0, name.length() - 6).replace("/", ".").replace("\\", ".");
            }
            return null;
        });

        System.out.println("===== Enter Unit Test: scanning path /org/junit/jupiter/api/* =====");
        System.out.println(scannedResults);

        assertTrue(scannedResults.contains(Test.class.getName()) );
        assertTrue(scannedResults.contains(Assertions.class.getName()) );
        System.out.println("===== Leave Unit Test: scanning path /org/junit/jupiter/api/* =====");
    }

}
