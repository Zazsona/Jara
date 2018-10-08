package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioLoadHandler implements AudioLoadResultHandler
{
    private final Audio audio;
    private Audio.RequestResult result;

    public AudioLoadHandler(Audio audio)
    {
        this.audio = audio;
        this.result = Audio.RequestResult.REQUEST_PENDING;
    }

    public Audio.RequestResult getResult()
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
            result = Audio.RequestResult.REQUEST_NOW_PLAYING;
        }
        else
        {
            result = Audio.RequestResult.REQUEST_ADDED_TO_QUEUE;
        }

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
            result = Audio.RequestResult.REQUEST_NOW_PLAYING;
        }
        else
        {
            result = Audio.RequestResult.REQUEST_ADDED_TO_QUEUE;
        }

    }

    @Override
    public void noMatches()
    {
        //No source found
        result = Audio.RequestResult.REQUEST_IS_BAD;
    }

    @Override
    public void loadFailed(FriendlyException e)
    {
        //Error
        result = Audio.RequestResult.REQUEST_RESULTED_IN_ERROR;
        e.printStackTrace();
    }
}
