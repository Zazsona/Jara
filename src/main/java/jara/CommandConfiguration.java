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
	 * 
	 * @return
	 * Class<? extends Command> - The comand class.
	 */
	public Class<? extends Command> getCommandClass()
	{
		return commandClass;
	}
	/**
	 *Creates a new instance of the command and runs it on a separate thread.
	 *
	 *@param
	 *msgEvent - Used to context
	 *parameters - Command parameters
	 */
	public void execute(GuildMessageReceivedEvent msgEvent, String...parameters)
	{
		if (isEnabled())
		{
			Runnable commandRunnable = () -> {
				try
				{
					getCommandClass().newInstance().run(msgEvent, parameters);
				}
				catch (InstantiationException | IllegalAccessException e)
				{
					msgEvent.getChannel().sendMessage("Sorry, I was unable to run the command.").queue();
					Logger logger = LoggerFactory.getLogger(CommandConfiguration.class);
					logger.error("A command request was sent but could not be fulfilled: "+parameters.toString()); //TODO: Provide more details (Guild, user, channel, etc.)
					e.printStackTrace();
				}
			};
			Thread commandThread = new Thread(commandRunnable);
			commandThread.setName(aliases[0]+"-Thread"); //TODO: Reimplement failure feedback
			commandThread.start();
		}
		else
		{
			Logger logger = LoggerFactory.getLogger(CommandConfiguration.class);
			logger.info("The command called with "+parameters[0]+" is disabled. Please enable it in the config.");
		}

	}
	/**
	 * 
	 * Simple get for all the different text strings
	 * that will call the command.
	 * 
	 * @return
	 * String[] - List of all command aliases
	 */
	public String[] getAliases()
	{
		return aliases;
	}
	/**
	 * 
	 * Reports back if the command is enabled in the
	 * config. If this returns false, execute will also fail.
	 * 
	 * @return
	 * true - Command is enabled
	 * false - Command is disabled
	 */
	public boolean isEnabled()
	{
		return enabledState;
	}
}
