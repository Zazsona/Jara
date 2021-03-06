package module;

import commands.CmdUtil;
import configuration.GuildSettings;
import configuration.SettingsUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.GuildUnavailableException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A subclass of {@link ModuleCommand}, provides utility methods for games which respect guild settings.
 */
public abstract class ModuleGameCommand extends ModuleCommand //A base class to build commands from.
{
	/**
	 * The game channel. If game channels are disabled, this is the same channel the command was used in.
	 */
	private TextChannel gameChannel;
	/**
	 * the message users react to in order to join the game channel. null if game channels are disabled.
	 */
	private Message gameMsg;

	/**
	 * A map containing the game channels, indexed by gameMsg id.
	 */
	private static HashMap<String, TextChannel> gameChannelMap;

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
	 * @param currentChannel context
	 * @param channelName the channel to create
	 * @param players the players to add to the channel on creation
	 * @return the channel to run the game in
	 */
	protected TextChannel createGameChannel(TextChannel currentChannel, String channelName, Member... players)
	{
		Guild guild = currentChannel.getGuild();
		Logger logger = LoggerFactory.getLogger(ModuleGameCommand.class);
		GuildSettings guildSettings = SettingsUtil.getGuildSettings(guild.getId());
		String gameCategoryID = guildSettings.getGameCategoryId();
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(CmdUtil.getHighlightColour(guild.getSelfMember()));
		if (!guildSettings.isGameChannelsEnabled())
		{
			return utiliseCurrentChannel(currentChannel, guildSettings, embed);
		}
		else
		{
			if (gameChannelMap == null)
			{
				gameChannelMap = new HashMap<>();
				/*
				 * The listener for reactions to gameMsgs.
				 */
				GameReactionHandler gameReactionHandler = new GameReactionHandler();
				currentChannel.getJDA().addEventListener(gameReactionHandler);
			}
			Category gameCategory = guild.getCategoryById(gameCategoryID);
			if (gameCategory != null)
			{
				return utiliseGameChannel(currentChannel, channelName, guild, logger, embed, gameCategory, players);
			}
			else
			{
				try
				{
					embed.setDescription("The game category has disappeared. Disabling game channels. You can re-enable these in the config.");
					currentChannel.sendMessage(embed.build()).queue();
					logger.info("Guild "+guild.getId()+"'s game category for the saved id doesn't exist. Removing from config...");
					guildSettings.setGameCategoryId("");
					guildSettings.setUseGameChannels(false);
				}
				catch (IOException e)
				{
					logger.error(e.toString());
				}

				return createGameChannel(currentChannel, channelName);
			}
		}
	}

	/**
	 * If a unique game channel is in use, this deletes the channel. Otherwise, it unmarks the channel as having a game running.
	 */
	protected void deleteGameChannel()
	{
		if (gameMsg != null && gameChannelMap.get(gameMsg.getId()) != null) //Basically, if this is a game channel...
		{
			if (channelTimeoutTimer != null)
				channelTimeoutTimer.cancel();

			gameChannel.sendMessage("Well played! This channel will be deleted in 30 seconds.").queue();
			try
			{
				Thread.sleep(30*1000);
			}
			catch (InterruptedException e)
			{
				LoggerFactory.getLogger(this.getClass()).error(e.toString());
			}
			gameChannel.delete().queue();
			gameChannelMap.remove(gameMsg.getId());

			EmbedBuilder embed = new EmbedBuilder();
			embed.setColor(CmdUtil.getHighlightColour(gameMsg.getGuild().getSelfMember()));
			embed.setDescription("This game has ended.");
			gameMsg.editMessage(embed.build()).queue();
			gameMsg.clearReactions().queue();
		}
		else
		{
			if (channelsRunningGames.containsKey(gameChannel.getGuild().getId()))
			{
				channelsRunningGames.get(gameChannel.getGuild().getId()).remove(gameChannel.getId());
			}
		}
		return;
	}

	/**
	 * Creates a game channel
	 * @param currentChannel the channel the command was issued in
	 * @param channelName the name of the channel to create
	 * @param guild the guild to create a channel in
	 * @param logger the logger
	 * @param embed the embed
	 * @param gameCategory the category to create a channel in
	 * @param players any players to add
	 * @return the channel
	 */
	private TextChannel utiliseGameChannel(TextChannel currentChannel, String channelName, Guild guild, Logger logger, EmbedBuilder embed, Category gameCategory, Member[] players)
	{
		try
		{
			TextChannel channel = (TextChannel) gameCategory.createTextChannel(channelName).complete();
			enableChannelTimeout(channel);
			channel.putPermissionOverride(channel.getGuild().getPublicRole()).setDeny(Permission.MESSAGE_READ).queue();
			for (Member player : players)
			{
				channel.createPermissionOverride(player).setAllow(Permission.MESSAGE_READ).queue();
			}
			sendGameMsg(currentChannel, channel);
			return channel;
		}
		catch (InsufficientPermissionException e)
		{
			embed.setDescription("A game category has been set up and exists, but I don't have permission to create channels and add players there!");
			currentChannel.sendMessage(embed.build()).queue();
			Thread.currentThread().interrupt();
			return null;
		}
		catch (GuildUnavailableException e)
		{
			logger.error("Guild "+guild.getId()+" became unavailable while trying to perform an action. Has Discord gone down?");
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e1)
			{
				logger.error(e.toString());
			}
			return createGameChannel(currentChannel, channelName);
		}
	}

	/**
	 * Starts a timer to delete the game channel after the guild's set timeout
	 * @param channel the channel to delete
	 */
	private void enableChannelTimeout(TextChannel channel)
	{
		Thread commandThread = Thread.currentThread();
		int channelTimeout = Integer.parseInt(SettingsUtil.getGuildSettings(channel.getGuild().getId()).getGameChannelTimeout());
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
						Message lastMessage = channel.retrieveMessageById(channel.getLatestMessageIdLong()).complete();
						OffsetDateTime timeToDelete = lastMessage.getTimeCreated().plusSeconds(channelTimeout/1000);
						if (!lastMessage.getTimeCreated().isBefore(timeToDelete))
						{
							deleteGameChannel();
							dispose();
							commandThread.interrupt();
						}
					}
				}
			}, channelTimeout/2, channelTimeout/2);
		}
	}

	/**
	 * Sends a message allowing players to add a reaction to join the channel
	 * @param currentChannel the channel the reactable message in in
	 * @param channel the channel to join
	 */
	private void sendGameMsg(TextChannel currentChannel, TextChannel channel)
	{
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(CmdUtil.getHighlightColour(currentChannel.getGuild().getSelfMember()));
		embed.setDescription(channel.getAsMention()+" has begun!\nReact with :game_die: to join.");
		gameMsg = currentChannel.sendMessage(embed.build()).complete();
		gameMsg.addReaction("\uD83C\uDFB2").queue();
		gameChannelMap.put(gameMsg.getId(), channel);
		gameChannel = channel;
	}

	/**
	 * Prepares the current channel for a game, and registers it as running a game.<br>
	 *     Kills the thread if multiple games in a channel is disabled and one is already running
	 * @param currentChannel the channel to use
	 * @param guildSettings the guild's settings
	 * @param embed the embed
	 * @return the TextChannel to use
	 */
	private TextChannel utiliseCurrentChannel(TextChannel currentChannel, GuildSettings guildSettings, EmbedBuilder embed)
	{
		if (guildSettings.isConcurrentGameInChannelAllowed())
		{
			Guild guild = currentChannel.getGuild();
			channelsRunningGames.putIfAbsent(guild.getId(), new ArrayList<>());
			if (channelsRunningGames.get(guild.getId()).contains(currentChannel.getId()))
			{
				embed.setDescription("There's already a game running in this channel.");
				currentChannel.sendMessage(embed.build()).queue();
				Thread.currentThread().interrupt();
				return null;
			}
			else
			{
				channelsRunningGames.get(guild.getId()).add(guild.getId());
			}
		}
		gameChannel = currentChannel;
		return currentChannel;
	}

	/**
	 * EventHandler to pick up on game join reactions.
	 */
	private class GameReactionHandler extends ListenerAdapter
	{
		@Override
		public void onMessageReactionAdd(MessageReactionAddEvent mra)
		{
			try
			{
				if (mra.getChannel().retrieveMessageById(mra.getMessageIdLong()).complete().getMember().equals(mra.getGuild().getSelfMember())) //Since this method checks all reactions, this at least limits it to bot only ones
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
