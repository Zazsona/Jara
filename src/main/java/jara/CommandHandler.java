package jara;

import configuration.CommandLauncher;
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
        HashMap<String, GuildSettingsJson.CustomCommandConfig> customGuildCommands = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).getCustomCommandMap();
		if (commandString.startsWith(commandPrefix))									   //Prefix to signify that a command is being called.
		{
			String[] command = commandString.split(" ");							   //Separating parameters.
			for (CommandLauncher commandConfig : commandLaunchers)
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
            for (String customKey : customGuildCommands.keySet())
            {
            	CommandLauncher customCommandLauncher = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).getCustomCommandLauncher(customKey);
                if (SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).isCommandEnabled(customKey))								//While execute() does an enabled check anyway, this saves us iterating through aliases.
                {
                    for (String alias : customCommandLauncher.getAliases())
                    {
                        if (command[0].equalsIgnoreCase(commandPrefix+alias))
                        {
                            customCommandLauncher.execute(msgEvent, command);
                            return;
                        }
                    }
                }
                else
				{
					msgEvent.getChannel().sendMessage("This command is disabled. Please talk to your guild owner if you wish to have it enabled.").queue();
				}
            }
			
		}

	}
}
