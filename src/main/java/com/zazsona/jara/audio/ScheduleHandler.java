package com.zazsona.jara.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.zazsona.jara.configuration.SettingsUtil;
import com.zazsona.jara.listeners.AudioListener;
import com.zazsona.jara.listeners.ListenerManager;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

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
        runListeners(audio.getAudioManager().getGuild(), track, ScheduledStatus.STARTED);
    }
    @Override
    public void onPlayerPause(AudioPlayer player)
    {
        //TODO: Add a configurable timer here where after so long the track is skipped / let users to a majority vote skip/resume.
        runListeners(audio.getAudioManager().getGuild(), player.getPlayingTrack(), ScheduledStatus.PAUSED);
    }
    @Override
    public void onPlayerResume(AudioPlayer player)
    {
        runListeners(audio.getAudioManager().getGuild(), player.getPlayingTrack(), ScheduledStatus.RESUMED);
    }
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason reason)
    {
        runListeners(audio.getAudioManager().getGuild(), track, ScheduledStatus.ENDED);
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
            new Thread(() -> audio.getAudioManager().closeAudioConnection()).start(); //TODO: Why did I do this again?
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

    private enum ScheduledStatus
    {
        STARTED,
        ENDED,
        PAUSED,
        RESUMED
    }

    private void runListeners(Guild guild, AudioTrack audioTrack, ScheduledStatus scheduledStatus)
    {
        ConcurrentLinkedQueue<AudioListener> listeners = ListenerManager.getAudioListeners();
        if (listeners.size() > 0)
        {
            new Thread(() ->
                       {
                           switch (scheduledStatus)
                           {
                               case STARTED:
                                   listeners.forEach((v) -> v.onTrackStarted(guild, audioTrack));
                                   break;
                               case ENDED:
                                   listeners.forEach((v) -> v.onTrackEnded(guild, audioTrack));
                                   break;
                               case PAUSED:
                                   listeners.forEach((v) -> v.onTrackPaused(guild, audioTrack));
                                   break;
                               case RESUMED:
                                   listeners.forEach((v) -> v.onTrackResumed(guild, audioTrack));
                                   break;
                           }
                       }).start();
        }
    }
}
