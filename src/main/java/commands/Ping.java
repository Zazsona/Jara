package commands;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Ping extends Command
{
	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
	{
		msgEvent.getChannel().sendMessage("Pong!").queue();	
	}

}
