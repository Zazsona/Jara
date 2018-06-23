package jara;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
							try
							{
								commandConfig.getCommandClass().newInstance().run(msgEvent, command);
							} 
							catch (InstantiationException | IllegalAccessException e)
							{
								msgEvent.getChannel().sendMessage("Sorry, I was unable to run the command.").queue();
								Logger logger = LoggerFactory.getLogger(CommandHandler.class);
								logger.error("A command request was sent but could not be fulfilled: "+command.toString()); //TODO: Provide more details (Guild, user, channel, etc.)
								e.printStackTrace();
							}
							return; //If the command is found, whether it worked or not, there's no point performing further checks.
						}
					}
				}
			}
			
		}

	}
}
