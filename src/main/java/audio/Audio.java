package audio;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.ArrayList;


public class Audio
{
	private ArrayList<AudioTrack> trackQueue = new ArrayList<>();
	private final AudioPlayerManager playerManager;
	private final AudioPlayer player;
	private final AudioManager audioManager;

	public enum RequestResult
	{
		REQUEST_PENDING,
		REQUEST_NOW_PLAYING,
		REQUEST_NO_LINK,
		REQUEST_ADDED_TO_QUEUE,
		REQUEST_RESULTED_IN_ERROR,
		REQUEST_USER_NOT_IN_VOICE,
		REQUEST_IS_BAD,
        REQUEST_CHANNEL_FULL,
		REQUEST_CHANNEL_PERMISSION_DENIED
	}

	private ArrayList<String> skipVotes = new ArrayList<>(); //This stores the user IDs of those who have voted to skip (Prevents the same user voting multiple times)

	public Audio(Guild guild)
	{
		playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
		player = playerManager.createPlayer();

		audioManager = guild.getAudioManager();
		audioManager.setSendingHandler(new AudioSendingHandler(player));

		ScheduleHandler scheduleHandler = new ScheduleHandler(this);
		player.addListener(scheduleHandler);
	}

	public RequestResult play(Member member, String query)
	{
		try
		{
			VoiceChannel channel = member.getVoiceState().getChannel();
			if (channel != null)
			{
			    if (channel.getUserLimit() != 0 && channel.getUserLimit() > channel.getMembers().size())
                {
                    return RequestResult.REQUEST_CHANNEL_FULL;
                }
				if (!isAudioPlayingInGuild())
				{
					try
					{
						audioManager.openAudioConnection(channel);
					}
					catch (InsufficientPermissionException e)
					{
						return RequestResult.REQUEST_CHANNEL_PERMISSION_DENIED;
					}

				}
				AudioLoadHandler audioLoadHandler = new AudioLoadHandler(this);
				playerManager.loadItem(query, audioLoadHandler); //If audio is already playing, it will be added to the queue instead.
				while (audioLoadHandler.getResult() == RequestResult.REQUEST_PENDING)
				{
					Thread.sleep(3); //TODO: This, more elegantly.
				}
				return audioLoadHandler.getResult();
			}
			return RequestResult.REQUEST_USER_NOT_IN_VOICE;
		}
		catch (Exception e)
		{
			//Error setting up audio
			e.printStackTrace();
			return RequestResult.REQUEST_RESULTED_IN_ERROR;
		}
	}

	public Boolean isAudioPlayingInGuild()
	{
		return (getPlayer().getPlayingTrack() != null);
	}

	/**
	 * Returns the currently queued tracks. The currently playing track is at index 0.
	 * @return
	 * ArrayList<AudioTrack> - The tracks.
	 */
	public ArrayList<AudioTrack> getTrackQueue()
	{
		return trackQueue;
	}

	/**
	 * Returns this guild's audio player.
	 * @return
	 * AudioPlayer - the player
	 */
	public AudioPlayer getPlayer()
	{
		return player;
	}

	/**
	 * Calculates the total run time of all songs in the queue.
	 * @return
	 * long - The total run time in ms.
	 */
	public long getTotalQueuePlayTime()
	{
		long time = 0;
		for (AudioTrack audioTrack : getTrackQueue())
		{
			time += audioTrack.getInfo().length;
		}
		return time;
	}

	/**
	 * Gets the current number of users who have voted to skip the current track.
	 * @return
	 * int - The # of skip votes
	 */
	public int getSkipVotes()
	{
		return skipVotes.size();
	}

	/**
	 * Inverts the user's current vote state.
	 * @param userID
	 * @return
	 * true - Vote added
	 * false - Vote removed
	 */
	public boolean registerSkipVote(String userID)
	{
		if (skipVotes.contains(userID))
		{
			skipVotes.remove(userID);
			return false;
		}
		else
		{
			skipVotes.add(userID);
			return true;
		}
	}

	/**
	 * Creates a new ArrayList to count skip votes, de-referencing the old one.
	 */
	public void resetSkipVote()
	{
		skipVotes = new ArrayList<>();
	}

	/**
	 * Gets AudioManager
	 * @return
	 */
	public AudioManager getAudioManager()
	{
		return audioManager;
	}






}
