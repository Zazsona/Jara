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
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleManager
{
    private static HashSet<String> reservedAliases;
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);

    /**
     * Parses through each jar within the modules folder and gathers its {@link CommandAttributes}.
     * @return the list of {@link CommandAttributes}
     */
    public static LinkedList<CommandAttributes> getAllCommandAttributes()
    {
        reservedAliases = new HashSet<>();
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
                    CommandAttributes ca = getCommandAttributes(file.getPath());
                    if (ca != null)
                    {
                        cas.add(ca);
                        reservedAliases.addAll(Arrays.asList(ca.getAliases()));
                    }

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

    /**
     * Gets the {@link CommandAttributes} for the specified jar.
     * @param jarPath the jar to analyse
     * @return the attributes of the command in the module, or null if unavailable
     * @throws ClassNotFoundException jar layout is invalid
     * @throws IOException unable to access jar
     */
    private static CommandAttributes getCommandAttributes(String jarPath) throws ClassNotFoundException, IOException
    {
        CommandAttributes ca = null;
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> entries = jarFile.entries();

        URL[] urls = {new URL("jar:file:" + jarPath + "!/")};
        URLClassLoader cl = URLClassLoader.newInstance(urls);

        JarEntry jarPact = jarFile.getJarEntry("pact.json");
        if (jarPact == null)
        {
            logger.error(jarFile.getName() + " has no pact.");
            return null;
        }

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
                CommandAttributes pactCA = getAttributesInPact(jarFile, jarPact);
                /*
                    Here we check to ensure there isn't any alias overlap, as this would cause issues with alias hunting.
                 */
                if (!Collections.disjoint(reservedAliases, Arrays.asList(pactCA.getAliases()))) //Fucking arrays.
                {
                    if (reservedAliases.contains(pactCA.getCommandKey()))
                    {
                        logger.error(jarFile.getName()+" has a conflicting key: "+pactCA.getCommandKey()+". It cannot be used.");
                        break;
                    }
                    else
                    {
                        logger.info(jarFile.getName()+" has overlapping aliases in the pact:");
                        ArrayList<String> aliases = new ArrayList<>();
                        for (String alias : pactCA.getAliases())
                        {
                            if (!reservedAliases.contains(alias))
                            {
                                aliases.add(alias);
                            }
                            else
                            {
                                logger.info(alias);
                            }
                        }
                        logger.info("These will be ignored from "+jarFile.getName());
                        if (aliases.size() == 0)
                        {
                            logger.error(jarEntry.getName()+" has NO non-conflicting aliases. It cannot be run.");
                        }
                        pactCA = new CommandAttributes(pactCA.getCommandKey(), pactCA.getDescription(), c, aliases.toArray(pactCA.getAliases()), pactCA.getCategory(), pactCA.isDisableable()); //TODO: Maybe it's time to add some setters.
                    }
                }
                ca = new CommandAttributes(pactCA.getCommandKey(), pactCA.getDescription(), c, pactCA.getAliases(), pactCA.getCategory(), pactCA.isDisableable());
            }
        }
        if (ca == null)
        {
            logger.info(jarFile.getName()+" has no entry point. (That is, a class that extends Command)");                  //Some (few) modules won't be commands. This allows for that possibility along with highlighting the error in case it is intended to be a command.
            return null;
        }
        return ca;
    }

    /**
     * Gets the attributes defined in the pact file and converts them to {@link CommandAttributes}.
     * @param jarFile the jar of the module
     * @param jarPact the pact
     * @return the {@link CommandAttributes} defined in the pact. The class will be null.
     * @throws IOException unable to access pact
     */
    private static CommandAttributes getAttributesInPact(JarFile jarFile, JarEntry jarPact) throws IOException
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

    //TODO: Help menus
}
