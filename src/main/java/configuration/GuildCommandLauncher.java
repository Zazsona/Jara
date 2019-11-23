package configuration;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;

import commands.CmdUtil;
import jara.SeasonalModuleAttributes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jara.ModuleAttributes;

public class GuildCommandLauncher
{
	private static Logger logger = LoggerFactory.getLogger(GuildCommandLauncher.class);
	protected final ModuleAttributes attributes;

	/**
	 * Constructor
	 * @param attributes the attributes of the module this launches
	 */
	public GuildCommandLauncher(ModuleAttributes attributes)
	{
		this.attributes = attributes;
	}

	/**
	 * Creates a new instance of the command and runs it on a separate thread.
	 * @param msgEvent context
	 * @param parameters the additional data passed with the command
	 */
	public void execute(GuildMessageReceivedEvent msgEvent, String...parameters)
	{
		GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
		if (guildSettings.isChannelWhitelisted(msgEvent.getChannel()))
		{
			if (guildSettings.isCommandEnabled(attributes.getKey()))
			{
				if (checkForTimedAvailability(attributes, guildSettings))
				{
					if (guildSettings.isPermitted(msgEvent.getMember(), attributes.getKey()))
					{
						Runnable commandRunnable = () -> instantiateCommand(msgEvent, parameters);
						Thread commandThread = new Thread(commandRunnable);
						commandThread.setName(msgEvent.getGuild().getName()+"-"+attributes.getKey()+"-Thread");
						commandThread.start();
						return;
					}
					else
					{
						msgEvent.getChannel().sendMessage("You do not have permission to use this command.").queue();
					}
				}
				else
				{
					msgEvent.getChannel().sendMessage("This seasonal command is out of season.").queue();
				}
			}
			else
			{
				msgEvent.getChannel().sendMessage("This command is disabled.").queue();
			}
		}
	}

	/**
	 * Instantiates the command, dealing with any exceptions that may occur.
	 * @param msgEvent context
	 * @param parameters the additional data passed with the command
	 */
	private void instantiateCommand(GuildMessageReceivedEvent msgEvent, String[] parameters)
	{
		try
		{
			attributes.getCommandClass().newInstance().run(msgEvent, parameters);
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			msgEvent.getChannel().sendMessage("Sorry, I was unable to run the command.").queue();
			Logger logger = LoggerFactory.getLogger(GuildCommandLauncher.class);
			logger.error("A command request was sent but could not be fulfilled.\nCommand: "+ Arrays.toString(parameters) +"\nGuild: "+msgEvent.getGuild().getId()+" ("+msgEvent.getGuild().getName()+")\nUser: "+msgEvent.getAuthor().getName()+"#"+msgEvent.getAuthor().getDiscriminator()+"Channel: "+msgEvent.getChannel().getId()+" ("+msgEvent.getChannel().getName()+")\nDate/Time: "+ LocalDateTime.now().toString()+"\n\nError: \n"+e.toString());
		}
		catch (NoSuchMethodError e)
		{
			logger.error("User attempted command "+parameters[0]+ ", but it is using an older API version, and is not supported.\n"+e.toString());
			EmbedBuilder embedBuilder = new EmbedBuilder();
			embedBuilder.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
			embedBuilder.setDescription("This command module is outdated and cannot properly function.\nIt is recommended to disable this command.");
			msgEvent.getChannel().sendMessage(embedBuilder.build()).queue();
		}
	}

	/**
	 * Checks if this command has date/time limits on when it is available, and if so, if we are within those limits.
	 * @param ma the module's attributes
	 * @param guildSettings the guild's settings
	 * @return true on available
	 */
	private boolean checkForTimedAvailability(ModuleAttributes ma, GuildSettings guildSettings)
	{
		if (ma instanceof SeasonalModuleAttributes)
		{
			ZonedDateTime zdt = ZonedDateTime.now(guildSettings.getTimeZoneId());
			SeasonalModuleAttributes sma = (SeasonalModuleAttributes) ma;
			return sma.isActive(zdt);
		}
		else
		{
			return true;
		}
	}

	/**
	 * Gets the attributed for the module this launches.
	 * @return the attributes
	 */
	public ModuleAttributes getModuleAttributes()
	{
		return attributes;
	}

}
