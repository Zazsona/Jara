package configuration;

import java.util.HashMap;

public abstract class GlobalSettingsJson
{
    /**
     * The token used for logging into Discord.
     */
    protected String token;
    /**
     * The list of configured commands, and their enabled status.
     */
    protected HashMap<String, Boolean> commandConfig;
}
