package configuration;

import java.io.IOException;
import java.time.LocalDateTime;

import jara.CommandRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commands.Command;
import jara.CommandAttributes;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class CommandConfiguration
{
	private boolean enabledState; //Whether the config allows this command to be used
	private CommandAttributes attributes;
	
	public CommandConfiguration(CommandAttributes attributes, boolean enabledState)
	{
		this.attributes = attributes;
		this.enabledState = enabledState;
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
		if (isEnabled() || !attributes.isDisableable()) //If it can't be disabled, run it anyway even if it is disabled. Stops people fucking with settings to the point the bot is unusable.
		{
			GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());

			if (guildSettings.isCommandEnabled(attributes.getCommandKey()) && (msgEvent.getMember().isOwner() || guildSettings.isPermitted(msgEvent.getMember(), attributes.getCommandClass())))
			{
				Runnable commandRunnable = () -> {
					try
					{
						attributes.getCommandClass().newInstance().run(msgEvent, parameters);
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
				commandThread.setName(attributes.getCommandKey()+"-Thread");
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
		return attributes.getAliases();
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
