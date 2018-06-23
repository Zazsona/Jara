package commands;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public abstract class Command //A base class to build commands from.
{
	public abstract void run(GuildMessageReceivedEvent msgEvent, String... parameters); //If this remains the only element, change to Interface.
	//msgEvent for command context, parameters for any required data.
}
