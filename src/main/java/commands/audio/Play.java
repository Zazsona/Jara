package commands.audio;

import audio.Audio;
import commands.Command;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Play extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        //TODO: Implement check for existing Audio instance. There should be a single instance to a guild. (CmdUtils?)
        Audio audio = new Audio(msgEvent.getGuild());
        audio.play(msgEvent.getMember(), parameters[1]);
    }
}
