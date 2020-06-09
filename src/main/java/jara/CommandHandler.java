package jara;

import commands.CmdUtil;
import configuration.GuildSettings;
import configuration.SettingsUtil;
import listeners.CommandListener;
import listeners.ListenerManager;
import module.ModuleCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandHandler extends ListenerAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
	private static final int COMMAND_TTL_SECONDS = 60*60*2; //2 hours
	private static final ConcurrentHashMap<Long, CommandInstance> commandInstanceMap = new ConcurrentHashMap<>();

	public CommandHandler()
	{
		Timer purgeTimer = new Timer();
		TimerTask tt = new TimerTask()
		{
			@Override
			public void run()
			{
				purgeInactiveCommands();
			}
		};
		purgeTimer.schedule(tt, COMMAND_TTL_SECONDS/2, COMMAND_TTL_SECONDS/2);
	}

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
			ModuleCommand command = attributes.getCommandClass().getConstructor().newInstance();
			Thread currentThread = Thread.currentThread();
			commandInstanceMap.put(currentThread.getId(), new CommandInstance(command, currentThread, Instant.now().getEpochSecond()));
			command.run(msgEvent, parameters);
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
		finally
		{
			commandInstanceMap.remove(Thread.currentThread().getId());
		}
	}

	/**
	 * Creates a new instance of the command and runs it on a separate thread.
	 * @param msgEvent context
	 * @param parameters the additional data passed with the command
	 */
	public void execute(GuildMessageReceivedEvent msgEvent, ModuleAttributes attributes, String...parameters)
	{
		if (!(attributes.isCustomCommand() && SettingsUtil.getGlobalSettings().isModuleEnabled(attributes.getKey())) || attributes.isCustomCommand())
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
							Runnable commandRunnable = () -> instantiateCommand(msgEvent, attributes, parameters);
							Thread commandThread = new Thread(commandRunnable);
							commandThread.setName(msgEvent.getGuild().getName()+"-"+attributes.getKey()+"-Thread");
							commandThread.start();
							runListeners(msgEvent, attributes, true);
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
				runListeners(msgEvent, attributes, false);
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

	private void runListeners(GuildMessageReceivedEvent msgEvent, ModuleAttributes moduleAttributes, boolean commandExecutionSuccessful)
	{
		ConcurrentLinkedQueue<CommandListener> listeners = ListenerManager.getCommandListeners();
		if (listeners.size() > 0)
		{
			new Thread(() ->
					   {
						   if (commandExecutionSuccessful)
							   listeners.forEach((v) -> v.onCommandSuccess(msgEvent, moduleAttributes));
						   else
							   listeners.forEach((v) -> v.onCommandFailure(msgEvent, moduleAttributes));
					   }).start();
		}
	}

	private void purgeInactiveCommands()
	{
		long currentSecond = Instant.now().getEpochSecond();
		Iterator<Map.Entry<Long, CommandInstance>> iterator = commandInstanceMap.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<Long, CommandInstance> entry = iterator.next();
			if (currentSecond-entry.getValue().getStartSecond() > COMMAND_TTL_SECONDS)
			{
				entry.getValue().kill();
				iterator.remove();
			}
		}
	}

	private class CommandInstance
	{
		private ModuleCommand command;
		private Thread thread;
		private long startSecond;

		public CommandInstance(ModuleCommand command, Thread thread, long startSecond)
		{
			this.command = command;
			this.thread = thread;
			this.startSecond = startSecond;
		}

		private long getStartSecond()
		{
			return startSecond;
		}

		private long getId()
		{
			return thread.getId();
		}

		private boolean isCommandActive()
		{
			return thread.isAlive();
		}

		private void kill()
		{
			command.dispose();
			thread.interrupt();
			synchronized (commandInstanceMap)
			{
				commandInstanceMap.remove(getId());
			}
		}
	}

}
