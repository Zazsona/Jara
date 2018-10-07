package configuration;


import com.google.gson.*;
import commands.Command;
import jara.CommandAttributes;
import jara.CommandRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class GlobalSettings extends GlobalSettingsJson
{
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(GlobalSettings.class);

    //=======================================================  Methods ==========================================================
    /**
     * Builds global settings JSON
     * @return
     * String -  Global Settings in a JSON format
     */
    public String getJSON()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    /**
     * Saves the global settings to file.<br>
     * For this to go through, token and commandConfig must not be null.
     * @throws
     * java.io.IOException - Error accessing the file
     * @throws
     * java.lang.NullPointerException - Missing required data.
     */
    public void save() throws NullPointerException, IOException
    {
        if (token == null || commandConfig == null)
        {
            logger.error("Cannot save, a required element is null.");
            throw new NullPointerException();
        }
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

        File settingsFile = SettingsUtil.getGlobalSettingsFile();
        if (!settingsFile.exists())
        {
            settingsFile.createNewFile();
        }
        PrintWriter printWriter = new PrintWriter(new FileOutputStream(settingsFile, false));
        printWriter.print(getJSON());
        printWriter.close();
    }

    /**
     * Loads the guild settings from file.
     * @throws IOException - Unable to access file
     * @throws NullPointerException - Missing data
     */
    public void restore() throws IOException, NullPointerException
    {
        String JSON = new String(Files.readAllBytes(SettingsUtil.getGlobalSettingsFile().toPath()));
        if (JSON.length() > 0)
        {
            Gson gson = new Gson();
            GlobalSettings settingsFromFile = gson.fromJson(JSON, GlobalSettings.class);

            this.token = settingsFromFile.getToken();
            this.commandConfig = settingsFromFile.getCommandConfigMap();
            if (!commandConfig.keySet().containsAll(Arrays.asList(CommandRegister.getAllCommandKeys())))
            {
                /*
                    We are not filling in the commands here because, unlike the guild side, the host has CHOSEN to update the bot.
                    Therefore, I feel it would be better to open the setup and let them configure it.
                 */
                logger.error("Commands are missing from the config. Is it outdated?");
                throw new NullPointerException();
                //TODO: Launch update (/new mod) setup
            }
        }
        else
        {
            logger.error("Global settings is empty.");
            throw new NullPointerException(); //There is no data
        }
    }


    //=================================================== Getters & Setters =====================================================
    /**
     * @return
     */
    public String getToken()
    {
        return token;
    }

    /**
     * @param token
     */
    public void setToken(String token)
    {
        this.token = token;
    }

    /**
     * @return
     */
    public HashMap<String, Boolean> getCommandConfigMap()
    {
        if (commandConfig == null)
        {
            return null;
        }
        HashMap<String, Boolean> clone = new HashMap<>(commandConfig);
        return clone;
    }
    public void setCommandConfigMap(HashMap<String, Boolean> commandConfig)
    {
        this.commandConfig = commandConfig;
    }
    /**
     * Updates the stored information for the commands defined by their keys.
     * @param newState
     * @param commandKeys
     */
    public void setCommandConfiguration(boolean newState, String... commandKeys)
    {
        for (String key : commandKeys)
        {
            if (CommandRegister.getCommand(key).isDisableable())
            {
                commandConfig.replace(key, newState);
            }
        }
    }
    /**
     * Updates the stored information for the commands within the category. Pass null parameter to retain current value.
     * @param newState
     * @param categoryID
     */
    public void setCategoryConfiguration(boolean newState, int categoryID)
    {
        ArrayList<String> keys = new ArrayList<>();
        for (CommandAttributes ca : CommandRegister.getCommandsInCategory(categoryID))
        {
            keys.add(ca.getCommandKey());
        }
        setCommandConfiguration(newState, keys.toArray(new String[0]));
    }

    /**
     * @param commandKey
     * @return
     */
    public boolean isCommandEnabled(String commandKey)
    {
        return commandConfig.get(commandKey);
    }

    /**
     * Gets the command keys of all commands enabled on this bot.
     * @return
     */
    public ArrayList<String> getEnabledCommands()
    {
        ArrayList<String> enabledCommands = new ArrayList<>();
        for (String key : CommandRegister.getAllCommandKeys())
        {
            if (commandConfig.get(key))
            {
                enabledCommands.add(key);
            }
        }
        return enabledCommands;
    }
}
