package jara;

import com.google.gson.Gson;
import commands.Command;
import configuration.SettingsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleManager
{
    Logger logger = LoggerFactory.getLogger(getClass());

    public LinkedList<CommandAttributes> getAllCommandAttributes()
    {
        LinkedList<CommandAttributes> cas = new LinkedList<>();
        File moduleDir = new File(SettingsUtil.getDirectory() + "/modules/");
        if (!moduleDir.exists())
            moduleDir.mkdirs();

        for (File file : moduleDir.listFiles())
        {
            try
            {
                if (file.isFile() && file.getName().endsWith(".jar"))
                {
                    cas.add(getCommandAttributes(file.getPath()));
                }
            }
            catch (IOException e)
            {
                logger.error("An error occurred when attempting to load a module.");
            }
            catch (ClassNotFoundException e)
            {
                logger.error("An error occurred when attempting to load classes in the module.");
            }
        }
        return cas;
    }

    private CommandAttributes getCommandAttributes(String jarPath) throws ClassNotFoundException, IOException
    {
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> entries = jarFile.entries();

        URL[] urls = { new URL("jar:file:" + jarPath + "!/") };
        URLClassLoader cl = URLClassLoader.newInstance(urls);
        while (entries.hasMoreElements())
        {
            JarEntry jarEntry = entries.nextElement();
            if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class"))
            {
                continue;
            }
            String className = jarEntry.getName().substring(0, jarEntry.getName().length() - 6);
            className = className.replace("/", ".");
            Class c = cl.loadClass(className);

            if (Command.class.isAssignableFrom(c))
            {
                JarEntry jarPact = jarFile.getJarEntry("pact.json");
                if (jarPact == null)
                {
                    logger.error(jarFile.getName() + " has no pact.");
                    break;
                }
                CommandAttributes pactCA = getAttributesInPact(jarFile, jarPact);
                return new CommandAttributes(pactCA.getCommandKey(), pactCA.getDescription(), c, pactCA.getAliases(), pactCA.getCategory(), pactCA.isDisableable());
            }
        }
        return null;
    }

    private CommandAttributes getAttributesInPact(JarFile jarFile, JarEntry jarPact) throws IOException
    {
        InputStreamReader is = new InputStreamReader(jarFile.getInputStream(jarPact));
        BufferedReader br = new BufferedReader(is);
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
        {
            jsonBuilder.append(line);
        }
        Gson gson = new Gson();
        return gson.fromJson(jsonBuilder.toString(), CommandAttributes.class);
    }
}
