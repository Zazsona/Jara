package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioLoadHandler implements AudioLoadResultHandler
{
    Audio audio;
    public AudioLoadHandler(Audio audio)
    {
        this.audio = audio;
    }
    @Override
    public void trackLoaded(AudioTrack track)
    {
        audio.getTrackQueue().add(track);
        if (!audio.isAudioPlayingInGuild())
        {
            audio.getPlayer().playTrack(audio.getTrackQueue().get(0));
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
        }
    }

    @Override
    public void noMatches()
    {
        //No source found
        //TODO: inform user
    }

    @Override
    public void loadFailed(FriendlyException e)
    {
        //Error
        //TODO: Inform user
        e.printStackTrace();
    }
}
