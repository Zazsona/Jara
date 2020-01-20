package jara;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Loader for resources in modules
 */
public class ModuleResourceLoader
{
    private static final HashMap<String, String> keyToURLMap = new HashMap<>();

    /**
     * Registers the file location of the module denoted by the key
     * @param key the module's key
     * @param url the module's file location
     */
    protected static void registerModule(String key, String url)
    {
        keyToURLMap.put(key, url);
    }

    /**
     * Gets an InputStream for resource access
     * @param moduleKey the module's key
     * @param resPath the location of the resource in the module (E.g src/zazsona/jara/image.png)
     * @return the InputStream, or null if the key is invalid, or no location is registered.
     * @throws IOException error accessing module jar.
     */
    public static InputStream getResourceStream(String moduleKey, String resPath) throws IOException
    {
        if (keyToURLMap.containsKey(moduleKey))
        {
            JarFile jarFile = new JarFile(keyToURLMap.get(moduleKey));
            JarEntry jarEntry = jarFile.getJarEntry(resPath);
            return jarFile.getInputStream(jarEntry);
        }
        return null;
    }

    /**
     * Gets the location path of the module denoted by the key
     * @param moduleKey the module's unique key
     * @return the path, or null if the key is invalid
     */
    public static String getModulePath(String moduleKey)
    {
        return keyToURLMap.get(moduleKey);
    }
}
