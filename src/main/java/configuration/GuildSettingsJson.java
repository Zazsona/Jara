package configuration;

import jara.CommandRegister;

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
    protected AudioConfig audioConfig = new AudioConfig();
    /**
     * The guild's game settings.
     */
    protected GameConfig gameConfig = new GameConfig();
    /**
     * The guild's command settings.
     */
    protected HashMap<String, CommandConfig> commandConfig = new HashMap<>();

    /**
     * The user made custom commands for this guild.
     */
    protected HashMap<String, CustomCommandConfig> customCommandsConfig = new HashMap<>();

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
    public class CustomCommandConfig
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
         * Constructor
         */
        protected CustomCommandConfig(String key, String[] aliases, String description, CommandRegister.Category category, String message)
        {
            this.key = key;
            this.aliases = aliases;
            this.description = description;
            this.category = category;
            this.message = message;
        }
    }
}
