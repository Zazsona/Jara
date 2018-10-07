package jara;

import configuration.CommandConfiguration;
import configuration.SettingsUtil;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandHandler extends ListenerAdapter 
{
	CommandConfiguration[] commandConfigs; //Contains details on all commands.
	public CommandHandler(CommandConfiguration[] commandConfigsArg)
	{
		commandConfigs = commandConfigsArg.clone();
	}
	
	@Override 
	public void onGuildMessageReceived(GuildMessageReceivedEvent msgEvent) //Reads commands
	{
		String commandString = msgEvent.getMessage().getContentDisplay();
		String commandPrefix = SettingsUtil.getGuildCommandPrefix(msgEvent.getGuild().getId()).toString();
		if (commandString.startsWith(commandPrefix))									   //Prefix to signify that a command is being called.
		{
			String[] command = commandString.split(" ");							   //Separating parameters.
			for (CommandConfiguration commandConfig : commandConfigs)
			{
				if (commandConfig.isEnabled())								//While execute() does an enabled check anyway, this saves us iterating through aliases.
				{
					for (String alias : commandConfig.getAliases())
					{
						if (command[0].equalsIgnoreCase(commandPrefix+alias))
						{
							commandConfig.execute(msgEvent, command);
							return;
						}
					}
				}
			}
			
		}

	}
}
