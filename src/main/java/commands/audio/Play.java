package commands.audio;

import audio.Audio;
import audio.Audio.RequestResult;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import commands.CmdUtil;
import commands.Command;
import configuration.SettingsUtil;
import jara.CommandRegister;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Play extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        Audio audio = CmdUtil.getGuildAudio(msgEvent.getGuild().getId());
        TextChannel channel = msgEvent.getChannel();

        if (parameters.length >= 2)
        {
            audio.playWithFeedback(msgEvent.getMember(), parameters[1], channel);
        }
        else
        {
            audio.playWithFeedback(msgEvent.getMember(), "", channel);
        }
    }
}
