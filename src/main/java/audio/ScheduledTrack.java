package audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class ScheduledTrack
{
    private AudioTrack audioTrack;
    private String userID;

    public ScheduledTrack(AudioTrack at, String userID)
    {
        this.audioTrack = at;
        this.userID = userID;
    }

    public AudioTrack getAudioTrack()
    {
        return audioTrack;
    }

    /**
     * Gets userID
     *
     * @return userID
     */
    public String getUserID()
    {
        return userID;
    }
}
