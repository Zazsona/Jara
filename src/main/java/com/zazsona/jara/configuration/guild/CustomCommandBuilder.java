package com.zazsona.jara.configuration.guild;

import com.zazsona.jara.ModuleManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class CustomCommandBuilder implements Serializable
{
    private static final long serialVersionUID = 1L;
    /**
     * The command key
     */
    private String key;
    /**
     * The strings which can be used to call the command
     */
    private String[] aliases;
    /**
     * A short description to describe the command
     */
    private String description;
    /**
     * The category of the command
     */
    private ModuleManager.Category category;
    /**
     * The text that will be returned when a user calls the command.
     */
    private String message;
    /**
     * A list of roles to add/remove to the user
     */
    private ArrayList<String> roleIDs;
    /**
     * The audio link to play.
     */
    private String audioLink;

    /**
     * Constructor
     * @param key the unique identifier for the command
     * @param aliases The strings which can be used to call the command
     * @param description A short description to describe the command
     * @param category The {@link ModuleManager.Category} of the command
     * @param roleIDs The IDs of the roles the command will toggle, or null to disable
     * @param audioLink the URL of audio to play, or null to disable
     * @param message the message to send, or null to disable
     */
    public CustomCommandBuilder(String key, String[] aliases, String description, ModuleManager.Category category, ArrayList<String> roleIDs, String audioLink, String message)
    {
        this.key = key;
        this.aliases = aliases;
        this.description = description;
        this.category = category;
        this.message = message;
        this.roleIDs = roleIDs;
        this.audioLink = audioLink;
    }

    /**
     * Gets the custom command's key
     * @return the key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Gets the custom command's aliases, alternate strings used to summon a command
     * @return the aliases
     */
    public String[] getAliases()
    {
        return aliases;
    }
    /**
     * Gets a short description of the custom command
     * @return description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Gets the command's category
     * @return category
     */
    public ModuleManager.Category getCategory()
    {
        return category;
    }

    /**
     * Gets the message to display upon execution
     * @return message or null if absent
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Gets the audio to play when the command is run
     * @return audio URL or null if absent
     */
    public String getAudioLink()
    {
        return audioLink;
    }

    /**
     * Gets the roles to toggle when the command is run
     * @return role IDs, or null if absent
     */
    public ArrayList<String> getRoles()
    {
        return (ArrayList<String>) roleIDs.clone();
    }

    /**
     * Toggles on/off the aliases to use
     * @param aliases the aliases to toggle
     */
    public void modifyAliases(String... aliases)
    {
        ArrayList<String> aliasesList = new ArrayList<>(Arrays.asList(this.aliases));
        for (String alias : aliases)
        {
            if (aliasesList.contains(alias))
            {
                aliasesList.remove(alias.toLowerCase());
            }
            else
            {
                aliasesList.add(alias.toLowerCase());
            }
        }
        this.aliases = aliasesList.toArray(new String[0]);
    }

    /**
     * Sets the description of the command
     * @param description the description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Sets the {@link ModuleManager.Category} of the command.
     * @param category the category
     */
    public void setCategory(ModuleManager.Category category)
    {
        this.category = category;
    }

    /**
     * Sets the message to display on the command
     * @param message the message, or null to remove this feature
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * Sets the roles to toggle on the command
     * @param roleIDs role IDs
     */
    public void modifyRoles(String... roleIDs)
    {
        for (String roleID : roleIDs)
        {
            if (this.roleIDs.contains(roleID))
            {
                this.roleIDs.remove(roleID);
            }
            else
            {
                this.roleIDs.add(roleID);
            }
        }
    }
    /**
     * Sets the audio to play on the command
     * @param audioLink the audio URL, or null to remove this feature
     */
    public void setAudioLink(String audioLink)
    {
        this.audioLink = audioLink;
    }
}
