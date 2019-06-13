package configuration.guild;

import java.io.Serializable;

public class GameConfig implements Serializable
{
    private static final long serialVersionUID = 1L;
    /**
     * Defines whether to use individual channels for each game.
     */
    public boolean useGameChannels;
    /**
     * Defines which Discord channel category to create game channels in.
     */
    public String gameCategoryId;
    /**
     * Defines the amount of time (in minutes) for which an inactive game channel should live.
     * The timer resets whenever a message is sent in the channel.
     */
    public String gameChannelTimeout;
    /**
     * Defines whether to have multiple games running simultaneously in a single channel or not<br>
     * This is ignored if useGameChannels is enabled, as that enforces this by design.
     */
    public boolean concurrentGameInChannelAllowed;
}
