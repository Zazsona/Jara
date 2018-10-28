package commands;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
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
	private HashMap<String, ArrayList<String>> channelsRunningGames;

	//TODO: Option to limit channel access (Private games)
		//- Perhaps give users the ability to define a list(s) of 'friends'?

	/**
	 * Returns a valid channel in which to run the game. If no valid channel is available, the thread is interrupted.
	 * This is because, without a valid channel that complies with the user's config, we cannot run the game.
	 * @param msgEvent context
	 * @param channelName the channel to create
	 * @return the channel to run the game in
	 */
	protected TextChannel createGameChannel(GuildMessageReceivedEvent msgEvent, String channelName)
	{
		Logger logger = LoggerFactory.getLogger(GameCommand.class);
		GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
		String gameCategoryID = guildSettings.getGameCategoryId();
		if (!guildSettings.isGameChannelsEnabled())
		{
			if (guildSettings.isConcurrentGameInChannelAllowed())
			{
				channelsRunningGames.putIfAbsent(msgEvent.getGuild().getId(), new ArrayList<>());
				if (channelsRunningGames.get(msgEvent.getGuild().getId()).contains(msgEvent.getChannel().getId()))
				{
					msgEvent.getChannel().sendMessage("There's already a game running in this channel.").queue();
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
			Category gameCategory = msgEvent.getGuild().getCategoryById(gameCategoryID);
			if (gameCategory != null)
			{
				try
				{
					TextChannel channel = (TextChannel) gameCategory.createTextChannel(channelName).complete();
					msgEvent.getChannel().sendMessage("Game started in "+channel.getAsMention()).queue();
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
					return channel;
				}
				catch (InsufficientPermissionException e)
				{
					msgEvent.getChannel().sendMessage("A game category has been set up and exists, but I don't have permission to create channels there!").queue();
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
					msgEvent.getChannel().sendMessage("The game category has disappeared. Disabling game channels. You can re-enable these in the config.").queue();
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
}
