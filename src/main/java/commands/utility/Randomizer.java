package commands.utility;

import commands.Command;
import jara.CommandRegister;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Random;

public class Randomizer extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        Random r = new Random();

        if (parameters.length > 1)
        {
            Message randMsg = msgEvent.getChannel().sendMessage(parameters[r.nextInt(parameters.length-1)+1]).complete(); //-1/+1 is being used here as this ignores 0 (the command trigger) without hitting the bounds of the parameters array.

            try
            {
                for (int interval = 100; interval<400; interval=interval+100)
                {
                    randMsg.editMessage(parameters[r.nextInt(parameters.length-1)+1]).queue();
                    Thread.sleep(interval);
                }

            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                randMsg.editMessage(parameters[r.nextInt(parameters.length-1)+1]).queue();
            }
        }
        else
        {
            CommandRegister.sendHelpInfo(msgEvent, getClass());
        }
    }
}
