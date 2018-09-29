package commands;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;

import java.io.IOException;

public abstract class Command //A base class to build commands from.
{
	public abstract void run(GuildMessageReceivedEvent msgEvent, String... parameters);
	//TODO: Add a configurable timer for how long after inactivity games should be ended / channels deleted.
	//TODO: Option to limit channel access (Private games)
		//- Perhaps give users the ability to define a list(s) of 'friends'?
	protected TextChannel createGameChannel(GuildMessageReceivedEvent msgEvent, String channelName)
	{
		Logger logger = LoggerFactory.getLogger(Command.class);
		GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
		String gameCategoryID = guildSettings.getGameCategoryId();
		if (gameCategoryID.equals(""))
		{
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
					return channel;
				}
				catch (InsufficientPermissionException e)
				{
					msgEvent.getChannel().sendMessage("A game category has been set up and exists, but I don't have permission to create channels there!").queue();
					return msgEvent.getChannel();
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
				logger.info("Guild "+msgEvent.getGuild().getId()+"'s game category for the saved id doesn't exist. Creating new category and updating id.");
				msgEvent.getGuild().getController().createCategory("Bot Games").complete();
				guildSettings.setGameCategoryId(gameCategoryID);

				try
				{
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
	protected void deleteGameChannel(GuildMessageReceivedEvent msgEvent, TextChannel channel)
	{
		if (!channel.equals(msgEvent.getChannel())) //Basically, if this is a game channel...
		{
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
		return;
	}
}
