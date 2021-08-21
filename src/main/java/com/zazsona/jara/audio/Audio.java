package com.zazsona.jara.audio;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.zazsona.jara.commands.CmdUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class Audio
{
    /**
* A history of played tracks.
     */
    private final ArrayList<ScheduledTrack> trackHistory = new ArrayList<>();
	/**
	 * The list of tracks waiting to be played, including that currently playing
	 */
	private final ArrayList<ScheduledTrack> trackQueue = new ArrayList<>();
	/**
	 * The number of tracks a user has queued.
	 */
	private final HashMap<String, Integer> userQueueQuantity = new HashMap<>();
	/**
	 * The guild's player manager
	 */
	private final AudioPlayerManager playerManager;
	/**
	 * The guild's player
	 */
	private final AudioPlayer player;
	/**
	 * The guild's audio manager
	 */
	private final AudioManager audioManager;
	/**
	 * This stores the user IDs of those who have voted to skip (Prevents the same user voting multiple times)
	 */
	private ArrayList<String> skipVotes = new ArrayList<>();

	/**
	 * The result of a play Request.
	 */
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
		REQUEST_CHANNEL_PERMISSION_DENIED,
		REQUEST_USER_LIMITED
	}

	/**
	 * Constructor.
	 *
	 * @param guild The guild to associate with this audio instance
	 */
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

	/**
	 * Plays the requested track to the member
	 * @param member The user to play the track to
	 * @param query The track to play
	 * @return RequestResult - The result of the request
	 */
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
				AudioLoadHandler audioLoadHandler = new AudioLoadHandler(this, member);
				playerManager.loadItem(query, audioLoadHandler); //If audio is already playing, it will be added to the queue instead.
				while (audioLoadHandler.getResult() == RequestResult.REQUEST_PENDING)
				{
					Thread.sleep(3);
				}
				return audioLoadHandler.getResult();
			}
			return RequestResult.REQUEST_USER_NOT_IN_VOICE;
		}
		catch (FriendlyException | InterruptedException e)
		{
			//Error setting up audio
			LoggerFactory.getLogger(this.getClass()).error(e.toString());
			return RequestResult.REQUEST_RESULTED_IN_ERROR;
		}
	}
	/**
	 * Plays the requested track to the member, and handles informing the user about the result of their request (Queue position, invalid URL, not in VC, etc.)
	 * @param member The user to play the track to
	 * @param query The track to play
	 * @param channel the channel to feed back to the user in
	 */
	public void playWithFeedback(Member member, String query, TextChannel channel)
	{
		playWithFeedback(member, query, channel, "Audio", "https://i.imgur.com/wHdSqH5.png");
	}
	/**
	 * Plays the requested track to the member, and handles informing the user about the result of their request (Queue position, invalid URL, not in VC, etc.)
	 * @param member The user to play the track to
	 * @param query The track to play
	 * @param channel the channel to feed back to the user in
	 * @param embedTitle the title to display on the embed
	 * @param embedImageURL a URL of an image to add to the embed.
	 */
	public void playWithFeedback(Member member, String query, TextChannel channel, String embedTitle, String embedImageURL)
	{
		RequestResult result;
		AudioTrackInfo requestedTrackInfo = null;

		if (query != null && !query.equals(""))
		{
			result = play(member, query);
			if (result == RequestResult.REQUEST_NOW_PLAYING || result == RequestResult.REQUEST_ADDED_TO_QUEUE)
				requestedTrackInfo = getTrackQueue().get(getTrackQueue().size()-1).getAudioTrack().getInfo(); //This always returns the last track (i.e, the one that was just requested)
		}
		else
		{
			result = RequestResult.REQUEST_NO_LINK;
		}

		sendAudioFeedback(channel, embedTitle, embedImageURL, result, requestedTrackInfo);
	}

	/**
	 * Sends a standardised error/success message, based on the provided request result
	 * @param channel the channel to send the error to
	 * @param embedTitle the embed title
	 * @param embedImageURL the embed thumbnail
	 * @param result the result of the audio request
	 * @param requestedTrackInfo the track info (can be null on error)
	 */
	public void sendAudioFeedback(TextChannel channel, String embedTitle, String embedImageURL, RequestResult result, AudioTrackInfo requestedTrackInfo)
	{
		StringBuilder descBuilder = new StringBuilder();
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(embedTitle);
		embed.setColor(CmdUtil.getHighlightColour(channel.getGuild().getSelfMember()));
		embed.setThumbnail(embedImageURL);
		switch (result)
		{
			case REQUEST_NOW_PLAYING:
				embed.setDescription(CmdUtil.formatAudioTrackDetails(getTrackQueue().get(0).getAudioTrack()));
				break;

			case REQUEST_ADDED_TO_QUEUE:
				descBuilder.append("Your request has been added to the queue.\n");
				descBuilder.append("Position: ").append(getTrackQueue().size()).append("\n"); //So, index 1 is position 2, 2 is 3, etc. Should be more readable for non-programmers.
				descBuilder.append("ETA: ").append((((getTotalQueuePlayTime() - requestedTrackInfo.length) / 1000) / 60)).append(" Minutes\n"); //This ETA is really rough. It abstracts to minutes and ignores the progress of the current track.
				descBuilder.append("=====\n");
				descBuilder.append(CmdUtil.formatAudioTrackDetails(getTrackQueue().get(getTrackQueue().size()-1).getAudioTrack()));
				embed.setDescription(descBuilder.toString());
				break;

			case REQUEST_RESULTED_IN_ERROR:
				embed.setTitle("Error");
				embed.setDescription("An unexpected error occurred. Please try again. If the error persists, please notify your server owner.");
				break;

			case REQUEST_IS_BAD:
				embed.setTitle("No Track Found");
				embed.setDescription("Sorry, but I couldn't find a track there.");
				break;

			case REQUEST_USER_NOT_IN_VOICE:
				embed.setTitle("No Voice Channel Found");
				embed.setDescription("I can't find you in any voice channels! Please make sure you're in one I have access to.");
				break;

			case REQUEST_CHANNEL_PERMISSION_DENIED:
				embed.setTitle("Permission Denied");
				embed.setDescription("I can't find you in any voice channels! Please make sure you're in one I have access to.");
				break;

			case REQUEST_CHANNEL_FULL:
				embed.setTitle("Channel Full");
				embed.setDescription("There's no space for me in that channel.");
				break;

			case REQUEST_NO_LINK:
				embed.setTitle("No Link");
				embed.setDescription("You haven't provided a valid track.");
				break;

			case REQUEST_USER_LIMITED:
				embed.setTitle("Queue Limit Reached");
				embed.setDescription("You have reached your queue limit.");
				break;

			default:
				embed.setTitle("Uh-Oh!");
				embed.setDescription("An unexpected event occurred. You may experience some issues.");
				break;
		}
		channel.sendMessage(embed.build()).queue();
	}

	/**
	 * Checks to see if audio is playing in the guild
	 * @return boolean on audio playing
	 */
	public boolean isAudioPlayingInGuild()
	{
		return (getPlayer().getPlayingTrack() != null);
	}

	/**
	 * Returns the currently scheduled tracks. The currently playing track is at index 0.
	 * @return list of queued tracks
	 */
	public ArrayList<ScheduledTrack> getTrackQueue()
	{
		return trackQueue;
	}

	/**
	 * Returns this guild's audio player.
	 * @return the player
	 */
	public AudioPlayer getPlayer()
	{
		return player;
	}

	/**
	 * Calculates the total run time of all songs in the queue.
	 * @return The total run time in ms.
	 */
	public long getTotalQueuePlayTime()
	{
		long time = 0;
		for (ScheduledTrack scheduledTrack : getTrackQueue())
		{
			time += scheduledTrack.getAudioTrack().getInfo().length;
		}
		return time;
	}

	/**
	 * Gets the current number of users who have voted to skip the current track.
	 * @return The # of skip votes
	 */
	public int getSkipVotes()
	{
		return skipVotes.size();
	}

	/**
	 * Inverts the user's current vote state.
	 * @param userID The user who has voted
	 * @return true if added, false if removed
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
	 * Clears the current skip vote count
	 */
	public void resetSkipVotes()
	{
		skipVotes.clear();
	}

	/**
	 * Gets the guild audio manager
	 * @return the {@link AudioManager}
	 */
	public AudioManager getAudioManager()
	{
		return audioManager;
	}

    /**
     * Gets the history of played tracks, including what is currently playing.<br>
     *     Note: This does not include tracks that are currently queued.
     * @return the track history
     */
	public ArrayList<ScheduledTrack> getTrackHistory()
    {
        return trackHistory;
    }

	/**
	 * Gets a the map of user id to queued track quantity.
	 * @return map of userID to queue count
	 */
	public HashMap<String, Integer> getUserQueueQuantity()
	{
		return userQueueQuantity;
	}






}
