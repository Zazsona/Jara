package commands.toys;

import commands.Command;
import commands.Help;
import jara.CommandRegister;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;


public class Say extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        if (parameters.length > 1)
        {
            StringBuilder message = new StringBuilder();
            for (String part : parameters)
            {
                message.append(part + " ");
            }
            msgEvent.getChannel().sendMessage(message.toString()).queue();
        }
        else
        {
            new Help().run(msgEvent, new String[] {"/?", CommandRegister.getCommand(getClass()).getCommandKey()}); //TODO: Consider making this a command register method? (GetInfo)
            /*
             * So, technically this is fine, as help is *always* enabled and cannot be disabled. But generally calling commands like this is a bad idea, as they may be disabled.
             * This also saves us having to copy the info here, which could be a problem as commands change.
             */
        }

    }
}
