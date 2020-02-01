package jara;

import commands.CmdUtil;
import configuration.GuildSettings;
import configuration.SettingsUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;

public class CommandHandler extends ListenerAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
	private static long commandCount = 0;
	private static HashMap<Integer, Integer> hourToCommandMap = new HashMap<>();

	/**
	 * Listen for valid command strings, and run the command launcher if one is found
	 * @param msgEvent the context of the message
	 */
	@Override 
	public void onGuildMessageReceived(GuildMessageReceivedEvent msgEvent) //Reads commands
	{
		if (!msgEvent.getAuthor().isBot())
		{
			GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
			String commandString = msgEvent.getMessage().getContentDisplay();
			String commandPrefix = guildSettings.getCommandPrefix().toString();

			if (commandString.startsWith(commandPrefix))
			{
				String[] command = commandString.split(" ");
				String key = command[0].replaceFirst(commandPrefix, "").toLowerCase();
				ModuleAttributes moduleAttributes = getModuleAttributes(key, guildSettings);
				if (moduleAttributes != null)
					execute(msgEvent, moduleAttributes, command);
			}
		}
	}

	/**
	 * Gets the attributes of a module or custom command
	 * @param alias the alias to find the module with
	 * @param guildSettings the guild to get custom modules from
	 * @return the attributes
	 */
	private ModuleAttributes getModuleAttributes(String alias, GuildSettings guildSettings)
	{
		ModuleAttributes moduleAttributes = ModuleManager.getModule(alias);
		if (moduleAttributes == null)
		{
			moduleAttributes = guildSettings.getCustomCommandSettings().getCommandAttributes(alias);
		}
		return moduleAttributes;
	}

	/**
	 * Instantiates the command, dealing with any exceptions that may occur.
	 * @param msgEvent context
	 * @param parameters the additional data passed with the command
	 */
	private void instantiateCommand(GuildMessageReceivedEvent msgEvent, ModuleAttributes attributes, String[] parameters)
	{
		try
		{
			attributes.getCommandClass().getConstructor().newInstance().run(msgEvent, parameters);
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			msgEvent.getChannel().sendMessage("Sorry, I was unable to run the command.").queue();
			Logger logger = LoggerFactory.getLogger(CommandHandler.class);
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
		catch (Exception e)
		{
			logger.error(attributes.getKey()+" has encountered an error in "+msgEvent.getGuild().getName()+". Details:\n", e);
		}
	}

	/**
	 * Creates a new instance of the command and runs it on a separate thread.
	 * @param msgEvent context
	 * @param parameters the additional data passed with the command
	 */
	private void execute(GuildMessageReceivedEvent msgEvent, ModuleAttributes attributes, String...parameters)
	{
		if (!(attributes.isCustomCommand() && SettingsUtil.getGlobalSettings().isModuleEnabled(attributes.getKey())) || attributes.isCustomCommand())
		{
			GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
			if (guildSettings.isChannelWhitelisted(msgEvent.getChannel()))
			{
				logCommandUsage();
				if (guildSettings.isCommandEnabled(attributes.getKey()))
				{
					if (checkForTimedAvailability(attributes, guildSettings))
					{
						if (guildSettings.isPermitted(msgEvent.getMember(), attributes.getKey()))
						{
							Runnable commandRunnable = () -> instantiateCommand(msgEvent, attributes, parameters);
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


	private void logCommandUsage()
	{
		commandCount++;
		int hoursSinceEpoch = (int) Math.floor(Instant.now().getEpochSecond()/3600.0);
		int usesThisHour = hourToCommandMap.getOrDefault(hoursSinceEpoch, 0);
		usesThisHour++;
		hourToCommandMap.put(hoursSinceEpoch, usesThisHour);
		if (hourToCommandMap.size() > 24)
		{
			hourToCommandMap.remove(hoursSinceEpoch-24);
		}
	}

	/**
	 * Gets the total command count from the active session
	 * @return the command count
	 */
	public static long getCommandCount()
	{
		return commandCount;
	}

	/**
	 * Gets a map, detailing the number of commands used in the last 24 hours.
	 * @return Map layout:<br>Key - Hour since epoch<br>Value - The number of commands used in that hour.
	 */
	public static HashMap<Integer, Integer> getCommandUsageMap()
	{
		return hourToCommandMap;
	}

}
