package commands;
import java.util.Random;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class CoinFlip extends Command
{
	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
	{
        Random r = new Random();
        int RandVal = (r.nextInt(2));
        if (RandVal == 0)
        {
            msgEvent.getChannel().sendMessage("It's heads!").queue();
        }
        else
        {
        	msgEvent.getChannel().sendMessage("It's tails!").queue();
        }
	}
	
}
