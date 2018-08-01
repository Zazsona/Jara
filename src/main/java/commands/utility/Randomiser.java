package commands.utility;

import commands.Command;
import commands.Help;
import configuration.CommandConfiguration;
import jara.CommandRegister;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Random;

public class Randomiser extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        Random r = new Random();
        if (parameters.length > 1)
        {
            Message rouletteMsg = msgEvent.getChannel().sendMessage(parameters[r.nextInt(parameters.length-1)+1]).complete(); //-1/+1 is being used here as this ignores 0 (the command trigger) without hitting the bounds of the parameters array.

            try
            {
                rouletteMsg.editMessage(parameters[r.nextInt(parameters.length-1)+1]).queue();
                Thread.sleep(100);
                rouletteMsg.editMessage(parameters[r.nextInt(parameters.length-1)+1]).queue();
                Thread.sleep(200);                                                                      //Increase the wait to built dramatic effect.
                rouletteMsg.editMessage(parameters[r.nextInt(parameters.length-1)+1]).queue();
                Thread.sleep(300);
                rouletteMsg.editMessage(parameters[r.nextInt(parameters.length-1)+1]).queue();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                rouletteMsg.editMessage(parameters[r.nextInt(parameters.length-1)+1]).queue();
            }
        }
        else
        {
            new Help().run(msgEvent, new String[] {"/?", CommandRegister.getCommand(getClass()).getCommandKey()});
            /*
             * So, technically this is fine, as help is *always* enabled and cannot be disabled. But generally calling commands like this is a bad idea, as they may be disabled.
             * This also saves us having to copy the info here, which could be a problem as commands change.
             */
        }

    }
}
