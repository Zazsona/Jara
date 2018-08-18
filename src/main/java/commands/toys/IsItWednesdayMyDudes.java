package commands.toys;

import commands.Command;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.time.LocalDate;

public class IsItWednesdayMyDudes extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        if (LocalDate.now().getDayOfWeek().getValue() == 3)
        {
            msgEvent.getChannel().sendMessage("https://i.imgur.com/n7I7cKp.jpg").complete();
        }
        else
        {
            msgEvent.getChannel().sendMessage("https://i.imgur.com/FLXswHO.jpg").complete();
        }
    }
}
