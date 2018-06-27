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
	
	public Class<? extends Command> getCommandClass()
	{
		return commandClass;
	}
	
	public void execute(GuildMessageReceivedEvent msgEvent, String...parameters)
	{
		if (isEnabled())
		{
			try
			{
				getCommandClass().newInstance().run(msgEvent, parameters);
			} catch (InstantiationException | IllegalAccessException e)
			{
				msgEvent.getChannel().sendMessage("Sorry, I was unable to run the command.").queue();
				Logger logger = LoggerFactory.getLogger(CommandConfiguration.class);
				logger.error("A command request was sent but could not be fulfilled: "+parameters.toString()); //TODO: Provide more details (Guild, user, channel, etc.)
				e.printStackTrace();
			}
		}
		else
		{
			Logger logger = LoggerFactory.getLogger(CommandConfiguration.class);
			logger.info("The command called with "+parameters[0]+" is disabled. Please enable it in the config.");
		}

	}
	
	public String[] getAliases()
	{
		return aliases;
	}
	
	public boolean isEnabled()
	{
		return enabledState;
	}
}
