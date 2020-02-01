package jara;

import com.google.gson.Gson;
import module.ModuleCommand;
import module.ModuleLoad;
import commands.Help;
import configuration.SettingsUtil;
import exceptions.ConflictException;
import exceptions.InvalidModuleException;
import module.ModuleConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Responsible for module management (that is, loads, controls, and resolve issues with jars placed in the modules folder)
 */
public class ModuleLoader
{
    /**
     * Set containing all registered aliases.
     */
    private static HashSet<String> reservedAliases;
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ModuleLoader.class);
    /**
     * The collection of classes to be run during program boot
     */
    private static HashMap<JarFile, Class<? extends ModuleLoad>> onLoadClasses;

    /**
     * The number of issues that will not impact operations
     */
    private static int warnings = 0;
    /**
     * The number of issues that may have an impact on operation
     */
    private static int errors = 0;


    /**
     * Parses through each jar within the modules folder and gathers its {@link ModuleAttributes}.
     * @param register all currently loaded modules, including built-in ones.
     * @return the list of {@link ModuleAttributes}
     * @throws InvalidModuleException one or more fatal errors occurred during module loading
     */
    protected static synchronized LinkedList<ModuleAttributes> loadModules(ArrayList<ModuleAttributes> register) throws InvalidModuleException
    {
        onLoadClasses = new HashMap<>();
        reservedAliases = new HashSet<>();
        LinkedList<ModuleAttributes> moduleAttributes = new LinkedList<>();

        for (ModuleAttributes inBuiltCA : register)
        {
            //Adds all in-built aliases so modules can't try and claim these.
            reservedAliases.addAll(Arrays.asList(inBuiltCA.getAliases()));
        }

        File moduleDir = new File(SettingsUtil.getDirectory() + "/Modules/");
        if (!moduleDir.exists())
            moduleDir.mkdirs();

        URLClassLoader cl = getClassLoader(moduleDir);

        for (File file : moduleDir.listFiles())
        {
            try
            {
                if (file.isFile() && file.getName().endsWith(".jar"))
                {
                    ModuleAttributes ma = loadModule(file.getPath(), cl);
                    if (ma != null)
                    {
                        moduleAttributes.add(ma);
                        reservedAliases.addAll(Arrays.asList(ma.getAliases()));
                    }
                }
            }
            catch (IOException | ClassNotFoundException | ConflictException e)
            {
                logger.error(e.toString());
                errors++;
            }
        }
        for (Class<? extends ModuleLoad> c : onLoadClasses.values())
        {
            Thread loadThread = new Thread(() ->
                       {
                           try
                           {
                               c.getConstructor().newInstance().load();
                           }
                           catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
                           {
                               logger.error("Unable to instantiate "+onLoadClasses.get(c).getName()+"'s load class. There is a high risk this module will not perform correctly, if at all.");
                               errors++;
                           }
                       });
            loadThread.setName(c.getSimpleName());
            loadThread.start();
            //Without creating a new thread, if the load class is infinitely running, Jara will shit the bed and prompt the user to restart setup.
        }
        if (errors <= 0)
        {
            logger.info("Loaded "+moduleAttributes.size()+" modules. ("+warnings+" warnings)");
            return moduleAttributes;
        }
        else
        {
            throw new InvalidModuleException("Attempted to load "+moduleAttributes.size()+" modules, but failed. ("+errors+" errors) ("+warnings+" warnings)");
        }
    }

    /**
     * Gets the class loader
     * @param moduleDir the directory to load modules from
     * @return the class loader
     */
    private static URLClassLoader getClassLoader(File moduleDir)
    {
        ArrayList<URL> urls = new ArrayList<>();
        for (File file : moduleDir.listFiles())
        {
            try
            {
                if (file.isFile() && file.getName().endsWith(".jar"))
                {
                    urls.add(new URL("jar:file:" + file.getPath() + "!/"));
                }
            }
            catch (MalformedURLException e)
            {
                logger.info(e.toString());
            }
        }
        return URLClassLoader.newInstance(urls.toArray(new URL[0]));
    }

    /**
     * Gets the {@link ModuleAttributes} for the specified jar.
     * @param jarPath the jar to analyse
     * @return the attributes of the command in the module, or null if unavailable
     * @throws ClassNotFoundException jar layout is invalid
     * @throws IOException unable to access jar
     * @throws ConflictException unable to resolve pact conflicts
     */
    private static ModuleAttributes loadModule(String jarPath, URLClassLoader cl) throws ClassNotFoundException, IOException, ConflictException
    {
        JarFile jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> entries = jarFile.entries();
        ModuleAttributes ma = loadClasses(jarFile, cl, entries);
        if (ma == null)
        {
            if (onLoadClasses.get(jarFile) == null)
            {
                warnings++;
                logger.warn(formatJarName(jarFile) + " has no pact or load class. It cannot directly communicate with Jara.");
            }
            return null; //Allows for MA dependant operations below this point.
        }
        else if (ma.getCommandClass() == null && ma.getLoadClass() == null && ma.getConfigClass() == null)
        {
            warnings++;
            logger.warn(formatJarName(jarFile)+" is a useless module. It has a pact, but no functionality.");
        }
        else if (ma.getCommandClass() != null && ma.getHelpPage().equals(new Help.HelpPage()))
        {
            warnings++;
            logger.warn(formatJarName(jarFile)+" has a command, but does not provide a help page.");
        }
        else if (!Core.getSupportedVersions().contains(ma.getTargetVersion()))
        {
            warnings++;
            logger.warn(ma.getKey()+" is built for unsupported Jara version: "+ma.getTargetVersion()+". It may not function as intended, please update for full support with Jara "+Core.getVersion()+".");
        }
        else
        {
            //logger.info("Successfully loaded "+ formatJarName(jarFile));
        }
        ModuleResourceLoader.registerModule(ma.getKey(), jarPath);
        return ma;
    }

    /**
     * Gets the jar name for pretty printing
     * @param jarFile the jar to get a pretty name for
     * @return the pretty name
     */
    @NotNull
    private static String formatJarName(JarFile jarFile)
    {
        return jarFile.getName().substring(jarFile.getName().lastIndexOf("\\")+1).replace(".jar", "");
    }

    /**
     * Loads a class from the specified module setup
     * @param jarFile the module's jar
     * @param cl the classloader
     * @return potentially modified CommandAttributes
     * @throws ClassNotFoundException invalid class directory
     * @throws IOException unable to access module file
     * @throws ConflictException classpath is taken
     */
    private static ModuleAttributes loadClasses(JarFile jarFile, URLClassLoader cl, Enumeration<JarEntry> jarEntries) throws ClassNotFoundException, IOException, ConflictException
    {
        JarEntry jarPact = jarFile.getJarEntry("pact.json");
        ModuleAttributes ma = getAttributesInPact(jarFile, jarPact);
        loadHelpPage(ma, jarFile);
        while (jarEntries.hasMoreElements())
        {
            JarEntry jarEntry = jarEntries.nextElement();
            if (jarEntry.getName().endsWith(".class"))
            {
                loadClass(jarFile, cl, ma, jarEntry);
            }
            //TODO: It's possible to load in files from a module's resources using Java 7's FileSystem, however, this will require an external process.
        }
        return ma;
    }

    /**
     * Loads a class, and assigns Jara communication classes in the supplied {@link ModuleAttributes}
     * @param jarFile the jar to load from
     * @param cl the loader
     * @param ma the module to attribute classes to
     * @param jarEntry the class file
     * @throws ClassNotFoundException invalid class directory supplied
     */
    private static void loadClass(JarFile jarFile, URLClassLoader cl, ModuleAttributes ma, JarEntry jarEntry) throws ClassNotFoundException
    {
        String className = jarEntry.getName().substring(0, jarEntry.getName().length() - 6);
        className = className.replace("/", ".");
        Class c = cl.loadClass(className);

        if (ma != null)
        {
            if (ModuleCommand.class.isAssignableFrom(c))
            {
                ma.setCommandClass(c);
            }
            if (ModuleConfig.class.isAssignableFrom(c))
            {
                ma.setConfigClass(c);
            }
        }
        if (ModuleLoad.class.isAssignableFrom(c))
        {
            onLoadClasses.put(jarFile, c);
            if (ma != null)
            {
                ma.setLoadClass(c);
            }
        }
    }

    /**
     * Sets the help page in the {@link ModuleAttributes} for the module.
     * @param ma the module's attributes
     * @param jarFile the file to get help from
     * @throws IOException file inaccessible
     */
    private static void loadHelpPage(ModuleAttributes ma, JarFile jarFile) throws IOException
    {
        JarEntry jarHelp = jarFile.getJarEntry("help.json");
        if (jarHelp != null && ma != null)
        {
            Gson gson = new Gson();
            ma.setHelpPage(gson.fromJson(getJson(jarFile, jarHelp), Help.HelpPage.class));
        }
    }

    /**
     * Gets the non-conflicting attributes defined in the pact file and converts them to {@link ModuleAttributes}.
     * @param jarFile the jar of the module
     * @param jarPact the pact
     * @return the {@link ModuleAttributes} defined in the pact, where the class will be null.
     * @throws IOException unable to access pact
     * @throws ConflictException unable to resolve conflicts
     */
    private static ModuleAttributes getAttributesInPact(JarFile jarFile, JarEntry jarPact) throws IOException, ConflictException
    {
        if (jarPact != null)
        {
            String json = getJson(jarFile, jarPact);
            ModuleAttributes ma = new ModuleAttributes(json);

            if (ma.getCategory() == ModuleManager.Category.SEASONAL)
                ma = new SeasonalModuleAttributes(json);

            return resolveConflicts(jarFile, ma);
        }
        else
        {
            return null;
        }
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
     * @param moduleAttributes the command attributes in the pact
     * @return the resolved command attributes with a null class.
     * @throws ConflictException unable to resolve conflicts
     */
    private static ModuleAttributes resolveConflicts(JarFile jarFile, ModuleAttributes moduleAttributes) throws ConflictException
    {
        if (!Collections.disjoint(reservedAliases, Arrays.asList(moduleAttributes.getAliases()))) //Fucking arrays.
        {
            if (reservedAliases.contains(moduleAttributes.getKey()))
            {
                throw new ConflictException(formatJarName(jarFile)+" has a conflicting key: "+moduleAttributes.getKey()+". It cannot be used.");
            }
            else    //TODO: One possible idea here is to remove the aliases from the modules that will end up with the highest quantity. However, this is an expensive operation as there may be many modules with conflicting aliases, which need to be searched for.
            {
                logger.info(formatJarName(jarFile)+" has overlapping aliases in the pact:");
                ArrayList<String> aliases = new ArrayList<>();
                for (String alias : moduleAttributes.getAliases())
                {
                    if (!moduleAttributes.getKey().equalsIgnoreCase(alias))
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
                }
                logger.info("These will be ignored from "+formatJarName(jarFile));
                moduleAttributes = new ModuleAttributes(moduleAttributes.getKey(), moduleAttributes.getDescription(), aliases.toArray(new String[0]), moduleAttributes.getCategory(), moduleAttributes.getTargetVersion(), true, false);
            }
        }
        return moduleAttributes;
    }
}
