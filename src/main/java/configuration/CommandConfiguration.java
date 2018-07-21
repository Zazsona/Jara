package configuration;

import java.time.LocalDateTime;

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
			GuildSettingsManager guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId());
			if (guildSettings.getGuildCommandEnabledStatus(aliases[0]) && (msgEvent.getMember().isOwner() || guildSettings.hasPermission(msgEvent, getCommandClass()))) //The first alias should always match the command key.
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
						logger.error("A command request was sent but could not be fulfilled.\nCommand: "+parameters.toString()+"\nGuild: "+msgEvent.getGuild().getId()+" ("+msgEvent.getGuild().getName()+")\nUser: "+msgEvent.getAuthor().getName()+"#"+msgEvent.getAuthor().getDiscriminator()+"Channel: "+msgEvent.getChannel().getId()+" ("+msgEvent.getChannel().getName()+")\nDate/Time: "+LocalDateTime.now().toString());
						e.printStackTrace();
					}
				};
				Thread commandThread = new Thread(commandRunnable);
				commandThread.setName(aliases[0]+"-Thread");
				commandThread.start();
				return;
			}
			else
			{
				msgEvent.getChannel().sendMessage("This command is disabled. Please talk to your guild owner if you wish to have it enabled.").queue();
			}
			
		}
		else
		{
			Logger logger = LoggerFactory.getLogger(CommandConfiguration.class);
			logger.info(msgEvent.getAuthor().getName()+"#"+msgEvent.getAuthor().getDiscriminator()+" attempted to use the command "+parameters[0]+", however it's disabled. Please enable it in the config.");
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
