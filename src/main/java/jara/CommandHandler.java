package jara;

import configuration.CommandLauncher;
import configuration.CustomCommandLauncher;
import configuration.GuildSettingsJson;
import configuration.SettingsUtil;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashMap;

public class CommandHandler extends ListenerAdapter 
{
	private final CommandLauncher[] commandLaunchers; //Contains details on all commands.
	public CommandHandler(CommandLauncher[] commandConfigsArg)
	{
		commandLaunchers = commandConfigsArg.clone();
	}
	
	@Override 
	public void onGuildMessageReceived(GuildMessageReceivedEvent msgEvent) //Reads commands
	{
		String commandString = msgEvent.getMessage().getContentDisplay();
		String commandPrefix = SettingsUtil.getGuildCommandPrefix(msgEvent.getGuild().getId()).toString();

		if (commandString.startsWith(commandPrefix))									   //Prefix to signify that a command is being called.
		{
			String[] command = commandString.split(" ");							   //Separating parameters.
			String key = command[0].replaceFirst(commandPrefix, "");

			int min = 0;
			int max = commandLaunchers.length;
			while (min <= max)
			{
				int mid = (max+min)/2;

				if (commandLaunchers[mid].getCommandKey().compareToIgnoreCase(key) < 0)
				{
					min = mid+1;
				}
				else if (commandLaunchers[mid].getCommandKey().compareToIgnoreCase(key) > 0)
				{
					max = mid-1;
				}
				else if (commandLaunchers[mid].getCommandKey().compareToIgnoreCase(key) == 0)
				{
					commandLaunchers[mid].execute(msgEvent, command);
					return;
				}
			}

			//The command was not found in the global register, so let's check out the custom one.
			SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).getCustomCommandLauncher(key.toLowerCase()).execute(msgEvent, command);
		}

	}
}
