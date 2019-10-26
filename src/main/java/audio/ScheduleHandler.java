package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import configuration.SettingsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleHandler extends AudioEventAdapter
{
    /**
     * The audio instance for this guild
     */
    private Audio audio;

    /**
     * Constructor
     * @param audio the {@link Audio} to report back to
     */
    public ScheduleHandler(Audio audio)
    {
        this.audio = audio;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track)
    {
    }
    @Override
    public void onPlayerPause(AudioPlayer player)
    {
        //TODO: Add a configurable timer here where after so long the track is skipped / let users to a majority vote skip/resume.
    }
    @Override
    public void onPlayerResume(AudioPlayer player)
    {
        //Audio resumed. Don't feel we really need to do anything special here. People will probably notice.
    }
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason reason)
    {
        audio.resetSkipVotes();
        String userID = audio.getTrackQueue().get(0).getUserID();
        audio.getUserQueueQuantity().replace(userID, audio.getUserQueueQuantity().get(userID)-1);
        audio.getTrackHistory().add(audio.getTrackQueue().get(0));
        audio.getTrackQueue().remove(0); //Remove the track we just played
        if (audio.getTrackQueue().size() > 0)
        {
            audio.getPlayer().playTrack(audio.getTrackQueue().get(0).getAudioTrack());
        }
        else if (SettingsUtil.getGuildSettings(audio.getAudioManager().getGuild().getId()).isVoiceLeavingEnabled())
        {
            new Thread(() -> audio.getAudioManager().closeAudioConnection()).start();
        }

    }
    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs)
    {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("AudioTrack "+track.getInfo().uri + "("+track.getInfo().title+") has become stuck. Starting next track.");
        String userID = audio.getTrackQueue().get(0).getUserID();
        audio.getUserQueueQuantity().replace(userID, audio.getUserQueueQuantity().get(userID)-1);
        audio.getTrackQueue().remove(0); //Remove the track we just tried to play
        audio.resetSkipVotes();
        if (audio.getTrackQueue().size() > 0)
        {
            audio.getPlayer().playTrack(audio.getTrackQueue().get(0).getAudioTrack());
        }
        else if (SettingsUtil.getGuildSettings(audio.getAudioManager().getGuild().getId()).isVoiceLeavingEnabled())
        {
            new Thread(() -> audio.getAudioManager().closeAudioConnection()).start();
        }
    }
}
