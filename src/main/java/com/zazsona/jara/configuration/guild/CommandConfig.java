package com.zazsona.jara.configuration.guild;

import java.io.Serializable;
import java.util.ArrayList;

public class CommandConfig implements Serializable
{
    private static final long serialVersionUID = 1L;
    /**
     * Holds the role IDs of which roles can use this command.
     */
    public ArrayList<String> permissions;
    /**
     * Whether the command is enabled or not for this guild.
     */
    public boolean enabled;

    /**
     * Constructor
     * @param enabled whether the command is enabled
     * @param permissions the roles IDs allowed to use the command
     */
    public CommandConfig(boolean enabled, ArrayList<String> permissions)
    {
        this.enabled = enabled;
        this.permissions = permissions;
    }
}
