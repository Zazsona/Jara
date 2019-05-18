package audio;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import commands.CmdUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.ArrayList;


public class Audio
{
	/**
	 * The list of tracks waiting to be played, including that currently playing
	 */
	private ArrayList<AudioTrack> trackQueue = new ArrayList<>();
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
		REQUEST_CHANNEL_PERMISSION_DENIED
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
				AudioLoadHandler audioLoadHandler = new AudioLoadHandler(this);
				playerManager.loadItem(query, audioLoadHandler); //If audio is already playing, it will be added to the queue instead.
				while (audioLoadHandler.getResult() == RequestResult.REQUEST_PENDING)
				{
					Thread.sleep(3);
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
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(CmdUtil.getHighlightColour(channel.getGuild().getSelfMember()));
		embed.setThumbnail(embedImageURL);
		embed.setTitle(embedTitle);
		StringBuilder descBuilder = new StringBuilder();
		AudioTrackInfo requestedTrackInfo = null;

		if (query != null && !query.equals(""))
		{
			result = play(member, query);
			if (result == RequestResult.REQUEST_NOW_PLAYING || result == RequestResult.REQUEST_ADDED_TO_QUEUE)
				requestedTrackInfo = getTrackQueue().get(getTrackQueue().size()-1).getInfo(); //This always returns the last track (i.e, the one that was just requested)
		}
		else
		{
			result = RequestResult.REQUEST_NO_LINK;
		}

		switch (result)
		{
			case REQUEST_NOW_PLAYING:
				embed.setDescription(CmdUtil.formatAudioTrackDetails(getTrackQueue().get(0)));
				break;

			case REQUEST_ADDED_TO_QUEUE:
				descBuilder.append("Your request has been added to the queue.\n");
				descBuilder.append("Position: "+getTrackQueue().size()+"\n"); //So, index 1 is position 2, 2 is 3, etc. Should be more readable for non-programmers.
				descBuilder.append("ETA: "+(((getTotalQueuePlayTime()-requestedTrackInfo.length)/1000)/60)+" Minutes\n"); //This ETA is really rough. It abstracts to minutes and ignores the progress of the current track.
				descBuilder.append("=====\n");
				descBuilder.append(CmdUtil.formatAudioTrackDetails(getTrackQueue().get(getTrackQueue().size()-1)));
				embed.setDescription(descBuilder.toString());
				break;

			case REQUEST_RESULTED_IN_ERROR:
				embed.setTitle("Error");
				channel.sendMessage("An unexpected error occurred. Please try again. If the error persists, please notify your server owner.").queue();
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

			default:
				embed.setTitle("Uh-Oh!");
				embed.setDescription("An unexpected event occurred. You may experience some issues.");
				break;
		}
		channel.sendMessage(embed.build()).queue();
	}

	/**
	 * Checks to see if audio is playing in the guild
	 * @return
	 * true - Audio playing<br>
	 * false - Audio not playing
	 */
	public Boolean isAudioPlayingInGuild()
	{
		return (getPlayer().getPlayingTrack() != null);
	}

	/**
	 * Returns the currently queued tracks. The currently playing track is at index 0.
	 * @return AudioTrack ArrayList
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
	 * @param userID The user who has voted
	 * @return
	 * true - Vote added<br>
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
	public void resetSkipVotes()
	{
		skipVotes = new ArrayList<>();
	}

	/**
	 * @return AudioManager - The guild's audio manager
	 */
	public AudioManager getAudioManager()
	{
		return audioManager;
	}






}
