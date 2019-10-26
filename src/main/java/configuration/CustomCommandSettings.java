package configuration;

import commands.CustomCommand;
import configuration.guild.CustomCommandBuilder;
import jara.Core;
import jara.ModuleAttributes;
import jara.ModuleRegister;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class CustomCommandSettings implements Serializable
{
    /**
     * The user made custom commands for this guild.
     */
    protected HashMap<String, CustomCommandBuilder> customCommands = new HashMap<>();

    protected transient GuildSettings guildSettings; //Transient so it doesn't cause a serialization loop

    /**
     * Sets the guild settings these custom commands are tied to.
     * The {@link CustomCommandSettings} in the {@link GuildSettings} must match, otherwise desyncronisation will occur.
     * @param gs the guild settings to tie to
     */
    protected void setGuildSettings(GuildSettings gs)
    {
        this.guildSettings = gs;
    }

    /**
     * Adda a custom command for this guild. These are simple commands which users can make which will simply reply back with a predefined message.
     * @param key the command's key
     * @param aliases the command's aliases
     * @param description the command's description
     * @param category the command's {@link jara.ModuleRegister.Category}
     * @param roleIDs the roles this command will grant/remove
     * @param audioLink the audio this command will play
     * @param message the message this command will display
     * @return the command builder, or null if the key is taken
     * @throws IOException unable to save
     */
    public CustomCommandBuilder addCommand(String key, String[] aliases, String description, ModuleRegister.Category category, ArrayList<String> roleIDs, String audioLink, String message) throws IOException
    {
        if (customCommands.containsKey(key.toLowerCase()) || ModuleRegister.getModule(key) != null)
        {
            return null;
        }
        customCommands.put(key.toLowerCase(), new CustomCommandBuilder(key, aliases, description, category, roleIDs, audioLink, message));
        guildSettings.setCommandConfiguration(true, new ArrayList<>(), key);
        guildSettings.save();
        return customCommands.get(key.toLowerCase());
    }

    /**
     * Updates a custom command stored in the config, if it exists.
     * @param key the key of the command to edit
     * @param ccb the command's new builder
     * @throws IOException unable to save
     */
    public void editCommand(String key, CustomCommandBuilder ccb) throws IOException
    {
        key = key.toLowerCase();
        if (customCommands.containsKey(key) || ModuleRegister.getModule(key) != null)
        {
            customCommands.put(key, ccb);
            guildSettings.save();
        }
    }

    /**
     * Removes a custom command for this guild.
     * @param key the key of the command to remove
     * @throws IOException unable to save
     */
    public void removeCommand(String key) throws IOException
    {
        key = key.toLowerCase();
        customCommands.remove(key);
        guildSettings.removeConfig(key);
        guildSettings.save();
    }

    /**
     * Gets a command specified by the key
     * @param key the key of the command to get
     * @return the command's builder, or null if the key is invalid
     */
    public CustomCommandBuilder getCommand(String key)
    {
        key = key.toLowerCase();
        CustomCommandBuilder ccb = customCommands.get(key);
        if (ccb != null)
        {
            return ccb;
        }
        else
        {
            for (Map.Entry<String, CustomCommandBuilder> entry : customCommands.entrySet())
            {
                for (String alias : entry.getValue().getAliases())
                {
                    if (key.equalsIgnoreCase(alias))
                    {
                        return customCommands.get(entry.getKey());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the case-respecting command keys.
     * @return a collection of custom command keys
     */
    public Collection<String> getCommandKeys()
    {
        LinkedList<String> keys = new LinkedList<>();
        for (CustomCommandBuilder ccb : customCommands.values())
        {
            keys.add(ccb.getKey());
        }
        return keys;
    }

    /**
     * Generates the command attributes for the specified custom command
     * @param key the key of the command to get
     * @return the command's attributes, or null if the key is invalid
     */
    public ModuleAttributes getCommandAttributes(String key)
    {
        key = key.toLowerCase();
        CustomCommandBuilder ccc = getCommand(key);
        if (ccc == null)
        {
            return null;
        }
        else
        {
            return new ModuleAttributes(ccc.getKey(), ccc.getDescription(), ccc.getAliases(), ccc.getCategory(), Core.getVersion(), true, CustomCommand.class, null, null, null);
        }
    }

    /**
     * Returns the custom command's launcher.
     * @param key the command's key
     * @return the command's launcher, or null on invalid key
     */
    public GuildCustomCommandLauncher getCommandLauncher(String key)
    {
        key = key.toLowerCase();
        ModuleAttributes ma = getCommandAttributes(key);
        if (ma == null)
        {
            return null;
        }
        else
        {
            return new GuildCustomCommandLauncher(ma);
        }

    }
}
