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

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GuildSettings extends GuildSettingsJson
{
    /**
     * The ID for the guild these settings belong to.
     */
    private final String guildId;

    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(GuildSettings.class);

    /**
     * Constructor
     * @param guildId
     */
    public GuildSettings(String guildId)
    {
        this.guildId = guildId;
    }

    //=======================================================  Methods ==========================================================

    /**
     * Builds guild settings JSON
     * @return
     * String -  Guild Settings in a JSON format
     */
    public String getJSON()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    /**
     * Saves the guild settings to file.<br>
     * For this to go through, token and commandConfig must not be null.
     * @throws
     * IOException - Error accessing the file
     * @throws
     * NullPointerException - Missing required data.
     */
    public void save() throws NullPointerException, IOException
    {
        if (this.guildId == null || this.commandConfig == null)
        {
            logger.error("Cannot save, a required element is null.");
            throw new NullPointerException();
        }
        if (this.commandConfig.size() < CommandRegister.getRegisterSize())
        {
            logger.error("Cannot save, commands are missing in the config.");
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

    /**
     * Loads the guild settings from file.
     * @throws IOException - Unable to access file
     * @throws NullPointerException - Missing data
     */
    public void restore() throws IOException, NullPointerException
    {
        String JSON = new String(Files.readAllBytes(SettingsUtil.getGuildSettingsFile(guildId).toPath()));
        if (JSON.length() > 0)
        {
            Gson gson = new Gson();
            GuildSettingsJson settingsFromFile = gson.fromJson(JSON, getClass());

            this.audioConfig.skipVotePercent = settingsFromFile.audioConfig.skipVotePercent;
            this.gameConfig.useGameChannels = settingsFromFile.gameConfig.useGameChannels;
            this.gameConfig.gameCategoryId = settingsFromFile.gameConfig.gameCategoryId;
            this.gameConfig.gameChannelTimeout = settingsFromFile.gameConfig.gameChannelTimeout;
            this.commandConfig = settingsFromFile.commandConfig;
        }
        else
        {
            logger.error("Guild settings are empty for "+guildId);
            throw new NullPointerException(); //There is no data
        }
    }

    /**
     * Applies default settings to the guild config<br>
     * As with other setter methods, this will not save to file.<br>
     */
    public void setDefaultSettings()
    {
        this.commandConfig = new HashMap<>();
        this.audioConfig = new AudioConfig();
        this.gameConfig = new GameConfig();

        for (CommandAttributes ca : CommandRegister.getRegister())
        {
            this.commandConfig.put(ca.getCommandKey(), new GuildSettingsJson.CommandConfig(!ca.isDisableable(), new ArrayList<>())); //By inverting isDisableable, we are disabling the command whenever isDisablable is true.
        }

        setUseGameChannels(false);
        setTrackSkipPercent(50);
        setGameChannelTimeout("0");
        setGameCategoryId("");
    }

    /**
     * Allows the role represented by the ID to use the command.
     * @param roleIDs
     * @param commandKeys
     */
    public void addPermissions(ArrayList<String> roleIDs, String... commandKeys)
    {
        setPermissions(true, roleIDs, commandKeys);
    }

    /**
     * Denies the role represented by the ID from using the command.
     * @param roleIDs
     * @param commandKeys
     */
    public void removePermissions(ArrayList<String> roleIDs, String... commandKeys)
    {
        setPermissions(false, roleIDs, commandKeys);
    }

    //=================================================== Getters & Setters ======================================================

    /**
     * @return
     */
    public String getGuildId()
    {
        return guildId;
    }

    /**
     * @return
     */
    private HashMap<String, GuildSettingsJson.CommandConfig> getCommandMap()
    {
        HashMap<String, CommandConfig> clone = new HashMap<>(this.commandConfig);
        return clone;
    }

    /**
     * @param guildCommandConfig
     */
    private void setCommandMap(HashMap<String, CommandConfig> guildCommandConfig)
    {
        this.commandConfig = guildCommandConfig;
    }

    /**
     * Checks if the member is allowed to use this command, based on their roles.
     * @param member
     * @param commandKey
     * @return
     */
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
        if (!Collections.disjoint(this.commandConfig.get(commandKey).permissions, roleIDs))
        {
            permissionGranted = true;
        }
        return permissionGranted;
    }
    /**
     * Checks if the member is allowed to use this command, based on their roles.
     * @param member
     * @param command
     * @return
     */
    public boolean isPermitted(Member member, Class<? extends Command> command)
    {
        return isPermitted(member, CommandRegister.getCommand(command).getCommandKey());
    }

    /**
     * Returns a list of role IDs who can use this command
     * @param key
     * @return ArrayList<String> - List of Role IDs
     */
    public ArrayList<String> getPermissions(String key)
    {
        return this.commandConfig.get(key).permissions;
    }

    /**
     * Modifies the permissions for the specified commands by adding/removing the roles in the list.
     * @param add whether to add/remove permissions
     * @param roleIDs the roles to permit
     * @param commandKeys the commands to affect
     */
    private void setPermissions(boolean add, ArrayList<String> roleIDs, String... commandKeys)
    {
        for (String key : commandKeys)
        {
            boolean state = this.commandConfig.get(key).enabled;
            ArrayList<String> permissions = this.commandConfig.get(key).permissions;
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
            this.commandConfig.replace(key, new GuildSettingsJson.CommandConfig(state, permissions));
        }
    }

    /**
     * @return
     */
    public String getGameCategoryId()
    {
        return this.gameConfig.gameCategoryId;
    }

    /**
     * @param gameCategoryId
     */
    public void setGameCategoryId(String gameCategoryId)
    {
        this.gameConfig.gameCategoryId = gameCategoryId;
    }

    /**
     * Updates the stored information for the commands defined by their keys. Pass null parameter to retain current value.
     * @param newState (Can be null)
     * @param newPermissions (Can be null)
     * @param commandKeys
     */
    public void setCommandConfiguration(Boolean newState, ArrayList<String> newPermissions, String... commandKeys)
    {
        Boolean state;
        ArrayList<String> permissions;
        for (String key : commandKeys)
        {
            if (CommandRegister.getCommand(key).isDisableable())
            {
                if (newState == null)
                {
                    state = this.commandConfig.get(key).enabled;
                }
                else
                {
                    state = newState;
                }
                if (newPermissions == null)
                {
                    permissions = this.commandConfig.get(key).permissions;
                }
                else
                {
                    permissions = newPermissions;
                }
                this.commandConfig.replace(key, new GuildSettingsJson.CommandConfig(state, permissions));
            }
        }
    }

    /**
     * Updates the stored information for the commands within the category. Pass null parameter to retain current value.
     * @param newState
     * @param newPermissions
     * @param categoryID
     */
    public void setCategoryConfiguration(Boolean newState, ArrayList<String> newPermissions, int categoryID)
    {
        ArrayList<String> keys = new ArrayList<>();
        for (CommandAttributes ca : CommandRegister.getCommandsInCategory(categoryID))
        {
            keys.add(ca.getCommandKey());
        }
        setCommandConfiguration(newState, newPermissions, keys.toArray(new String[0]));
    }


    /**
     * @param commandKey
     * @return
     */
    public boolean isCommandEnabled(String commandKey)
    {
        return this.commandConfig.get(commandKey).enabled;
    }

    /**
     * Gets the command keys of all commands enabled in this guild.
     * @return
     */
    public ArrayList<String> getEnabledCommands()
    {
        ArrayList<String> enabledCommands = new ArrayList<>();
        for (String key : CommandRegister.getAllCommandKeys())
        {
            if (this.commandConfig.get(key).enabled)
            {
                enabledCommands.add(key);
            }
        }
        return enabledCommands;
    }


    /**
     * @return
     */
    public String getGameChannelTimeout()
    {
        return this.gameConfig.gameChannelTimeout;
    }

    /**
     * @param gameChannelTimeout
     */
    public void setGameChannelTimeout(String gameChannelTimeout)
    {
        this.gameConfig.gameChannelTimeout = gameChannelTimeout;
    }

    /**
     * @return
     */
    public boolean isGameChannelsEnabled()
    {
        return this.gameConfig.useGameChannels;
    }

    /**
     * @param
     */
    public void setUseGameChannels(boolean useGameChannels)
    {
        this.gameConfig.useGameChannels = useGameChannels;
    }

    /**
     * @return
     */
    public int getTrackSkipPercent()
    {
        return this.audioConfig.skipVotePercent;
    }

    /**
     * @param newPercent
     */
    public void setTrackSkipPercent(int newPercent)
    {
        if (newPercent >= 0 && newPercent <= 100)
        {
            this.audioConfig.skipVotePercent = newPercent;
        }
    }

}
