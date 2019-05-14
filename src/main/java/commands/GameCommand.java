package commands;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public abstract class GameCommand extends Command //A base class to build commands from.
{
	private static HashMap<String, TextChannel> gameChannelMap;
	private static GameReactionListener gameReactionListener;
	private Message gameMsg;

	/**
	 * A timer of configurable length. It counts the time since the last user interaction in the game channel. If it expires, the channel is deleted.
	 */
	private Timer channelTimeoutTimer;
	/**
	 * They key is the guildID, with the arraylist holding channel IDs.
	 *
	 * This is for holding channels currently running games, in order to enact the one game per channel configuration option.
	 * It is redundant to put "GameChannels" in here if that option is enabled, as these are deleted automatically.
	 */
	private HashMap<String, ArrayList<String>> channelsRunningGames = new HashMap<>();

	//TODO: Option to limit channel access (Private games)
		//- Perhaps give users the ability to define a list(s) of 'friends'?

	/**
	 * Returns a valid channel in which to run the game. If no valid channel is available, the thread is interrupted.
	 * This is because, without a valid channel that complies with the user's config, we cannot run the game.
	 * @param msgEvent context
	 * @param channelName the channel to create
	 * @return the channel to run the game in
	 */
	protected TextChannel createGameChannel(GuildMessageReceivedEvent msgEvent, String channelName, Member... players)
	{
		Logger logger = LoggerFactory.getLogger(GameCommand.class);
		GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
		String gameCategoryID = guildSettings.getGameCategoryId();
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
		if (!guildSettings.isGameChannelsEnabled())
		{
			if (guildSettings.isConcurrentGameInChannelAllowed())
			{
				channelsRunningGames.putIfAbsent(msgEvent.getGuild().getId(), new ArrayList<>());
				if (channelsRunningGames.get(msgEvent.getGuild().getId()).contains(msgEvent.getChannel().getId()))
				{
					embed.setDescription("There's already a game running in this channel.");
					msgEvent.getChannel().sendMessage(embed.build()).queue();
					Thread.currentThread().interrupt();
					return null;
				}
				else
				{
					channelsRunningGames.get(msgEvent.getGuild().getId()).add(msgEvent.getChannel().getId());
				}
			}
			return msgEvent.getChannel();
		}
		else
		{
			if (gameChannelMap == null)
			{
				gameChannelMap = new HashMap<>();
				gameReactionListener = new GameReactionListener();
				msgEvent.getJDA().addEventListener(gameReactionListener);
			}
			Category gameCategory = msgEvent.getGuild().getCategoryById(gameCategoryID);
			if (gameCategory != null)
			{
				try
				{
					TextChannel channel = (TextChannel) gameCategory.createTextChannel(channelName).complete();
					int channelTimeout = Integer.parseInt(SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).getGameChannelTimeout());
					if (channelTimeout != 0)
					{
						channelTimeoutTimer = new Timer();
						channelTimeoutTimer.schedule(new TimerTask()
						{
							@Override
							public void run()
							{
								if (channel.hasLatestMessage())
								{
									Message lastMessage = channel.getMessageById(channel.getLatestMessageIdLong()).complete();
									OffsetDateTime timeToDelete = lastMessage.getCreationTime().plusSeconds(channelTimeout/1000);
									if (!lastMessage.getCreationTime().isBefore(timeToDelete))
									{
										deleteGameChannel(msgEvent, channel);
									}
								}
							}
						}, channelTimeout/2, channelTimeout/2);
					}
					channel.putPermissionOverride(channel.getGuild().getPublicRole()).setDeny(Permission.MESSAGE_READ).queue();
					for (Member player : players)
					{
						channel.createPermissionOverride(player).setAllow(Permission.MESSAGE_READ).queue();
					}
					embed.setDescription(msgEvent.getMember().getEffectiveName()+" has started "+channel.getAsMention()+".\nReact with :game_die: to join.");
					gameMsg = msgEvent.getChannel().sendMessage(embed.build()).complete();
					gameMsg.addReaction("\uD83C\uDFB2").queue();
					gameChannelMap.put(gameMsg.getId(), channel);
					return channel;
				}
				catch (InsufficientPermissionException e)
				{
					embed.setDescription("A game category has been set up and exists, but I don't have permission to create channels and add players there!");
					msgEvent.getChannel().sendMessage(embed.build()).queue();
					Thread.currentThread().interrupt();
					return null;
				}
				catch (GuildUnavailableException e)
				{
					logger.error("Guild "+msgEvent.getGuild().getId()+" became unavailable while trying to perform an action. Has Discord gone down?");
					e.printStackTrace();
					try
					{
						Thread.sleep(1000);
					} 
					catch (InterruptedException e1)
					{
						e.printStackTrace(); 
					}
					return createGameChannel(msgEvent, channelName);
				}
			}
			else
			{
				try
				{
					embed.setDescription("The game category has disappeared. Disabling game channels. You can re-enable these in the config.");
					msgEvent.getChannel().sendMessage(embed.build()).queue();
					logger.info("Guild "+msgEvent.getGuild().getId()+"'s game category for the saved id doesn't exist. Removing from config...");
					guildSettings.setGameCategoryId("");
					guildSettings.setUseGameChannels(false);
					guildSettings.save();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				return createGameChannel(msgEvent, channelName);
			}
		}
	}

	/**
	 * If a unique game channel is in use, this deletes the channel. Otherwise, it unmarks the channel as having a game running.
	 * @param msgEvent context
	 * @param channel the channel to delete/unmark
	 */
	protected void deleteGameChannel(GuildMessageReceivedEvent msgEvent, TextChannel channel)
	{
		if (!channel.equals(msgEvent.getChannel())) //Basically, if this is a game channel...
		{
			if (channelTimeoutTimer != null)
				channelTimeoutTimer.cancel();

			channel.sendMessage("Well played! This channel will be deleted in 30 seconds.").queue();
			try
			{
				Thread.sleep(30*1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			channel.delete().queue();
			gameChannelMap.remove(gameMsg.getId());
			gameMsg.delete().queue();
		}
		else
		{
			if (channelsRunningGames.containsKey(msgEvent.getGuild().getId()))
			{
				channelsRunningGames.get(msgEvent.getGuild().getId()).remove(channel.getId());
			}
		}
		return;
	}

	private class GameReactionListener extends ListenerAdapter
	{
		@Override
		public void onMessageReactionAdd(MessageReactionAddEvent mra)
		{
			try
			{
				if (mra.getChannel().getMessageById(mra.getMessageIdLong()).complete().getMember().equals(mra.getGuild().getSelfMember())) //Since this method checks all reactions, this at least limits it to bot only ones
				{
					if (gameChannelMap.containsKey(mra.getMessageId()))
					{
						PermissionOverride po = gameChannelMap.get(mra.getMessageId()).getPermissionOverride(mra.getMember());
						if (po != null && !po.getAllowed().contains(Permission.MESSAGE_READ))
						{
							gameChannelMap.get(mra.getMessageId()).createPermissionOverride(mra.getMember()).setAllow(Permission.MESSAGE_READ).queue();
						}
					}
				}
			}
			catch (InsufficientPermissionException e)
			{
				mra.getChannel().sendMessage("Error: Unable to add player due to insufficient permissions.").queue();
			}
		}
	}
}
