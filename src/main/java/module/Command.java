package module;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * The class which indicates a class is a command. Without this interface, commands will not be identified.
 */
public abstract class Command //A base class to build commands from.
{
	/**
	 * The entrance method for a command. Always start from this point.
	 * @param msgEvent the context
	 * @param parameters the parameters for this command, including the calling term.
	 */
	public abstract void run(GuildMessageReceivedEvent msgEvent, String... parameters);
}
