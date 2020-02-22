package listeners;

import configuration.GuildSettings;

public class ConfigListener extends JaraListener
{
    /**
     * Fires when the config is updated, such as user-made changes, or a Jara version update.
     * @param guildID the guild ID
     * @param guildSettings the updated settings.
     */
    public void onUpdate(String guildID, GuildSettings guildSettings)
    {

    }

    /**
     * CURRENTlY UNUSED
     * Fires when the guild's settings are reset, ideal for clearing module data.
     * @param guildID the reset guild's id
     */
    public void onReset(String guildID)
    {

    }

    /**
     * Fires when the setup wizard is run in this guild.
     * @param guildID
     */
    public void onSetup(String guildID)
    {

    }
}
