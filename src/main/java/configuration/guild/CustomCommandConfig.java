package configuration.guild;

import jara.CommandRegister;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class CustomCommandConfig implements Serializable
{
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
    private CommandRegister.Category category;
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
     */
    public CustomCommandConfig(String key, String[] aliases, String description, CommandRegister.Category category, ArrayList<String> roleIDs, String audioLink, String message)
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
     * @return
     */
    public String getKey()
    {
        return key;
    }

    /**
     * @return
     */
    public String[] getAliases()
    {
        return aliases;
    }
    /**
     * @return description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @return category
     */
    public CommandRegister.Category getCategory()
    {
        return category;
    }

    /**
     * @return message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @return
     */
    public String getAudioLink()
    {
        return audioLink;
    }

    /**
     * @return
     */
    public ArrayList<String> getRoles()
    {
        return (ArrayList<String>) roleIDs.clone();
    }

    public void modifyAliases(String... aliases)
    {
        ArrayList<String> aliasesList = new ArrayList<>();
        aliasesList.addAll(Arrays.asList(this.aliases));
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

    public void setDescription(String description)
    {
        this.description = description;
    }
    public void setCategory(CommandRegister.Category category)
    {
        this.category = category;
    }
    public void setMessage(String message)
    {
        this.message = message;
    }
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
    public void setAudioLink(String audioLink)
    {
        this.audioLink = audioLink;
    }
}
