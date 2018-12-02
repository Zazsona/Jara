package jara;

import com.google.gson.Gson;
import commands.Command;
import commands.NewHelp;
import configuration.SettingsUtil;
import exceptions.ConflictException;
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
    /**
     * Set containing all registered aliases.
     */
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
            catch (IOException | ClassNotFoundException | ConflictException e)
            {
                logger.error(e.getMessage());
            }
        }
        logger.info("Loaded "+cas.size()+" modules.");
        return cas;
    }

    /**
     * Gets the {@link CommandAttributes} for the specified jar.
     * @param jarPath the jar to analyse
     * @return the attributes of the command in the module, or null if unavailable
     * @throws ClassNotFoundException jar layout is invalid
     * @throws IOException unable to access jar
     * @throws ConflictException unable to resolve pact conflicts
     */
    private static CommandAttributes getCommandAttributes(String jarPath) throws ClassNotFoundException, IOException, ConflictException
    {
        CommandAttributes ca = null;
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> entries = jarFile.entries();

        URL[] urls = {new URL("jar:file:" + jarPath + "!/")};
        URLClassLoader cl = URLClassLoader.newInstance(urls);

        JarEntry jarPact = jarFile.getJarEntry("pact.json");

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

            if (Command.class.isAssignableFrom(c) && jarPact != null)
            {
                CommandAttributes pactCA = getAttributesInPact(jarFile, jarPact);
                ca = new CommandAttributes(pactCA.getCommandKey(), pactCA.getDescription(), c, pactCA.getAliases(), pactCA.getCategory(), pactCA.isDisableable());

                JarEntry jarHelp = jarFile.getJarEntry("help.json");
                if (jarHelp != null)
                {
                    NewHelp.addPage(pactCA.getCommandKey(), getHelpPage(jarFile, jarHelp));
                }
                else
                {
                    logger.info(jarFile.getName()+" has no help page.");
                }
            }
        }
        if (jarPact == null)
        {
            throw new ClassNotFoundException(jarFile.getName() + " has no pact.");
        }
        if (ca == null) //If no Command class is found...
        {
            throw new ClassNotFoundException(jarFile.getName()+" has no entry point. (That is, a class that extends Command)");
        }
        return ca;
    }

    /**
     * Gets the non-conflicting attributes defined in the pact file and converts them to {@link CommandAttributes}.
     * @param jarFile the jar of the module
     * @param jarPact the pact
     * @return the {@link CommandAttributes} defined in the pact, where the class will be null.
     * @throws IOException unable to access pact
     * @throws ConflictException unable to resolve conflicts
     */
    private static CommandAttributes getAttributesInPact(JarFile jarFile, JarEntry jarPact) throws IOException, ConflictException
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
        CommandAttributes pactCA = gson.fromJson(jsonBuilder.toString(), CommandAttributes.class);
        return resolveConflicts(jarFile, pactCA);
    }

    /**
     * Gets the help info for the specified module and file
     * @param jarFile the jar of the module
     * @param jarHelp the help page entry in the jar
     * @return the help page
     * @throws IOException unable to access help page
     */
    private static NewHelp.HelpPage getHelpPage(JarFile jarFile, JarEntry jarHelp) throws IOException //TODO: Combine this and getCommandAttributesInPact, as functionality is near identical.
    {
        InputStreamReader is = new InputStreamReader(jarFile.getInputStream(jarHelp));
        BufferedReader br = new BufferedReader(is);
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
        {
            jsonBuilder.append(line);
        }
        Gson gson = new Gson();
        return gson.fromJson(jsonBuilder.toString(), NewHelp.HelpPage.class);
    }

    /**
     * Attempts to resolve any conflicts with the aliases in the pact.<br>
     *     Key conflicts cannot be resolved, as these must be unique.
     * @param jarFile the jar file of the module
     * @param pactCA the command attributes in the pact
     * @return the resolved command attributes with a null class.
     * @throws ConflictException unable to resolve conflicts
     */
    private static CommandAttributes resolveConflicts(JarFile jarFile, CommandAttributes pactCA) throws ConflictException
    {
        if (!Collections.disjoint(reservedAliases, Arrays.asList(pactCA.getAliases()))) //Fucking arrays.
        {
            if (reservedAliases.contains(pactCA.getCommandKey()))
            {
                throw new ConflictException(jarFile.getName()+" has a conflicting key: "+pactCA.getCommandKey()+". It cannot be used.");
            }
            else    //TODO: One possible idea here is to remove the aliases from the modules that will end up with the highest quantity. However, this is an expensive operation as there may be many modules with conflicting aliases, which need to be searched for.
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
                    throw new ConflictException(jarFile.getName()+" has NO non-conflicting aliases. It cannot be run.");
                }
                pactCA = new CommandAttributes(pactCA.getCommandKey(), pactCA.getDescription(), null, aliases.toArray(pactCA.getAliases()), pactCA.getCategory(), pactCA.isDisableable());
            }
        }
        return pactCA;
    }

    //TODO: Help menus
}
