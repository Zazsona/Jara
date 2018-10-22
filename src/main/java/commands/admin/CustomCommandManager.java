package commands.admin;

import commands.Command;
import configuration.GuildSettingsJson;
import configuration.SettingsUtil;
import jara.CommandRegister;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.ArrayList;

public class AddCommand extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).addCustomCommand("foobar", new String[]{"testy"}, "A test.", CommandRegister.Category.UTILITY, "Success.", true, new ArrayList<>());
        msgEvent.getChannel().sendMessage("Done.").queue();
        try
        {
            SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).save();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


    }
    //TODO: Implement this (Ensure key and alias are lower case when saving)
}
