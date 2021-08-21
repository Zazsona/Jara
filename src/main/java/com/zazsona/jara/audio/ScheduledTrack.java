package com.zazsona.jara.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class ScheduledTrack
{
    private AudioTrack audioTrack;
    private String userID;

    /**
     * Constructor
     * @param at the track
     * @param userID the user who scheduled it
     */
    public ScheduledTrack(AudioTrack at, String userID)
    {
        this.audioTrack = at;
        this.userID = userID;
    }

    /**
     * Gets the track to play
     * @return the track
     */
    public AudioTrack getAudioTrack()
    {
        return audioTrack;
    }

    /**
     * Gets the ID of the user who queued this track
     * @return userID
     */
    public String getUserID()
    {
        return userID;
    }
}
