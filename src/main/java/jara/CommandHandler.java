package jara;

import commands.GameCommand;
import configuration.CommandLauncher;
import configuration.SettingsUtil;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashMap;

public class CommandHandler extends ListenerAdapter 
{
	private final HashMap<String, CommandLauncher> commandLaunchers; //Contains details on all commands.
	public CommandHandler(HashMap<String, CommandLauncher> commandLaunchers)
	{
		this.commandLaunchers = commandLaunchers;
	}
	
	@Override 
	public void onGuildMessageReceived(GuildMessageReceivedEvent msgEvent) //Reads commands
	{
		try
		{
			if (!msgEvent.getAuthor().isBot()) //This is to prevent /say abuse from this bot & others, which would allow users to execute commands under the bot's permissions.
			{
				String commandString = msgEvent.getMessage().getContentDisplay();
				String commandPrefix = SettingsUtil.getGuildCommandPrefix(msgEvent.getGuild().getId()).toString();

				if (commandString.startsWith(commandPrefix))									   //Prefix to signify that a command is being called.
				{
					String[] command = commandString.split(" ");							   //Separating parameters.
					String key = command[0].replaceFirst(commandPrefix, "").toLowerCase();

					CommandLauncher cl = commandLaunchers.get(key);
					if (cl == null || SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).getCustomCommand(key) != null) //This second check ensures that, if the key also matches a custom command, that gets precedence. This is because the custom command key is stored as a regular command for compatibility.
					{
						cl = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).getCustomCommandLauncher(key.toLowerCase());

						if (cl == null)
						{
							for (String customCommandKey : SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).getCustomCommandMap().keySet())
							{
								cl = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).getCustomCommandLauncher(customCommandKey);
								String[] aliases = cl.getAliases();

								int min = 0;
								int max = aliases.length-1;
								while (min <= max)
								{
									int mid = (max+min)/2;

									if (aliases[mid].compareToIgnoreCase(key) < 0)
									{
										min = mid+1;
									}
									else if (aliases[mid].compareToIgnoreCase(key) > 0)
									{
										max = mid-1;
									}
									else if (aliases[mid].compareToIgnoreCase(key) == 0)
									{
										break;
									}
								}
								cl = null; //We haven't found any valid command launcher.
							}
						}
					}
					if (cl != null)
					{
						cl.execute(msgEvent, command);
					}
				}
			}
		}
		catch (NullPointerException e)
		{
			//Command does not exist.
		}
	}
}
