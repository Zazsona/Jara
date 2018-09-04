package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioLoadHandler implements AudioLoadResultHandler
{
    Audio audio;
    public byte result;
    public AudioLoadHandler(Audio audio)
    {
        this.audio = audio;
        this.result = audio.REQUEST_PENDING;
    }

    public byte getResult()
    {
        return result;
    }
    @Override
    public void trackLoaded(AudioTrack track)
    {
        audio.getTrackQueue().add(track);
        if (!audio.isAudioPlayingInGuild())
        {
            audio.getPlayer().playTrack(audio.getTrackQueue().get(0));
            result = audio.REQUEST_NOW_PLAYING;
        }
        result = audio.REQUEST_ADDED_TO_QUEUE;
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist)
    {
        for (AudioTrack track : playlist.getTracks())
        {
            audio.getTrackQueue().add(track);
        }
        if (!audio.isAudioPlayingInGuild())
        {
            audio.getPlayer().playTrack(audio.getTrackQueue().get(0));
            result = audio.REQUEST_NOW_PLAYING;
        }
        result = audio.REQUEST_ADDED_TO_QUEUE;
    }

    @Override
    public void noMatches()
    {
        //No source found
        result = audio.REQUEST_IS_BAD;
    }

    @Override
    public void loadFailed(FriendlyException e)
    {
        //Error
        result = audio.REQUEST_RESULTED_IN_ERROR;
        e.printStackTrace();
    }
}
