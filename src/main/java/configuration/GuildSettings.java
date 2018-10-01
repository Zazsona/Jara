package configuration;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import commands.Command;
import jara.CommandAttributes;
import jara.CommandRegister;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GuildSettings
{
    private String guildId;
    private String gameCategoryId;
    private String gameChannelTimeout;
    private HashMap<String, Config> guildCommandConfig;

    public GuildSettings(String guildId)
    {
        this.guildId = guildId;
    }

    public String getGuildId()
    {
        return guildId;
    }
    public void setGuildCommandConfig(HashMap<String, Config> guildCommandConfig)
    {
        this.guildCommandConfig = guildCommandConfig;
    }
    public HashMap<String, Config> getGuildCommandConfig()
    {
        HashMap<String, Config> clone = new HashMap<>();
        clone.putAll(guildCommandConfig);
        return clone;
    }
    public ArrayList<String> getPermissions(String key)
    {
        return guildCommandConfig.get(key).permissions;
    }
    public void setPermissions(String key, ArrayList<String> permissions)
    {
        guildCommandConfig.get(key).permissions = permissions;
    }
    public String getGameCategoryId()
    {
        return gameCategoryId;
    }
    public void setGameCategoryId(String gameCategoryId)
    {
        this.gameCategoryId = gameCategoryId;
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
        System.out.println(globalSettingsJson);
        return globalSettingsJson;
    }

    /**
     * Saves the global settings to file.
     *
     * For this to go through, token and commandConfig must not be null.
     * @throws
     * IOException - Error accessing the file
     * @throws
     * NullPointerException - Missing required data.
     */
    public void save() throws NullPointerException, IOException
    {
        if (guildId == null || guildCommandConfig == null)
        {
            throw new NullPointerException();
        }
        if (guildCommandConfig.size() < CommandRegister.getRegisterSize())
        {
            throw new NullPointerException();
        }
        //TODO: Make it so any unset (missing) commands are disabled?

        File settingsFile = SettingsUtil.getGuildSettingsFile(guildId);
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
        String JSON = new String(Files.readAllBytes(SettingsUtil.getGuildSettingsFile(guildId).toPath()));
        if (JSON.length() > 0)
        {
            Gson gson = new Gson();
            GuildSettings settingsFromFile = gson.fromJson(JSON, GuildSettings.class);

            this.gameCategoryId = settingsFromFile.getGameCategoryId();
            this.gameChannelTimeout = settingsFromFile.getGameChannelTimeout();
            this.guildCommandConfig = settingsFromFile.getGuildCommandConfig();
        }
        else
        {
            throw new NullPointerException(); //There is no data
        }
    }
    public void generateDefaultCommandConfig()
    {
        guildCommandConfig = new HashMap<>();
        for (CommandAttributes ca : CommandRegister.getRegister())
        {
            guildCommandConfig.put(ca.getCommandKey(), new Config(!ca.isDisableable(), new ArrayList<>())); //By inverting isDisableable, we are disabling it when isDisablable is true.
        }
    }
    /**
     * Updates the stored information for the command defined by the key. Pass null parameter to retain current value.
     *
     * @param newState (Can be null)
     * @param newPermissions (Can be null)
     * @param commandKeys
     */
    public void modifyCommandConfiguration(Boolean newState, ArrayList<String> newPermissions, String... commandKeys)
    {
        Boolean state;
        ArrayList<String> permissions;
        for (String key : commandKeys)
        {
            if (CommandRegister.getCommand(key).isDisableable())
            {
                if (newState == null)
                {
                    state = guildCommandConfig.get(key).enabled;
                }
                else
                {
                    state = newState;
                }
                if (newPermissions == null)
                {
                    permissions = guildCommandConfig.get(key).permissions;
                }
                else
                {
                    permissions = newPermissions;
                }
                guildCommandConfig.replace(key, new Config(newState, permissions));
            }
        }
    }
    public void modifyCategoryConfiguration(Boolean newState, ArrayList<String> newPermissions, int categoryID)
    {
        ArrayList<String> keys = new ArrayList<>();
        for (CommandAttributes ca : CommandRegister.getCommandsInCategory(categoryID))
        {
            keys.add(ca.getCommandKey());
        }
        modifyCommandConfiguration(newState, newPermissions, keys.toArray(new String[keys.size()]));
    }
    public void addPermissions(ArrayList<String> roleIDs, String... commandKeys)
    {
        modifyPermissions(true, roleIDs, commandKeys);
    }
    public void removePermissions(ArrayList<String> roleIDs, String... commandKeys)
    {
        modifyPermissions(false, roleIDs, commandKeys);
    }
    private void modifyPermissions(boolean add, ArrayList<String> roleIDs, String... commandKeys)
    {
        for (String key : commandKeys)
        {
            boolean state = guildCommandConfig.get(key).enabled;
            ArrayList<String> permissions = guildCommandConfig.get(key).permissions;
            for (String roleID : roleIDs)
            {
                if (add) //If add it true, add the roles.
                {
                    permissions.add(roleID);
                }
                else //If add is false, remove.
                {
                    permissions.remove(roleID);
                }
            }
            guildCommandConfig.replace(key, new Config(state, permissions));
        }
    }
    public boolean isCommandEnabled(String commandKey)
    {
        return guildCommandConfig.get(commandKey).enabled;
    }
    public ArrayList<String> getEnabledCommands()
    {
        ArrayList<String> enabledCommands = new ArrayList<>();
        for (String key : CommandRegister.getAllCommandKeys())
        {
            if (guildCommandConfig.get(key).enabled)
            {
                enabledCommands.add(key);
            }
        }
        return enabledCommands;
    }
    public boolean isPermitted(Member member, String commandKey)
    {
        if (member.isOwner())
        {
            return true;
        }
        boolean permissionGranted = false;
        ArrayList<String> roleIDs = new ArrayList<>();
        for (Role role : member.getRoles())
        {
            roleIDs.add(role.getId());
        }
        if (!Collections.disjoint(guildCommandConfig.get(commandKey).permissions, roleIDs))
        {
            permissionGranted = true;
        }
        return permissionGranted;
    }
    public boolean isPermitted(Member member, Class<? extends Command> command)
    {
        return isPermitted(member, CommandRegister.getCommand(command).getCommandKey());
    }

    public String getGameChannelTimeout()
    {
        return gameChannelTimeout;
    }

    public void setGameChannelTimeout(String gameChannelTimeout)
    {
        this.gameChannelTimeout = gameChannelTimeout;
    }

    private class Config
    {
        public Config(boolean enabled, ArrayList<String> permissions)
        {
            this.enabled = enabled;
            this.permissions = permissions;
        }
        public ArrayList<String> permissions;
        public boolean enabled;
    }
}