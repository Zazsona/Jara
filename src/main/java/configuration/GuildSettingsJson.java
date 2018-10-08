package configuration;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class GuildSettingsJson
{
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

    protected class AudioConfig
    {
        /**
         * The percentage of total members in a voice channel (excluding the bot) who need to vote for a track to be skipped.
         */
        protected int skipVotePercent;

        /**
         * Whether or not to leave voice channels when no audio is playing
         */
        protected boolean useVoiceLeaving;
    }
    protected class GameConfig
    {
        /**
         * Defines whether to use individual channels for each game.
         */
        protected boolean useGameChannels;
        /**
         * Defines which Discord channel category to create game channels in.
         */
        protected String gameCategoryId;
        /**
         * Defines the amount of time (in minutes) for which an inactive game channel should live.
         * The timer resets whenever a message is sent in the channel.
         */
        protected String gameChannelTimeout;
    }
    protected class CommandConfig
    {
        /**
         * Holds the role IDs of which roles can use this command.
         */
        protected ArrayList<String> permissions;
        /**
         * Whether the command is enabled or not for this guild.
         */
        protected boolean enabled;

        /**
         * Constructor
         * @param enabled
         * @param permissions
         */
        protected CommandConfig(boolean enabled, ArrayList<String> permissions)
        {
            this.enabled = enabled;
            this.permissions = permissions;
        }
    }
}
