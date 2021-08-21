package com.zazsona.jara.configuration.guild;

import java.io.Serializable;
import java.util.HashMap;

public class AudioConfig implements Serializable
{
    private static final long serialVersionUID = 1L;
    /**
     * The percentage of total members in a voice channel (excluding the bot) who need to vote for a track to be skipped.
     */
    public int skipVotePercent;

    /**
     * Whether or not to leave voice channels when no audio is playing
     */
    public boolean useVoiceLeaving;

    /**
     * The number of items each role can queue.
     */
    public HashMap<String, Integer> roleQueueLimit;
}
