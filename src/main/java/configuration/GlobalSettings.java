package configuration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jara.ModuleRegister;
import jara.ModuleAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
        if (!moduleConfig.keySet().containsAll(ModuleRegister.getCommandModuleKeys()))
        {
            for (ModuleAttributes ma : ModuleRegister.getModules())
            {
                if (!moduleConfig.keySet().contains(ma.getKey()))
                {
                    moduleConfig.put(ma.getKey(), !ma.isDisableable());
                }
            }
            logger.info("Modules were missing in the config, so have been added with disabled state.");
        }

        File configFile = new File(getGlobalSettingsFilePath());
        if (!configFile.exists())
        {
            configFile.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(getGlobalSettingsFilePath());
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        oos.writeObject(gson.toJson(this));
        oos.close();
        fos.close();
    }

    /**
     * Loads in global settings from file
     * @return true on restoration, false on reset/error
     * @throws IOException unable to read file
     */
    public synchronized boolean restore() throws IOException
    {
        try
        {
            if (new File(getGlobalSettingsFilePath()).exists())
            {
                FileInputStream fis = new FileInputStream(getGlobalSettingsFilePath());
                ObjectInputStream ois = new ObjectInputStream(fis);
                Gson gson = new Gson();
                GlobalSettings gs = gson.fromJson((String) ois.readObject(), GlobalSettings.class);
                this.token = gs.token;
                this.moduleConfig = gs.moduleConfig;
                ois.close();
                fis.close();
                if (!moduleConfig.keySet().containsAll(ModuleRegister.getCommandModuleKeys()))
                {
                    logger.info("Found new commands. Adding them to the config.");
                    for (String key : ModuleRegister.getModuleKeys())
                    {
                        if (!moduleConfig.keySet().contains(key))
                        {
                            moduleConfig.put(key, true);
                        }
                    }
                    save();
                }
                return true;
            }
            else
            {
                this.token = "";
                this.moduleConfig = new HashMap<>();
                return false;
            }
        }
        catch (ClassNotFoundException e)
        {
            logger.error(e.toString());
            return false;
        }
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
            ModuleAttributes ma = ModuleRegister.getModule(key);
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
}
