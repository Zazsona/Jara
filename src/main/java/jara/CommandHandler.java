package jara;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandHandler extends ListenerAdapter 
{
	CommandConfiguration[] commandRegister; //Contains details on all commands.
	public CommandHandler(CommandConfiguration[] commandRegisterArg)
	{
		commandRegister = commandRegisterArg;
	}
	
	@Override 
	public void onGuildMessageReceived(GuildMessageReceivedEvent msgEvent) //Reads commands
	{
		String commandString = msgEvent.getMessage().getContentDisplay();
		if (commandString.startsWith("/"))									   //Prefix to signify that a command is being called.
		{
			String[] command = commandString.split(" ");							   //Separating parameters.
			for (CommandConfiguration commandConfig : commandRegister)
			{
				if (commandConfig.isEnabled())
				{
					for (String alias : commandConfig.getAliases())
					{
						if (command[0].equalsIgnoreCase("/"+alias))
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
