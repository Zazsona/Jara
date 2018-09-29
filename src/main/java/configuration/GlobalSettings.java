package configuration;


import com.google.gson.*;
import jara.CommandAttributes;
import jara.CommandRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

public class GlobalSettings
{
    private String token;
    private HashMap<String, Boolean> commandConfig;

    public void setToken(String token)
    {
        this.token = token;
    }
    public String getToken()
    {
        return token;
    }
    public void setCommandConfig(HashMap<String, Boolean> commandConfig)
    {
        this.commandConfig = commandConfig;
    }
    public HashMap<String, Boolean> getCommandConfig()
    {
        if (commandConfig == null)
        {
            return null;
        }
        HashMap<String, Boolean> clone = new HashMap<>();
        clone.putAll(commandConfig);
        return clone;
    }

    /**
     * Builds global settings JSON
     * @return
     * String -  Global Settings in a JSON format
     */
    public String getJSON()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String globalSettingsJson = gson.toJson(this);
        return globalSettingsJson;
    }

    /**
     * Saves the global settings to file.
     *
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
            throw new NullPointerException();
        }
        if (commandConfig.size() < CommandRegister.getRegisterSize())
        {
            throw new NullPointerException();
        }
        //TODO: Make it so any unset (missing) commands are disabled?

        File settingsFile = SettingsUtil.getGlobalSettingsFile();
        if (!settingsFile.exists())
        {
            settingsFile.createNewFile();
        }
        PrintWriter printWriter = new PrintWriter(new FileOutputStream(settingsFile, false));
        printWriter.print(getJSON());
        printWriter.close();
    }
    public void restore() throws IOException, NullPointerException
    {
        String JSON = new String(Files.readAllBytes(SettingsUtil.getGlobalSettingsFile().toPath()));
        if (JSON.length() > 0)
        {
            Gson gson = new Gson();
            GlobalSettings settingsFromFile = gson.fromJson(JSON, GlobalSettings.class);

            this.token = settingsFromFile.getToken();
            this.commandConfig = settingsFromFile.getCommandConfig();
        }
        else
        {
            throw new NullPointerException(); //There is no data
        }
    }
    public void modifyCommandConfiguration(boolean newState, String... commandKeys)
    {
        for (String key : commandKeys)
        {
            if (CommandRegister.getCommand(key).isDisableable())
            {
                commandConfig.replace(key, newState);
            }
        }
    }
    public void modifyCategoryConfiguration(boolean newState, int categoryID)
    {
        ArrayList<String> keys = new ArrayList<>();
        for (CommandAttributes ca : CommandRegister.getCommandsInCategory(categoryID))
        {
            keys.add(ca.getCommandKey());
        }
        modifyCommandConfiguration(newState, keys.toArray(new String[keys.size()]));
    }
    public boolean isCommandEnabled(String commandKey)
    {
        return commandConfig.get(commandKey);
    }
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
