package commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jara.GuildSettingsManager;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;

public abstract class Command //A base class to build commands from.
{
	public abstract void run(GuildMessageReceivedEvent msgEvent, String... parameters);
	
	protected TextChannel createGameChannel(GuildMessageReceivedEvent msgEvent, String channelName)
	{
		Logger logger = LoggerFactory.getLogger(Command.class);
		GuildSettingsManager guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId());
		String gameCategoryID = guildSettings.getGuildGameCategoryID();
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
				guildSettings.setGuildGameCategoryID(gameCategoryID);
				return createGameChannel(msgEvent, channelName);
			}
		}
	}
}
