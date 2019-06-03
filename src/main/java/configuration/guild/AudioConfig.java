package configuration.guild;

import java.io.Serializable;

public class AudioConfig implements Serializable
{
    /**
     * The percentage of total members in a voice channel (excluding the bot) who need to vote for a track to be skipped.
     */
    public int skipVotePercent;

    /**
     * Whether or not to leave voice channels when no audio is playing
     */
    public boolean useVoiceLeaving;
}
