package configuration;

import commands.CmdUtil;
import commands.Command;
import commands.CustomCommand;
import configuration.guild.AudioConfig;
import configuration.guild.CommandConfig;
import configuration.guild.CustomCommandBuilder;
import configuration.guild.GameConfig;
import jara.CommandAttributes;
import jara.CommandRegister;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class GuildSettings implements Serializable
{
    private static final long serialVersionUID = 1L;
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
     * The logger.
     */
    private static transient final Logger logger = LoggerFactory.getLogger(GuildSettings.class);

    /**
     * The user made custom commands for this guild.
     */
    protected HashMap<String, CustomCommandBuilder> customCommandsConfig;

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
    private synchronized void restore() throws IOException
    {
        try
        {
            if (new File(getGuildSettingsFilePath(guildID)).exists())
            {
                FileInputStream fis = new FileInputStream(getGuildSettingsFilePath(guildID));
                ObjectInputStream ois = new ObjectInputStream(fis);
                GuildSettings settingsFromFile = (GuildSettings) ois.readObject();
                this.commandPrefix = settingsFromFile.commandPrefix;
                this.audioConfig = settingsFromFile.audioConfig;
                this.gameConfig = settingsFromFile.gameConfig;
                this.customCommandsConfig = settingsFromFile.customCommandsConfig;
                this.commandConfig = new HashMap<>(settingsFromFile.commandConfig);
                if (!commandConfig.keySet().containsAll(Arrays.asList(CommandRegister.getAllCommandKeys())))
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
        catch (ClassNotFoundException e)
        {
            Guild guild = Core.getShardManager().getGuildById(guildID);
            guild.getOwner().getUser().openPrivateChannel().complete().sendMessage("The config for your guild, "+guild.getName()+", has become corrupted or is no longer available and has been reset. Please contact your host for further details.").queue();
            logger.error("Guild settings are corrupted for guild "+guildID+" ("+guild.getName()+"). Resetting.");
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
    private synchronized void save() throws NullPointerException, IOException
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
        oos.writeObject(this);
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
        if (!commandConfig.keySet().containsAll(Arrays.asList(CommandRegister.getAllCommandKeys())))
        {
            ArrayList<String> newKeys = new ArrayList<>();
            for (String key : CommandRegister.getAllCommandKeys())
            {
                if (!commandConfig.keySet().contains(key))
                {
                    commandConfig.put(key, new CommandConfig(!CommandRegister.getCommand(key).isDisableable(), new ArrayList<>()));
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
        this.commandConfig = new HashMap<>();
        this.customCommandsConfig = new HashMap<>();
        this.audioConfig = new AudioConfig();
        this.gameConfig = new GameConfig();

        for (CommandAttributes ca : CommandRegister.getRegister())
        {
            this.commandConfig.put(ca.getCommandKey(), new CommandConfig(!ca.isDisableable(), new ArrayList<>())); //By inverting isDisableable, we are disabling the command whenever isDisablable is true.
        }

        commandPrefix = '/';
        audioConfig.useVoiceLeaving = true;
        audioConfig.skipVotePercent = 50;
        gameConfig.useGameChannels = false;
        gameConfig.gameChannelTimeout = "0";
        gameConfig.gameCategoryId = "";
        gameConfig.concurrentGameInChannelAllowed = false;

        Guild guild = Core.getShardManager().getGuildById(guildID);
        ArrayList<String> everyoneRole = new ArrayList<>();
        everyoneRole.add(guild.getPublicRole().getId());
        addPermissions(everyoneRole, "Help");
        save();
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
     * @return
     */
    private HashMap<String, CommandConfig> getCommandMap()
    {
        return new HashMap<>(this.commandConfig);
    }

    /**
     * @param guildCommandConfig
     */
    private void setCommandMap(HashMap<String, CommandConfig> guildCommandConfig) throws IOException
    {
        this.commandConfig = guildCommandConfig;
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
            cc = commandConfig.get(getCustomCommand(commandKey).getKey());
            if (cc == null)
            {
                cc = commandConfig.get(CommandRegister.getCommand(commandKey).getCommandKey());
            }
        }
        roleIDs.add(member.getGuild().getPublicRole().getId()); //everyone role is not included in getRoles()
        if (!Collections.disjoint(cc.permissions, roleIDs))
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
    private boolean setPermissions(boolean add, ArrayList<String> roleIDs, String... commandKeys) throws IOException
    {
        boolean changed = false;
        for (String key : commandKeys)
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
            CommandAttributes ca = getCustomCommand(key)!=null ? getCustomCommandAttributes(key) : CommandRegister.getCommand(key);
            if (ca == null)
            {
                return;
            }
            else if (ca.isDisableable())
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
                this.commandConfig.replace(key, new CommandConfig(state, permissions));
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
    public void setCategoryConfiguration(Boolean newState, ArrayList<String> newPermissions, CommandRegister.Category categoryID) throws IOException
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
     * Adda a custom command for this guild. These are simple commands which users can make which will simply reply back with a predefined message.
     * @param key
     * @param aliases
     * @param description
     * @param category
     * @param roleIDs
     * @param audioLink
     * @param message
     *
     * @return
     * CustomCommandConfig - The new Command<br>
     * null - A command with that key already exists.
     */
    public CustomCommandBuilder addCustomCommand(String key, String[] aliases, String description, CommandRegister.Category category, ArrayList<String> roleIDs, String audioLink, String message) throws IOException
    {
        key = key.toLowerCase();
        if (customCommandsConfig.containsKey(key) || commandConfig.containsKey(key)) //TODO: Make sure command keys respect case
        {
            return null;
        }
        customCommandsConfig.put(key, new CustomCommandBuilder(key, aliases, description, category, roleIDs, audioLink, message));
        commandConfig.put(key, new CommandConfig(true, new ArrayList<>()));
        save();
        return customCommandsConfig.get(key);
    }

    /**
     * Updates a custom command stored in the config, if it exists.
     * @param key the key of the command to edit
     * @param ccb the command's new profile
     * @throws IOException
     */
    public void editCustomCommand(String key, CustomCommandBuilder ccb) throws IOException
    {
        key = key.toLowerCase();
        if (customCommandsConfig.containsKey(key) || commandConfig.containsKey(key))
        {
            customCommandsConfig.put(key, ccb);
            save();
        }
    }

    /**
     * Removes a custom command for this guild.
     * @param key
     */
    public void removeCustomCommand(String key) throws IOException
    {
        key = key.toLowerCase();
        customCommandsConfig.remove(key);
        commandConfig.remove(key);
        save();
    }

    /**
     * @param key
     * @return CustomCommandConfig - The custom guild command.
     * @return null - Invalid key/alias
     */
    public CustomCommandBuilder getCustomCommand(String key)
    {
        key = key.toLowerCase();
        if (customCommandsConfig.containsKey(key))
        {
            return customCommandsConfig.get(key.toLowerCase());
        }
        else
        {
            for (Map.Entry<String, CustomCommandBuilder> entry : customCommandsConfig.entrySet())
            {
                for (String alias : entry.getValue().getAliases())
                {
                    if (key.equals(alias))
                    {
                        return customCommandsConfig.get(entry.getKey());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the HashMap with the custom command config details.<br>
     * The key is the command key, with the value being of type CustomCommandConfig.
     * @return
     */
    public HashMap<String, CustomCommandBuilder> getCustomCommandMap()
    {
        return customCommandsConfig;
    }

    /**
     * Generates the command attributes for the specified custom command
     * @param key
     * @return CommandAttributes - The command's attributes
     * @return null - Invalid key
     */
    public CommandAttributes getCustomCommandAttributes(String key)
    {
        key = key.toLowerCase();
        CustomCommandBuilder ccc = getCustomCommand(key);
        if (ccc == null)
        {
            return null;
        }
        else
        {
            return new CommandAttributes(ccc.getKey(), ccc.getDescription(), CustomCommand.class, ccc.getAliases(), ccc.getCategory(), true);
        }
    }

    /**
     * Returns the custom command's launcher.
     * @param key
     * @return CommandLauncher = The custom command's launcher
     * @return null - Invalid key
     */
    public CustomCommandLauncher getCustomCommandLauncher(String key)
    {
        key = key.toLowerCase();
        CommandAttributes ca = getCustomCommandAttributes(key);
        if (ca == null)
        {
            return null;
        }
        else
        {
            return new CustomCommandLauncher(getCustomCommandAttributes(key));
        }

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
}
