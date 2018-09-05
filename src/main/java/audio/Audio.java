package audio;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.ArrayList;


public class Audio
{
	private static ArrayList<String> guildsPlayingAudio = new ArrayList<>();
	private ArrayList<AudioTrack> trackQueue = new ArrayList<>();
	private AudioPlayerManager playerManager;
	private AudioPlayer player;
	private AudioManager audioManager;
	private Guild guild;

	public static final byte REQUEST_PENDING = 0;
	public static final byte REQUEST_NOW_PLAYING = 1;
	public static final byte REQUEST_ADDED_TO_QUEUE = 2;
	public static final byte REQUEST_RESULTED_IN_ERROR = 3;
	public static final byte REQUEST_USER_NOT_IN_VOICE = 4;
	public static final byte REQUEST_IS_BAD = 5;

	public Audio(Guild guild)
	{
		this.guild = guild;

		playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);
		player = playerManager.createPlayer();

		audioManager = guild.getAudioManager();
		audioManager.setSendingHandler(new AudioSendingHandler(player));

		ScheduleHandler scheduleHandler = new ScheduleHandler(this);
		player.addListener(scheduleHandler);
	}

	public byte play(Member member, String query)
	{
		try
		{
			VoiceChannel channel = member.getVoiceState().getChannel();
			if (channel != null)
			{
				if (!isAudioPlayingInGuild())
				{
					audioManager.openAudioConnection(channel);
				}
				AudioLoadHandler audioLoadHandler = new AudioLoadHandler(this);
				playerManager.loadItem(query, audioLoadHandler); //If audio is already playing, it will be added to the queue instead.
				while (audioLoadHandler.getResult() == REQUEST_PENDING)
				{
					Thread.sleep(3); //TODO: This, more elegantly.
				}
				return audioLoadHandler.getResult();
			}
			return REQUEST_USER_NOT_IN_VOICE;
		}
		catch (Exception e)
		{
			//Error setting up audio
			e.printStackTrace();
			return REQUEST_RESULTED_IN_ERROR;
		}
	}

	public static Boolean isAudioPlayingInGuild(String guildID)
	{
		return guildsPlayingAudio.contains(guildID);
	}
	public Boolean isAudioPlayingInGuild()
	{
		return guildsPlayingAudio.contains(guild.getId());
	}
	public void setIsAudioPlayingInGuild(Boolean state)
	{
		setIsAudioPlayingInGuild(guild.getId(), state);
	}
	public static void setIsAudioPlayingInGuild(String guildID, Boolean state)
	{
		if (state)
		{
			if (!guildsPlayingAudio.contains(guildID))
			{
				guildsPlayingAudio.add(guildID);
			}
		}
		else
		{
			guildsPlayingAudio.remove(guildID);
		}
	}
	public ArrayList<AudioTrack> getTrackQueue()
	{
		return trackQueue;
	}
	public AudioPlayer getPlayer()
	{
		return player;
	}
	public AudioManager getAudioManager()
	{
		return audioManager;
	}
	public long getTotalQueuePlayTime()
	{
		long time = 0;
		for (AudioTrack audioTrack : getTrackQueue())
		{
			time += audioTrack.getInfo().length;
		}
		return time;
	}





}
