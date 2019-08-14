package configuration;
import jara.CommandRegister;
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
    protected HashMap<String, Boolean> commandConfig;

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
        if (!commandConfig.keySet().containsAll(Arrays.asList(CommandRegister.getAllCommandKeys())))
        {
            if (!commandConfig.keySet().containsAll(Arrays.asList(CommandRegister.getAllCommandKeys())))
            {
                for (String key : CommandRegister.getAllCommandKeys())
                {
                    if (!commandConfig.keySet().contains(key))
                    {
                        commandConfig.put(key, !CommandRegister.getCommand(key).isDisableable());
                    }
                }
                logger.info("Commands were missing in the config, so have been added with disabled state.");
            }
        }

        File configFile = new File(getGlobalSettingsFilePath());
        if (!configFile.exists())
        {
            configFile.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(getGlobalSettingsFilePath());
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
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
                GlobalSettings gs = (GlobalSettings) ois.readObject();
                this.token = gs.token;
                this.commandConfig = gs.commandConfig;
                ois.close();
                fis.close();
                if (!commandConfig.keySet().containsAll(Arrays.asList(CommandRegister.getAllCommandKeys())))
                {
                    logger.info("Found new commands. Adding them to the config.");
                    for (String key : CommandRegister.getAllCommandKeys())
                    {
                        if (!commandConfig.keySet().contains(key))
                        {
                            commandConfig.put(key, true);
                        }
                    }
                    save();
                }
                return true;
            }
            else
            {
                this.token = "";
                this.commandConfig = new HashMap<>();
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

    public HashMap<String, Boolean> getCommandConfigMap()
    {
        return commandConfig;
    }

    public void setCommandConfigMap(HashMap<String, Boolean> commandConfig) throws IOException
    {
        this.commandConfig = commandConfig;
        save();
    }
    /**
     * Updates the stored information for the commands defined by their keys.
     * @param newState
     * @param commandKeys
     */
    public void setCommandConfiguration(boolean newState, String... commandKeys) throws IOException
    {
        boolean changed = false;
        for (String key : commandKeys)
        {
            if (CommandRegister.getCommand(key).isDisableable())
            {
                commandConfig.replace(key, newState);
                changed = true;
            }
        }
        if (changed)
            save();
    }

    /**
     * @param commandKey
     * @return
     */
    public boolean isCommandEnabled(String commandKey)
    {
        return commandConfig.get(commandKey);
    }
}
