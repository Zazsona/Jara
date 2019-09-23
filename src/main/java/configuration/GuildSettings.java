package configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.MalformedJsonException;
import commands.CmdUtil;
import module.Command;
import configuration.guild.AudioConfig;
import configuration.guild.CommandConfig;
import configuration.guild.GameConfig;
import jara.ModuleAttributes;
import jara.ModuleRegister;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
    private String guildID;
    /**
     * The Character used to summon the bot
     */
    protected char commandPrefix;
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
     * The logger.
     */
    private static transient final Logger logger = LoggerFactory.getLogger(GuildSettings.class);

    public GuildSettings(String guildID) throws IOException
    {
        this.guildID = guildID;
        restore();
    }

    /**
     * Returns the directory which stores guild settings files.
     * @return
     * File - Guild Settings directory
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
     * @param guildID
     * @return
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
                FileInputStream fis = new FileInputStream(getGuildSettingsFilePath(guildID));
                ObjectInputStream ois = new ObjectInputStream(fis);
                Gson gson = new Gson();
                GuildSettings settingsFromFile = gson.fromJson((String) ois.readObject(), GuildSettings.class);
                ois.close();
                fis.close();
                if (!Core.getVersion().equalsIgnoreCase(settingsFromFile.jaraVersion))
                {
                    updateConfig(settingsFromFile);
                    this.customCommandSettings.setGuildSettings(this);
                }
                else
                {
                    this.commandPrefix = settingsFromFile.commandPrefix;
                    this.audioConfig = settingsFromFile.audioConfig;
                    this.gameConfig = settingsFromFile.gameConfig;
                    this.commandConfig = new HashMap<>(settingsFromFile.commandConfig);
                    this.customCommandSettings = settingsFromFile.customCommandSettings;
                    this.customCommandSettings.setGuildSettings(this);
                }
                if (!commandConfig.keySet().containsAll(Arrays.asList(ModuleRegister.getCommandModuleKeys())))
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
                    Core.getShardManager().getGuildById(guildID).getOwner().getUser().openPrivateChannel().complete().sendMessage(embed.build()).queue(); //Yuck.
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
        catch (ClassNotFoundException | StreamCorruptedException | MalformedJsonException e)
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
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        oos.writeObject(gson.toJson(this));
        oos.close();
        fos.close();
    }

    /**
     * Deletes this guild's settings.
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
        String[] commandModuleKeys = ModuleRegister.getCommandModuleKeys();
        if (!commandConfig.keySet().containsAll(Arrays.asList(commandModuleKeys)))
        {
            ArrayList<String> newKeys = new ArrayList<>();
            for (String key : commandModuleKeys)
            {
                if (!commandConfig.keySet().contains(key))
                {
                    commandConfig.put(key, new CommandConfig(!ModuleRegister.getModule(key).isDisableable(), new ArrayList<>()));
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
     */
    public void setDefaultSettings() throws IOException
    {
        this.jaraVersion = Core.getVersion();
        this.commandConfig = new HashMap<>();
        this.customCommandSettings = new CustomCommandSettings();
        this.audioConfig = new AudioConfig();
        this.gameConfig = new GameConfig();

        for (ModuleAttributes ma : ModuleRegister.getCommandModules())
        {
            this.commandConfig.put(ma.getKey(), new CommandConfig(!ma.isDisableable(), new ArrayList<>())); //By inverting isDisableable, we are disabling the command whenever isDisablable is true.
        }

        Guild guild = Core.getShardManager().getGuildById(guildID);
        addPermissions(guild.getPublicRole().getId(), "Help");

        commandPrefix = '/';
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
     * @param legacySettings
     * @throws IOException
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

        save();
        logger.info("Updated settings for guild "+guildID+" to Jara "+Core.getVersion());
    }

    /**
     * Allows the roles represented by the IDs to use the command.
     * @param roleIDs
     * @param commandKeys
     */
    public boolean addPermissions(ArrayList<String> roleIDs, String... commandKeys) throws IOException
    {
        return setPermissions(true, roleIDs, commandKeys);
    }

    /**
     * Allows the role represented by the ID to use the command.
     * @param roleID
     * @param commandKeys
     */
    public boolean addPermissions(String roleID, String... commandKeys) throws IOException
    {
        return setPermissions(true, roleID, commandKeys);
    }

    /**
     * Denies the roles represented by the IDs from using the command.
     * @param roleIDs
     * @param commandKeys
     */
    public boolean removePermissions(ArrayList<String> roleIDs, String... commandKeys) throws IOException
    {
        return setPermissions(false, roleIDs, commandKeys);
    }

    /**
     * Denies the role represented by the ID from using the command.
     * @param roleID
     * @param commandKeys
     */
    public boolean removePermissions(String roleID, String... commandKeys) throws IOException
    {
        return setPermissions(false, roleID, commandKeys);
    }

    /**
     * @return
     */
    public String getGuildId()
    {
        return guildID;
    }

    /**
     * Consider using SettingsUtil#getCommandChar instead if you don't need any other settings.
     * @return
     */
    public Character getCommandPrefix()
    {
        return this.commandPrefix;
    }

    /**
     * @param newChar
     */
    public void setCommandPrefix(Character newChar) throws IOException
    {
        this.commandPrefix = newChar;
        save();
    }

    /**
     * Checks if the member is allowed to use this command, based on their roles.
     * @param member
     * @param commandKey
     * @return
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
                    cc = commandConfig.get(ModuleRegister.getModule(commandKey).getKey());
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
     * Checks if the member is allowed to use this command, based on their roles.
     * @param member
     * @param command
     * @return
     */
    public boolean isPermitted(Member member, Class<? extends Command> command)
    {
        return isPermitted(member, ModuleRegister.getModule(command).getKey());
    }

    /**
     * Returns a list of role IDs who can use this command
     * @param key
     * @return ArrayList<String> - List of Role IDs
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
     * @return
     */
    public String getGameCategoryId()
    {
        return this.gameConfig.gameCategoryId;
    }

    /**
     * @param gameCategoryId
     */
    public void setGameCategoryId(String gameCategoryId) throws IOException
    {
        this.gameConfig.gameCategoryId = gameCategoryId;
        save();
    }

    /**
     * Updates the stored information for the commands defined by their keys. Pass null parameter to retain current value.
     * @param newState (Can be null)
     * @param newPermissions (Can be null)
     * @param commandKeys
     */
    public void setCommandConfiguration(Boolean newState, ArrayList<String> newPermissions, String... commandKeys) throws IOException
    {
        Boolean state;
        ArrayList<String> permissions;
        for (String key : commandKeys)
        {
            ModuleAttributes ma = customCommandSettings.getCommand(key)!=null ? customCommandSettings.getCommandAttributes(key) : ModuleRegister.getModule(key);
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
     * @param newState
     * @param newPermissions
     * @param categoryID
     */
    public void setCategoryConfiguration(Boolean newState, ArrayList<String> newPermissions, ModuleRegister.Category categoryID) throws IOException
    {
        ArrayList<String> keys = new ArrayList<>();
        for (ModuleAttributes ma : ModuleRegister.getModulesInCategory(categoryID))
        {
            if (ma.getCommandClass() != null)
            {
                keys.add(ma.getKey());
            }
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
        for (String key : ModuleRegister.getModuleKeys())
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
    public void setGameChannelTimeout(String gameChannelTimeout) throws IOException
    {
        this.gameConfig.gameChannelTimeout = gameChannelTimeout;
        save();
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
    public void setUseGameChannels(boolean useGameChannels) throws IOException
    {
        this.gameConfig.useGameChannels = useGameChannels;
        save();
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
    public void setTrackSkipPercent(int newPercent) throws IOException
    {
        if (newPercent >= 0 && newPercent <= 100)
        {
            this.audioConfig.skipVotePercent = newPercent;
            save();
        }
    }

    /**
     * @return
     */
    public boolean isVoiceLeavingEnabled()
    {
        return this.audioConfig.useVoiceLeaving;
    }

    /**
     * @param state
     */
    public void setVoiceLeaving(boolean state) throws IOException
    {
        this.audioConfig.useVoiceLeaving = state;
        save();
    }

    /**
     * Gets the member's highest audio queue amount, based on their roles.
     * @param member
     * @return
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
     * Gets the audio queue limit for this role.
     * @param role
     * @return
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
     * @param role
     * @param limit the number of tracks a role member can queue up
     * @throws IOException
     */
    public void setAudioQueueLimit(Role role, int limit) throws IOException
    {
        audioConfig.roleQueueLimit.put(role.getId(), limit);
        save();
    }

    /**
     * Sets the ability to have two or more games running in a channel simultaneously.
     * @param state
     */
    public void setConcurrentGameInChannelAllowed(boolean state) throws IOException
    {
        this.gameConfig.concurrentGameInChannelAllowed = state;
        save();
    }

    /**
     * Gets if two or more games can be running in a single channel simultaneously
     * @return
     */
    public boolean isConcurrentGameInChannelAllowed()
    {
        return this.gameConfig.concurrentGameInChannelAllowed;
    }

    /**
     * Removes the configuration entry for the command specified by the key.
     * @param key
     */
    protected void removeConfig(String key)
    {
        commandConfig.remove(key);
    }

    /**
     * Gets custom command settings
     * @return
     */
    public CustomCommandSettings getCustomCommandSettings()
    {
        return customCommandSettings;
    }
}
