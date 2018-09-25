package commands.utility;
import java.util.Random;

import commands.Command;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class CoinFlip extends Command
{
	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
	{
        Random r = new Random();
        boolean isHeads = (r.nextBoolean());
        if (isHeads)
        {
            msgEvent.getChannel().sendMessage("It's heads!").queue();
        }
        else
        {
        	msgEvent.getChannel().sendMessage("It's tails!").queue();
        }
	}
	
}
