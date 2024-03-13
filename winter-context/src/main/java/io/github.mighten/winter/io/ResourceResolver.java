package io.github.mighten.winter.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple classpath scan works both in directory and jar:
 */
public class ResourceResolver {

    Logger logger = LoggerFactory.getLogger(getClass());

    String basePackage;

    /**
     * to initialize scanner with param `basePackage`
     * @param basePackage
     */
    public ResourceResolver(String basePackage) {
        this.basePackage = basePackage;
    }


    /**
     *
     * @param mapper
     * @param <R>
     * @return
     */
    public <R> List<R> scan(Function<Resource, R> mapper) {
        String basePackagePath = this.basePackage.replace(".", "/");
        String path = basePackagePath;
        try {
            List<R> fullClassNameList = new ArrayList<>();
            triage(basePackagePath, path, fullClassNameList, mapper);
            return fullClassNameList;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * scan differently according to its type (file, jar)
     * @param basePackagePath
     * @param path
     * @param collector
     * @param mapper
     * @throws IOException
     * @throws URISyntaxException
     */
    <R> void triage(String basePackagePath, String path, List<R> collector, Function<Resource, R> mapper) throws IOException, URISyntaxException {
        logger.atDebug().log("scan path: {}", path);
        Enumeration<URL> en = getContextClassLoader().getResources(path);
        while (en.hasMoreElements()) {
            URL url = en.nextElement();
            URI uri = url.toURI();
            String uriStr = removeTrailingSlash(uriToString(uri));
            String uriBaseStr = uriStr.substring(0, uriStr.length() - basePackagePath.length());

            if (uriBaseStr.startsWith("file:") )
                uriBaseStr = uriBaseStr.substring(5);
            if (uriStr.startsWith("jar:") )
                scanFile(true, uriBaseStr, jarUriToPath(basePackagePath, uri), collector, mapper);
            else
                scanFile(false, uriBaseStr, Paths.get(uri), collector, mapper);
        }
        return ;
    }

    /**
     * Get ClassLoader
     *      from Servlet Container or current Class
     * @return
     */
    ClassLoader getContextClassLoader() {
        // Try to get `ClassLoader` from Servlet Container
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        // if Servlet Container failed to offer `ClassLoader`,
        //    then retry to get `ClassLoader` from current Class
        if (cl == null)
            cl = getClass().getClassLoader();
        return cl;
    }

    /**
     * convert *.jar to basePackage Path
     * @param basePackagePath
     * @param jarUri
     * @return
     * @throws IOException
     */
    Path jarUriToPath(String basePackagePath, URI jarUri) throws IOException {
        return FileSystems.newFileSystem(jarUri, Map.of()).getPath(basePackagePath);
    }

    /**
     * traverse all files in the given path,
     *      collect jar or regular file with `collector`
     * @param isJar
     * @param base
     * @param root
     * @param collector
     * @param mapper
     * @throws IOException
     */
    <R> void scanFile(boolean isJar, String base, Path root, List<R> collector, Function<Resource, R> mapper) throws IOException {
        String baseDir = removeTrailingSlash(base);
        Files.walk(root).filter(Files::isRegularFile).forEach(file -> {
            Resource resource = null;

            if (isJar) {
                resource = new Resource(baseDir, removeLeadingSlash(file.toString()));
            } else {
                String path = file.toString();
                String name = removeLeadingSlash(path.substring(baseDir.length()));
                resource = new Resource("file:" + path, name);
            }

            logger.atDebug().log("found resource: {}", resource);

            R r = mapper.apply(resource);
            if (r != null)
                collector.add(r);
        });

        return ;
    }

    String uriToString(URI uri) {
        return URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8);
    }

    String removeLeadingSlash(String s) {
        if (s.startsWith("/") || s.startsWith("\\")) {
            s = s.substring(1);
        }
        return s;
    }

    String removeTrailingSlash(String s) {
        if (s.endsWith("/") || s.endsWith("\\")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
}
