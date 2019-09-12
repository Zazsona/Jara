package configuration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jara.ModuleRegister;
import jara.ModuleAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
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
     * Returns the file where global settings are stored.
     * @return
     * File - Global settings file
     */
    private String getGlobalSettingsFilePath()
    {
        return (SettingsUtil.getDirectory().getAbsolutePath()+"/Settings.jara");
    }

    public synchronized void save() throws IOException
    {
        if (!moduleConfig.keySet().containsAll(Arrays.asList(ModuleRegister.getCommandModuleKeys())))
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
                if (!moduleConfig.keySet().containsAll(Arrays.asList(ModuleRegister.getCommandModuleKeys())))
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

    public String getToken()
    {
        return token;
    }

    /**
     * @param token
     */
    public void setToken(String token) throws IOException
    {
        this.token = token;
        save();
    }

    public HashMap<String, Boolean> getModuleConfigMap()
    {
        return moduleConfig;
    }

    public void setModuleConfigMap(HashMap<String, Boolean> commandConfig) throws IOException
    {
        this.moduleConfig = commandConfig;
        save();
    }
    /**
     * Updates the stored information for the modules defined by their keys.
     * @param newState
     * @param keys
     */
    public void setModuleConfiguration(boolean newState, String... keys) throws IOException
    {
        boolean changed = false;
        for (String key : keys)
        {
            if (ModuleRegister.getModule(key).isDisableable())
            {
                moduleConfig.replace(key, newState);
                changed = true;
            }
        }
        if (changed)
            save();
    }

    /**
     * @param key
     * @return
     */
    public boolean isModuleEnabled(String key)
    {
        return moduleConfig.get(key);
    }
}
