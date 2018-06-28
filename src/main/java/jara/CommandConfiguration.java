package jara;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commands.Command;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class CommandConfiguration
{
	private boolean enabledState; //Whether the config allows this command to be used
	private String[] aliases; //Text strings that will call the command
	private Class<? extends Command> commandClass;
	
	public CommandConfiguration(boolean enabledStateArg, String[] aliasesArg, Class<? extends Command> commandClassArg)
	{
		enabledState = enabledStateArg;
		aliases = aliasesArg;
		commandClass = commandClassArg;
	}
	/**
	 * 
	 * Simple get method for the command's corresponding class.
	 * Do not use this method for instantiating a command as it
	 * does not perform an enabled check. 
	 * 
	 * Instead, use execute()
	 */
	public Class<? extends Command> getCommandClass()
	{
		return commandClass;
	}
	/**
	 * 
	 * @param msgEvent
	 * @param parameters
	 * 
	 * Creates a new instance of the command and runs it.
	 * 
	 * Returns:
	 * true - Command instantiated successfully
	 * false - Error occurred. Check the console for details. Command is not run.
	 */
	public boolean execute(GuildMessageReceivedEvent msgEvent, String...parameters)
	{
		if (isEnabled())
		{
			try
			{
				getCommandClass().newInstance().run(msgEvent, parameters);
				return true;
			} 
			catch (InstantiationException | IllegalAccessException e)
			{
				msgEvent.getChannel().sendMessage("Sorry, I was unable to run the command.").queue();
				Logger logger = LoggerFactory.getLogger(CommandConfiguration.class);
				logger.error("A command request was sent but could not be fulfilled: "+parameters.toString()); //TODO: Provide more details (Guild, user, channel, etc.)
				e.printStackTrace();
				return false;
			}
		}
		else
		{
			Logger logger = LoggerFactory.getLogger(CommandConfiguration.class);
			logger.info("The command called with "+parameters[0]+" is disabled. Please enable it in the config.");
			return false;
		}

	}
	/**
	 * 
	 * Simple get for all the different text strings
	 * that will call the command.
	 */
	public String[] getAliases()
	{
		return aliases;
	}
	/**
	 * 
	 * Reports back if the command is enabled in the
	 * config. If this returns false, execute will also fail.
	 */
	public boolean isEnabled()
	{
		return enabledState;
	}
}
