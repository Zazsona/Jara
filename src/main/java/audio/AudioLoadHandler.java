package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import configuration.SettingsUtil;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.LoggerFactory;

public class AudioLoadHandler implements AudioLoadResultHandler
{
    /**
     * The Audio instance for this guild
     */
    private final Audio audio;
    /**
     * The ID of the user who issued the request.
     */
    private final Member member;
    /**
     * The result of the load
     */
    private Audio.RequestResult result;

    /**
     * Constructor
     * @param audio the {@link Audio} to feedback to
     * @param member the member who loaded the track
     */
    public AudioLoadHandler(Audio audio, Member member)
    {
        this.audio = audio;
        this.member = member;
        this.result = Audio.RequestResult.REQUEST_PENDING;
    }

    /**
     * Gets the result of the last track load attempt
     * @return the result of the load
     */
    public Audio.RequestResult getResult()
    {
        return result;
    }

    @Override
    public void trackLoaded(AudioTrack track)
    {
        boolean canQueue = registerUserQueueItem();
        if (canQueue)
        {
            audio.getTrackQueue().add(new ScheduledTrack(track, member.getUser().getId()));
            if (!audio.isAudioPlayingInGuild())
            {
                audio.getPlayer().playTrack(audio.getTrackQueue().get(0).getAudioTrack());
                result = Audio.RequestResult.REQUEST_NOW_PLAYING;
            }
            else
            {
                result = Audio.RequestResult.REQUEST_ADDED_TO_QUEUE;
            }
        }
        else
        {
            result = Audio.RequestResult.REQUEST_USER_LIMITED;
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist)
    {
        int queueCount = 0;
        for (AudioTrack track : playlist.getTracks())
        {
            if (registerUserQueueItem())
            {
                audio.getTrackQueue().add(new ScheduledTrack(track, member.getUser().getId()));
                queueCount++;
            }

        }
        if (queueCount > 0)
        {
            if (!audio.isAudioPlayingInGuild())
            {
                audio.getPlayer().playTrack(audio.getTrackQueue().get(0).getAudioTrack());
                result = Audio.RequestResult.REQUEST_NOW_PLAYING;
            }
            else
            {
                result = Audio.RequestResult.REQUEST_ADDED_TO_QUEUE;
            }
        }
        else
        {
            result = Audio.RequestResult.REQUEST_USER_LIMITED;
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
        LoggerFactory.getLogger(this.getClass()).info(e.toString());
    }

    /**
     * Confirm the user is able to queue an addition track, and mark it against their quota
     * @return boolean on if they have permission to queue
     */
    private synchronized boolean registerUserQueueItem()
    {
        if (audio.getUserQueueQuantity().get(member.getUser().getId()) == null)
        {
            if (SettingsUtil.getGuildSettings(audio.getAudioManager().getGuild().getId()).getAudioQueueLimit(member) > 0)
            {
                audio.getUserQueueQuantity().put(member.getUser().getId(), 1);
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            int queueQuantity = audio.getUserQueueQuantity().get(member.getUser().getId());
            if (queueQuantity < SettingsUtil.getGuildSettings(audio.getAudioManager().getGuild().getId()).getAudioQueueLimit(member))
            {
                audio.getUserQueueQuantity().replace(member.getUser().getId(), queueQuantity+1);
                return true;
            }
            else
            {
                return false;
            }
        }
    }
}
