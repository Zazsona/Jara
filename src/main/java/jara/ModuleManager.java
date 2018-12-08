package jara;

import com.google.gson.Gson;
import commands.Command;
import commands.Load;
import commands.NewHelp;
import configuration.SettingsUtil;
import exceptions.ConflictException;
import exceptions.InvalidModuleException;
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
     * The collection of classes to be run during program boot
     */
    private static HashMap<Class<? extends Load>, JarFile> onLoadClasses;

    /**
     * The number of issues that will not impact operations
     */
    private static int warnings = 0;
    /**
     * The number of issues that may have an impact on operation
     */
    private static int errors = 0;
    /**
     * Parses through each jar within the modules folder and gathers its {@link CommandAttributes}.
     * @return the list of {@link CommandAttributes}
     * @throws IOException one or more fatal errors occurred during module loading
     */
    public static LinkedList<CommandAttributes> loadModules() throws InvalidModuleException
    {
        onLoadClasses = new HashMap<>();
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
                    CommandAttributes ca = loadModule(file.getPath());
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
                errors++;
            }
        }
        for (Class<? extends Load> c : onLoadClasses.keySet())
        {
            try
            {
                c.newInstance().load();
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                logger.error("Unable to instantiate "+onLoadClasses.get(c).getName()+"'s load class. There is a high risk this module will not perform correctly, if at all.");
                errors++;
            }

        }
        if (errors <= 0)
        {
            logger.info("Loaded "+cas.size()+" modules. ("+warnings+" warnings)");
            return cas;
        }
        else
        {
            throw new InvalidModuleException("Attempted to load "+cas.size()+" modules, but failed. ("+errors+" errors) ("+warnings+" warnings)");
        }
    }

    /**
     * Gets the {@link CommandAttributes} for the specified jar.
     * @param jarPath the jar to analyse
     * @return the attributes of the command in the module, or null if unavailable
     * @throws ClassNotFoundException jar layout is invalid
     * @throws IOException unable to access jar
     * @throws ConflictException unable to resolve pact conflicts
     */
    private static CommandAttributes loadModule(String jarPath) throws ClassNotFoundException, IOException, ConflictException
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
                    for (String alias : ca.getAliases())
                    {
                        NewHelp.addPage(alias.toLowerCase(), getHelpPage(jarFile, jarHelp));
                    }
                }
                else
                {
                    for (String alias : ca.getAliases())
                    {
                        NewHelp.addPage(alias.toLowerCase(), new NewHelp.HelpPage()); //Default values
                    }
                    logger.info(jarFile.getName()+" has no help page.");
                    warnings++;
                }
            }
            if (Load.class.isAssignableFrom(c))
            {
                onLoadClasses.put(c, jarFile);
            }
        }
        if (jarPact == null)
        {
            warnings++;
            throw new ClassNotFoundException(jarFile.getName() + " has no pact.");
        }
        if (ca == null) //If no Command class is found...
        {
            warnings++;
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
        Gson gson = new Gson();
        CommandAttributes pactCA = gson.fromJson(getJson(jarFile, jarPact), CommandAttributes.class);
        return resolveConflicts(jarFile, pactCA);
    }

    /**
     * Gets the help page for the module
     * @param jarFile the jar of the module
     * @param jarHelp the help file
     * @return the {@link commands.NewHelp.HelpPage} from the file
     * @throws IOException unable to access file
     */
    private static NewHelp.HelpPage getHelpPage(JarFile jarFile, JarEntry jarHelp) throws IOException
    {
        Gson gson = new Gson();
        return gson.fromJson(getJson(jarFile, jarHelp), NewHelp.HelpPage.class);
    }

    /**
     * Gets the json data from the specified file as a String
     * @param jarFile the jar of the module
     * @param jarEntry the file in the jar
     * @return the json as a String
     * @throws IOException unable to access jar page
     */
    private static String getJson(JarFile jarFile, JarEntry jarEntry) throws IOException
    {
        InputStreamReader is = new InputStreamReader(jarFile.getInputStream(jarEntry));
        BufferedReader br = new BufferedReader(is);
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
        {
            jsonBuilder.append(line);
        }
        return jsonBuilder.toString();
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
                errors++;
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
                    errors++;
                    throw new ConflictException(jarFile.getName()+" has NO non-conflicting aliases. It cannot be run.");
                }
                pactCA = new CommandAttributes(pactCA.getCommandKey(), pactCA.getDescription(), null, aliases.toArray(pactCA.getAliases()), pactCA.getCategory(), pactCA.isDisableable());
            }
        }
        return pactCA;
    }

    //TODO: Help menus
}
