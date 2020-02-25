package module;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * The class which indicates a class is a command. Without this interface, commands will not be identified.
 */
public abstract class ModuleCommand extends ModuleClass
{
    /**
	 * The entrance method for the command.
	 * @param msgEvent the context
	 * @param parameters the parameters for this command, including the calling term.
	 */
	public abstract void run(GuildMessageReceivedEvent msgEvent, String... parameters);

	/**
	 * If a command has been running for a long time, this method will be called, and the thread killed.
	 * This is to stop commands being forgotten about and hogging up resources. As such, you should dispose of any items here and tidy up.
	 * <br><br>
	 * If the command is starting a service you do not want killed, you should do so by creating a new thread.
	 */
	public void dispose()
	{
	}
}
