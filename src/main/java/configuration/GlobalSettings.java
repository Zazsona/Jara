package configuration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jara.ModuleManager;
import jara.ModuleAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;

public class GlobalSettings implements Serializable
{
    private static final long serialVersionUID = 1L;
    /**
     * The logger.
     */
    private static transient final Logger logger = LoggerFactory.getLogger(GlobalSettings.class);
    /**
     * The token used for logging into Discord.
     */
    protected String token;
    /**
     * The list of configured commands, and their enabled status.
     */
    protected HashMap<String, Boolean> moduleConfig;

    /**
     * Returns the filepath where global settings are stored.
     * @return the filepath
     */
    private String getGlobalSettingsFilePath()
    {
        return (SettingsUtil.getDirectory().getAbsolutePath()+"/Settings.jara");
    }

    /**
     * Saves global settings
     * @throws IOException unable to access file
     */
    public synchronized void save() throws IOException
    {
        addMissingModules();
        File configFile = new File(getGlobalSettingsFilePath());
        if (!configFile.exists())
        {
            configFile.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(getGlobalSettingsFilePath(), false);
        PrintWriter printWriter = new PrintWriter(fos);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        printWriter.print(gson.toJson(this));
        printWriter.close();
        fos.close();
    }

    /**
     * Loads in global settings from file
     * @return true on restoration, false on reset/error
     * @throws IOException unable to read file
     */
    public synchronized boolean restore() throws IOException
    {
        if (new File(getGlobalSettingsFilePath()).exists())
        {
            String json = new String(Files.readAllBytes(new File(getGlobalSettingsFilePath()).toPath()));
            Gson gson = new Gson();
            GlobalSettings gs = gson.fromJson(json, GlobalSettings.class);
            this.token = gs.token;
            this.moduleConfig = gs.moduleConfig;
            boolean added = addMissingModules();
            if (added)
                save();
            return true;
        }
        else
        {
            this.token = "";
            this.moduleConfig = new HashMap<>();
            return false;
        }
    }

    private boolean addMissingModules()
    {
        boolean added = false;
        if (!moduleConfig.keySet().containsAll(ModuleManager.getCommandModuleKeys()))
        {
            added = true;
            logger.info("Found new modules. Adding them to the config.");
            for (String key : ModuleManager.getModuleKeys())
            {
                if (!moduleConfig.keySet().contains(key))
                {
                    moduleConfig.put(key, true);
                }
            }
        }
        return added;
    }

    /**
     * Gets the Discord API token
     * @return the token
     */
    public String getToken()
    {
        return token;
    }

    /**
     * Sets the Discord API token
     * @param token the token
     * @throws IOException unable to write to file
     */
    public void setToken(String token) throws IOException
    {
        this.token = token;
        save();
    }

    /**
     * Gets the enabled status of modules
     * @return map of module key to enabled state
     */
    public HashMap<String, Boolean> getModuleConfigMap()
    {
        return moduleConfig;
    }

    /**
     * Sets the module config map
     * @param commandConfig map of module key to enabled state
     * @throws IOException unable to save
     */
    public void setModuleConfigMap(HashMap<String, Boolean> commandConfig) throws IOException
    {
        this.moduleConfig = commandConfig;
        save();
    }
    /**
     * Updates the stored information for the modules defined by their keys.
     * @param newState the new enabled state of the modules
     * @param keys the modules to update
     * @throws IOException unable to write to file
     */
    public void setModuleConfiguration(boolean newState, String... keys) throws IOException
    {
        boolean changed = false;
        for (String key : keys)
        {
            ModuleAttributes ma = ModuleManager.getModule(key);
            if (ma != null && ma.isDisableable())
            {
                moduleConfig.replace(key, newState);
                changed = true;
            }
        }
        if (changed)
            save();
    }

    /**
     * Checks if a module is enabled globally
     * @param key the module to check's key
     * @return boolean on enabled
     */
    public boolean isModuleEnabled(String key)
    {
        return moduleConfig.get(key);
    }

    /**
     * Sets all modules to a single common state, where possible.
     * @throws IOException
     */
    public void setAll(boolean enable) throws IOException
    {
        HashMap<String, Boolean> newCommandConfig = new HashMap<>();
        for (ModuleAttributes ma : ModuleManager.getModules())
        {
            if (enable || (!enable && ma.isDisableable()))
                newCommandConfig.put(ma.getKey(), enable);
        }
        setModuleConfigMap(newCommandConfig);
    }
}
