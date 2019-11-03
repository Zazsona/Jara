package configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.MalformedJsonException;
import commands.CmdUtil;
import configuration.guild.AudioConfig;
import configuration.guild.CommandConfig;
import configuration.guild.GameConfig;
import jara.ModuleAttributes;
import jara.ModuleManager;
import jara.Core;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

public class GuildSettings implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * The Jara version this config was last accessed with.
     */
    private String jaraVersion;
    /**
     * The ID of this guild
     */
    private final String guildID;
    /**
     * The Character used to summon the bot
     */
    private char commandPrefix;
    /**
     * The Time Zone id for this guild.
     */
    private String timeZoneId;
    /**
     * The guild's audio settings.
     */
    protected AudioConfig audioConfig;
    /**
     * The guild's game settings.
     */
    protected GameConfig gameConfig;
    /**
     * The guild's command settings.
     */
    protected HashMap<String, CommandConfig> commandConfig;
    /**
     * The custom commands
     */
    protected CustomCommandSettings customCommandSettings;
    /**
     * The guild's settings, supplied by modules.
     */
    private HashMap<String, Object> moduleConfig;


    /**
     * The logger.
     */
    private static transient final Logger logger = LoggerFactory.getLogger(GuildSettings.class);

    /**
     * Constructor
     * @param guildID the id of the guild
     * @throws IOException unable to access file
     */
    public GuildSettings(String guildID) throws IOException
    {
        this.guildID = guildID;
        restore();
    }

    /**
     * Returns the directory which stores guild settings files.
     * @return the directory
     */
    private File getGuildSettingsDirectory()
    {
        File guildSettingsFolder;
        guildSettingsFolder = new File(SettingsUtil.getDirectory().getAbsolutePath()+"/Guilds/");
        if (!guildSettingsFolder.exists())
        {
            guildSettingsFolder.mkdirs();
        }
        return guildSettingsFolder;
    }

    /**
     * Gets the filepath for this guild's settings file
     * @param guildID the guild's id
     * @return the path
     */
    private String getGuildSettingsFilePath(String guildID)
    {
        return (getGuildSettingsDirectory().getPath()+"/"+guildID+".jara");
    }

    /**
     * Loads the guild settings from file.
     * @throws IOException - Unable to access file
     * @throws NullPointerException - Missing data
     */
    protected synchronized void restore() throws IOException
    {
        try
        {
            File guildFile = new File(getGuildSettingsFilePath(guildID));
            if (guildFile.exists())
            {
                String json = new String(Files.readAllBytes(guildFile.toPath())); //TODO: Not ideal as files can be of any size
                Gson gson = new Gson();
                GuildSettings settingsFromFile = gson.fromJson(json, GuildSettings.class);
                if (!Core.getVersion().equalsIgnoreCase(settingsFromFile.jaraVersion))
                {
                    updateConfig(settingsFromFile);
                    this.customCommandSettings.setGuildSettings(this);
                }
                else
                {
                    this.jaraVersion = Core.getVersion();
                    this.commandPrefix = settingsFromFile.commandPrefix;
                    this.timeZoneId = settingsFromFile.timeZoneId;
                    this.audioConfig = settingsFromFile.audioConfig;
                    this.gameConfig = settingsFromFile.gameConfig;
                    this.commandConfig = new HashMap<>(settingsFromFile.commandConfig);
                    this.moduleConfig = settingsFromFile.moduleConfig;
                    this.customCommandSettings = settingsFromFile.customCommandSettings;
                    this.customCommandSettings.setGuildSettings(this);
                }
                if (!commandConfig.keySet().containsAll(ModuleManager.getCommandModuleKeys()))
                {
                    ArrayList<String> newCommands = addMissingCommands();
                    StringBuilder sb = new StringBuilder();
                    for (String newCommand : newCommands)
                    {
                        sb.append(newCommand).append("\n");
                    }
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setDescription("The bot has been updated with new commands/settings added!\nUse config to configure these, they have been disabled for now.\n"+sb.toString());
                    embed.setColor(CmdUtil.getHighlightColour(null));
                    Core.getShardManagerNotNull().getGuildById(guildID).getOwner().getUser().openPrivateChannel().complete().sendMessage(embed.build()).queue(); //Yuck.
                    save();
                }
            }
            else
            {
                Guild guild = Core.getShardManager().getGuildById(guildID);
                logger.error("Creating new settings for "+guildID+" ("+guild.getName()+")...");
                setDefaultSettings();
                save();
            }
        }
        catch (StreamCorruptedException | MalformedJsonException e)
        {
            Guild guild = Core.getShardManager().getGuildById(guildID);
            guild.getOwner().getUser().openPrivateChannel().complete().sendMessage("The config for your guild, "+guild.getName()+", has become corrupted or is no longer available and has been reset.\nA backup of the corrupted config has been created. Please contact your host for further details.").queue();
            logger.error("Guild settings are corrupted for guild "+guildID+" ("+guild.getName()+"). Resetting.");
            Files.copy(new File(getGuildSettingsFilePath(guildID)).toPath(), new File(getGuildSettingsFilePath(guildID)+" - Corrupted.jara").toPath(), StandardCopyOption.REPLACE_EXISTING);
            setDefaultSettings();
            save();
        }

    }

    /**
     * Saves the guild settings to file.<br>
     * For this to go through, token and commandConfig must not be null.
     * @throws
     * IOException - Error accessing the file
     * @throws
     * NullPointerException - Missing required data.
     */
    protected synchronized void save() throws NullPointerException, IOException
    {
        if (this.guildID == null || this.commandConfig == null)
        {
            logger.error("Cannot save, a required element is null.");
            throw new NullPointerException();
        }

        File settingsFile = new File(getGuildSettingsFilePath(guildID));
        if (!settingsFile.exists())
        {
            settingsFile.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(getGuildSettingsFilePath(guildID));
        PrintWriter pw = new PrintWriter(fos);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        pw.print(gson.toJson(this));
        pw.close();
        fos.close();
    }

    /**
     * Deletes this guild's settings.
     * @throws IOException unable to save
     */
    public synchronized void delete() throws IOException
    {
        setDefaultSettings();
        new File(getGuildSettingsFilePath(guildID)).delete();
    }

    /**
     * Adds commands missing from the guild config, and informs the host.
     * @return the keys of commands that were missing.
     */
    private ArrayList<String> addMissingCommands()
    {
        ArrayList<String> commandModuleKeys = ModuleManager.getCommandModuleKeys();
        if (!commandConfig.keySet().containsAll(commandModuleKeys))
        {
            ArrayList<String> newKeys = new ArrayList<>();
            for (String key : commandModuleKeys)
            {
                if (!commandConfig.keySet().contains(key))
                {
                    commandConfig.put(key, new CommandConfig(!ModuleManager.getModule(key).isDisableable(), new ArrayList<>()));
                    newKeys.add(key);
                }
            }
            logger.info("Commands were missing in the config for guild "+guildID+", so have been added with disabled state."); //This could get really spammy after an update.
            return newKeys;
        }
        return null;
    }

    /**
     * Applies default settings to the guild config
     * @throws IOException unable to save
     */
    public void setDefaultSettings() throws IOException
    {
        this.jaraVersion = Core.getVersion();
        this.commandConfig = new HashMap<>();
        this.customCommandSettings = new CustomCommandSettings();
        this.audioConfig = new AudioConfig();
        this.gameConfig = new GameConfig();

        for (ModuleAttributes ma : ModuleManager.getCommandModules())
        {
            this.commandConfig.put(ma.getKey(), new CommandConfig(!ma.isDisableable(), new ArrayList<>())); //By inverting isDisableable, we are disabling the command whenever isDisablable is true.
        }

        Guild guild = Core.getShardManager().getGuildById(guildID);
        addPermissions(guild.getPublicRole().getId(), "Help");

        commandPrefix = '/';
        timeZoneId = TimeZone.getTimeZone(ZoneOffset.UTC).getID();
        moduleConfig = new HashMap<>();
        audioConfig.useVoiceLeaving = true;
        audioConfig.skipVotePercent = 50;
        audioConfig.roleQueueLimit = new HashMap<>();
        audioConfig.roleQueueLimit.put(guild.getPublicRole().getId(), 1);
        gameConfig.useGameChannels = false;
        gameConfig.gameChannelTimeout = "0";
        gameConfig.gameCategoryId = "";
        gameConfig.concurrentGameInChannelAllowed = false;
        save();
    }

    /**
     * This method will take a legacy config and match it to the current Jara version.
     * Once updated in this object, the updates will be written to file.
     * @param legacySettings the settings from a past Jara version
     * @throws IOException unable to access file
     */
    private void updateConfig(GuildSettings legacySettings) throws IOException
    {
        setDefaultSettings();
        if (legacySettings.audioConfig != null)
        {
            if (legacySettings.audioConfig.roleQueueLimit != null)
                this.audioConfig.roleQueueLimit = legacySettings.audioConfig.roleQueueLimit;

            if (legacySettings.audioConfig.skipVotePercent != 0)
                this.audioConfig.skipVotePercent = legacySettings.audioConfig.skipVotePercent;

            if (legacySettings.audioConfig.useVoiceLeaving != false)
                this.audioConfig.useVoiceLeaving = legacySettings.audioConfig.useVoiceLeaving;
        }
        if (legacySettings.gameConfig != null)
        {
            if (legacySettings.gameConfig.concurrentGameInChannelAllowed != false)
                this.gameConfig.concurrentGameInChannelAllowed = legacySettings.gameConfig.concurrentGameInChannelAllowed;

            if (legacySettings.gameConfig.gameCategoryId != null)
                this.gameConfig.gameCategoryId = legacySettings.gameConfig.gameCategoryId;

            if (legacySettings.gameConfig.gameChannelTimeout != null)
                this.gameConfig.gameChannelTimeout = legacySettings.gameConfig.gameChannelTimeout;

            if (legacySettings.gameConfig.useGameChannels != false)
                this.gameConfig.useGameChannels = legacySettings.gameConfig.useGameChannels;
        }
        if (legacySettings.commandConfig != null)
            this.commandConfig = legacySettings.commandConfig;
        if (legacySettings.customCommandSettings != null)
            this.customCommandSettings = legacySettings.customCommandSettings;
        if (legacySettings.commandPrefix != 0)
            this.commandPrefix = legacySettings.commandPrefix;
        if (legacySettings.timeZoneId != null)
            this.timeZoneId = legacySettings.timeZoneId;
        if (legacySettings.moduleConfig != null)
            this.moduleConfig = legacySettings.moduleConfig;

        save();
        logger.info("Updated settings for guild "+guildID+" to Jara "+Core.getVersion());
    }

    /**
     * Allows the roles represented by the IDs to use the command.
     * @param roleIDs the roles to add
     * @param commandKeys the commands to add to
     * @return boolean on change occurred
     * @throws IOException unable to save
     */
    public boolean addPermissions(ArrayList<String> roleIDs, String... commandKeys) throws IOException
    {
        return setPermissions(true, roleIDs, commandKeys);
    }

    /**
     * Allows the role represented by the ID to use the command.
     * @param roleID the role to add
     * @param commandKeys the commands to add to
     * @return boolean on change occurred
     * @throws IOException unable to save
     */
    public boolean addPermissions(String roleID, String... commandKeys) throws IOException
    {
        return setPermissions(true, roleID, commandKeys);
    }

    /**
     * Denies the roles represented by the IDs from using the command.
     * @param roleIDs the roles to remove
     * @param commandKeys the commands to remove from
     * @return boolean on change occurred
     * @throws IOException unable to save
     */
    public boolean removePermissions(ArrayList<String> roleIDs, String... commandKeys) throws IOException
    {
        return setPermissions(false, roleIDs, commandKeys);
    }

    /**
     * Denies the role represented by the ID from using the command.
     * @param roleID the role to remove
     * @param commandKeys the commands to remove from
     * @return boolean on change occurred
     * @throws IOException unable to save
     */
    public boolean removePermissions(String roleID, String... commandKeys) throws IOException
    {
        return setPermissions(false, roleID, commandKeys);
    }

    /**
     * Gets the ID of the guild these settings correspond to
     * @return the id
     */
    public String getGuildId()
    {
        return guildID;
    }

    /**
     * Consider using SettingsUtil#getCommandChar instead if you don't need any other settings.
     * @return the prefix
     */
    public Character getCommandPrefix()
    {
        return this.commandPrefix;
    }

    /**
     * The command prefix, that is, the character that must precede command keys/alises for Jara to recognise them.
     * @param newChar the character to use.
     * @throws IOException unable to save
     */
    public void setCommandPrefix(Character newChar) throws IOException
    {
        this.commandPrefix = newChar;
        save();
    }

    /**
     * Checks if the member is allowed to use this command, based on their roles.
     * @param member the member to check
     * @param commandKey the key of the command to check
     * @return boolean on permitted
     */
    public boolean isPermitted(Member member, String commandKey)
    {
        if (this.commandConfig.containsKey(commandKey))
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
            CommandConfig cc = commandConfig.get(commandKey);   //Yeah, this is a bit messy, but it checks every source for a possible match
            if (cc == null)
            {
                cc = commandConfig.get(customCommandSettings.getCommand(commandKey).getKey());
                if (cc == null)
                {
                    cc = commandConfig.get(ModuleManager.getModule(commandKey).getKey());
                }
            }
            roleIDs.add(member.getGuild().getPublicRole().getId()); //everyone role is not included in getRoles()
            if (!Collections.disjoint(cc.permissions, roleIDs))
            {
                permissionGranted = true;
            }
            return permissionGranted;
        }
        return false;
    }

    /**
     * Returns a list of role IDs who can use this command
     * @param key the command's key
     * @return the role IDs
     */
    public ArrayList<String> getPermissions(String key)
    {
        if (this.commandConfig.containsKey(key))
        {
            return this.commandConfig.get(key).permissions;
        }
        return null;
    }

    /**
     * Modifies the permissions for the specified commands by adding/removing the roles in the list.
     * @param add whether to add/remove permissions
     * @param roleIDs the roles to permit
     * @param commandKeys the commands to affect
     * @return boolean on if a change occurred
     * @throws IOException unable to save
     */
    private boolean setPermissions(boolean add, ArrayList<String> roleIDs, String... commandKeys) throws IOException
    {
        boolean changed = false;
        for (String key : commandKeys)
        {
            if (this.commandConfig.containsKey(key))
            {
                boolean state = this.commandConfig.get(key).enabled;
                ArrayList<String> permissions = this.commandConfig.get(key).permissions;
                if (add) //If add is true, add the roles.
                {
                    if (!permissions.containsAll(roleIDs))
                    {
                        permissions.removeAll(roleIDs); //This ensures a roleID isn't listed twice.
                        permissions.addAll(roleIDs);
                        changed = true;
                    }
                }
                else //If add is false, remove.
                {
                    changed = permissions.removeAll(roleIDs);
                }
                this.commandConfig.replace(key, new CommandConfig(state, permissions));
            }
        }
        if (changed)
        {
            save();
        }
        return changed;
    }

    /**
     * Modifies the permissions for the specified commands by adding/removing the roles in the list.
     * @param add whether to add/remove permissions
     * @param roleID the role to permit
     * @param commandKeys the commands to affect
     * @return boolean on if a change occurred
     * @throws IOException unable to save
     */
    private boolean setPermissions(boolean add, String roleID, String... commandKeys) throws IOException
    {
        boolean changed = false;
        for (String key : commandKeys)
        {
            if (this.commandConfig.containsKey(key))
            {
                boolean state = this.commandConfig.get(key).enabled;
                ArrayList<String> permissions = this.commandConfig.get(key).permissions;
                if (add) //If add is true, add the roles.
                {
                    if (!permissions.contains(roleID))
                    {
                        permissions.add(roleID);
                        changed = true;
                    }
                }
                else //If add is false, remove.
                {
                    changed = permissions.remove(roleID);
                }
                this.commandConfig.replace(key, new CommandConfig(state, permissions));
            }
        }
        if (changed)
        {
            save();
        }
        return changed;
    }

    /**
     * Gets the {@link net.dv8tion.jda.api.entities.Category} id to create game channels in.
     * @return the id
     */
    public String getGameCategoryId()
    {
        return this.gameConfig.gameCategoryId;
    }

    /**
     * Sets the ID of the {@link net.dv8tion.jda.api.entities.Category} to create Game Channels in.
     * @param gameCategoryId the category id
     * @throws IOException unable to save
     */
    public void setGameCategoryId(String gameCategoryId) throws IOException
    {
        this.gameConfig.gameCategoryId = gameCategoryId;
        save();
    }

    /**
     * Updates the stored information for the commands defined by their keys. Pass null parameter to retain current value.
     * @param newState the state to set(Can be null)
     * @param newPermissions the role IDs to toggle access for (Can be null)
     * @param commandKeys the keys of the commands to modify
     * @throws IOException unable to save
     */
    public void setCommandConfiguration(Boolean newState, ArrayList<String> newPermissions, String... commandKeys) throws IOException
    {
        Boolean state;
        ArrayList<String> permissions;
        for (String key : commandKeys)
        {
            ModuleAttributes ma = customCommandSettings.getCommand(key)!=null ? customCommandSettings.getCommandAttributes(key) : ModuleManager.getModule(key);
            if (ma == null)
            {
                continue;
            }
            else if (ma.isDisableable())
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
                this.commandConfig.put(key, new CommandConfig(state, permissions));
            }
        }
        save();
    }

    /**
     * Updates the stored information for the commands within the category. Pass null parameter to retain current value.
     * @param newState the new enabled state of the category's commands
     * @param newPermissions the roles IDs to add/remove to commands in the category
     * @param categoryID the ID of the category
     * @throws IOException unable to save
     */
    public void setCategoryConfiguration(Boolean newState, ArrayList<String> newPermissions, ModuleManager.Category categoryID) throws IOException
    {
        ArrayList<String> keys = new ArrayList<>();
        for (ModuleAttributes ma : ModuleManager.getModulesInCategory(categoryID))
        {
            if (ma.getCommandClass() != null)
            {
                keys.add(ma.getKey());
            }
        }
        setCommandConfiguration(newState, newPermissions, keys.toArray(new String[0]));
    }

    /**
     * Gets if a command is enabled
     * @param commandKey the command to check
     * @return boolean on enabled
     */
    public boolean isCommandEnabled(String commandKey)
    {
        return this.commandConfig.get(commandKey).enabled;
    }

    /**
     * Gets the command keys of all commands enabled in this guild.
     * @return a list of keys
     */
    public ArrayList<String> getEnabledCommands()
    {
        ArrayList<String> enabledCommands = new ArrayList<>();
        for (String key : ModuleManager.getModuleKeys())
        {
            if (this.commandConfig.get(key).enabled)
            {
                enabledCommands.add(key);
            }
        }
        return enabledCommands;
    }


    /**
     * Gets how many milliseconds a game channel can be inactive for before deletion.
     * @return the timeout, in milliseconds
     */
    public String getGameChannelTimeout()
    {
        return this.gameConfig.gameChannelTimeout;
    }

    /**
     * Sets after how long to delete a game channel
     * @param gameChannelTimeout the time in milliseconds
     * @throws IOException unable to save
     */
    public void setGameChannelTimeout(String gameChannelTimeout) throws IOException
    {
        this.gameConfig.gameChannelTimeout = gameChannelTimeout;
        save();
    }

    /**
     * Gets if game channels are enabled
     * @return boolean on enabled
     */
    public boolean isGameChannelsEnabled()
    {
        return this.gameConfig.useGameChannels;
    }

    /**
     * Sets whether to create new channels when a game is launched
     * @param useGameChannels whether to use game channels
     * @throws IOException unable to save
     */
    public void setUseGameChannels(boolean useGameChannels) throws IOException
    {
        this.gameConfig.useGameChannels = useGameChannels;
        save();
    }

    /**
     * Gets the percentage of users in a voice channel (excluding bots) that need to vote for a skip for the track to move on
     * @return the percentage
     */
    public int getTrackSkipPercent()
    {
        return this.audioConfig.skipVotePercent;
    }

    /**
     * Sets the percentage of users in a voice channel (excluding bots) that need to vote for a skip for the track to move on
     * @param newPercent the percentage of users needed to vote
     * @throws IOException error writing to file
     */
    public void setTrackSkipPercent(int newPercent) throws IOException
    {
        if (newPercent >= 0 && newPercent <= 100)
        {
            this.audioConfig.skipVotePercent = newPercent;
            save();
        }
    }

    /**
     * Checks if voice leaving is enabled
     * @return voice leaving enabled
     */
    public boolean isVoiceLeavingEnabled()
    {
        return this.audioConfig.useVoiceLeaving;
    }

    /**
     * Sets if the bot should leave the voice channel when nothing is playing
     * @param state whether to enable leaving or not
     * @throws IOException unable to save
     */
    public void setVoiceLeaving(boolean state) throws IOException
    {
        this.audioConfig.useVoiceLeaving = state;
        save();
    }

    /**
     * Gets the member's highest audio queue amount, based on their roles.
     * @param member the member to check
     * @return the tracks they can queue
     */
    public int getAudioQueueLimit(Member member)
    {
        List<Role> memberRoles = member.getRoles();
        int queueLimit = audioConfig.roleQueueLimit.get(member.getGuild().getPublicRole().getId());
        Integer queueLimitForCurrentRole = 0;
        for (Role role : memberRoles)
        {
            queueLimitForCurrentRole = audioConfig.roleQueueLimit.get(role.getId());
            if (queueLimitForCurrentRole != null && queueLimitForCurrentRole > queueLimit)
            {
                queueLimit = queueLimitForCurrentRole;
            }
        }
        return queueLimit;
    }

    /**
     * Gets the audio queue limit for this role. This is the number of tracks they can have queued.
     * @param role the role to get the limit for
     * @return the limit
     */
    public int getAudioQueueLimit(Role role)
    {
        int queueLimit = audioConfig.roleQueueLimit.get(role.getGuild().getPublicRole().getId());
        if (audioConfig.roleQueueLimit.get(role.getId()) != null && audioConfig.roleQueueLimit.get(role.getId()) > queueLimit)
        {
            queueLimit = audioConfig.roleQueueLimit.get(role.getId());
        }
        return queueLimit;
    }

    /**
     * Sets the audio queue limit for this role.
     * @param role the role to set a limit for
     * @param limit the number of tracks a role member can queue up
     * @throws IOException unable to save
     */
    public void setAudioQueueLimit(Role role, int limit) throws IOException
    {
        audioConfig.roleQueueLimit.put(role.getId(), limit);
        save();
    }

    /**
     * Sets the ability to have two or more games running in a channel simultaneously.
     * @param state true/false on enable
     * @throws IOException unable to save
     */
    public void setConcurrentGameInChannelAllowed(boolean state) throws IOException
    {
        this.gameConfig.concurrentGameInChannelAllowed = state;
        save();
    }

    /**
     * Gets if two or more games can be running in a single channel simultaneously
     * @return boolean on setting
     */
    public boolean isConcurrentGameInChannelAllowed()
    {
        return this.gameConfig.concurrentGameInChannelAllowed;
    }

    /**
     * Removes the module command configuration entry for the module specified by the key.
     * @param key the module key
     */
    protected void removeConfig(String key)
    {
        commandConfig.remove(key);
    }

    /**
     * Gets custom command settings
     * @return the {@link CustomCommandSettings}
     */
    public CustomCommandSettings getCustomCommandSettings()
    {
        return customCommandSettings;
    }

    /**
     * Gets the Zoned Date Time this guild.
     * @return current time in the guild
     */
    public ZonedDateTime getZonedDateTime()
    {
        return LocalDateTime.now().atZone(ZoneId.of(timeZoneId));
    }

    /**
     * Gets the timezone ID for this guild.
     * @return the Zone ID
     */
    public ZoneId getTimeZoneId()
    {
        return ZoneId.of(timeZoneId);
    }

    /**
     * Sets the timezone id.
     * @param timeZoneId the timezone to set
     * @throws IOException unable to access file
     */
    public void setTimeZoneId(String timeZoneId) throws IOException
    {
        this.timeZoneId = timeZoneId;
        save();
    }

    /**
     * A method for module developers to have basic configuration without creating their own files.<br>
     *     This method adds the specified value into the guild's main config, so storing large values is heavily not recommended.<br>
     *         It is essential the key you provide is unique to avoid conflicts with other modules. It is recommended to use your classpath, followed by a specific name for this key, such as com.Zazsona.Jara.MyModuleSetting
     * @param key a key unique to your module
     * @param value the small value to store
     * @throws IOException unable to save
     */
    public void setCustomModuleSetting(String key, Object value) throws IOException
    {
        moduleConfig.put(key, value);
        save();
    }

    /**
     * Gets a value, denoted by the key, previously stored with {@link #setCustomModuleSetting(String, Object)}.
     * @param key the key paired with a value
     * @return the value, or null if the key points to nothing
     */
    public Object getCustomModuleSetting(String key)
    {
        return moduleConfig.get(key);
    }

    /**
     * Removes a module setting from the file.
     * @param key the key of the setting to remove
     * @throws IOException unable to access file
     */
    public void removeCustomModuleSetting(String key) throws IOException
    {
        moduleConfig.remove(key);
        save();
    }
}
