package configuration;

import jara.CommandRegister;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
        /**
         * Defines whether to have multiple games running simultaneously in a single channel or not<br>
         * This is ignored if useGameChannels is enabled, as that enforces this by design.
         */
        protected boolean concurrentGameInChannelAllowed;
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
         * A list of roles to add/remove to the user
         */
        private ArrayList<String> roleIDs;
        /**
         * The audio link to play.
         */
        private String audioLink;

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
                    aliasesList.remove(alias);
                }
                else
                {
                    aliasesList.add(alias);
                }
            }
            this.aliases = aliasesList.toArray(aliases);
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
        /**
         * Constructor
         */
        protected CustomCommandConfig(String key, String[] aliases, String description, CommandRegister.Category category, ArrayList<String> roleIDs, String audioLink, String message)
        {
            this.key = key;
            this.aliases = aliases;
            this.description = description;
            this.category = category;
            this.message = message;
            this.roleIDs = roleIDs;
            this.audioLink = audioLink;
        }
    }
}
