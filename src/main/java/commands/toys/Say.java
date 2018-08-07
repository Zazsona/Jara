package commands.toys;

import commands.Command;
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
            for (int i = 1; i<parameters.length; i++)
            {
                message.append(parameters[i]).append(" ");
            }
            msgEvent.getChannel().sendMessage(message.toString()).queue();
        }
        else
        {
            CommandRegister.sendHelpInfo(msgEvent, getClass());
        }

    }
}
