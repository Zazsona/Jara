package commands.audio;

import audio.Audio;
import commands.CmdUtil;
import commands.Command;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class ForceSkip extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        Audio audio = CmdUtil.getGuildAudio(msgEvent.getGuild().getId());
        TextChannel tChannel = msgEvent.getChannel();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setThumbnail("https://i.imgur.com/wHdSqH5.png");
        embed.setTitle("Skip Track");

        if (audio.isAudioPlayingInGuild())
        {
            embed.setDescription("Forcibly skipping track.");
            audio.getPlayer().stopTrack();
        }
        else if(!audio.isAudioPlayingInGuild())
        {
            embed.setDescription("No track is currently playing.");
        }

        tChannel.sendMessage(embed.build()).queue();

    }
}
