package commands.audio;

import audio.Audio;
import commands.CmdUtil;
import commands.Command;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Pause extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        Audio audio = CmdUtil.getGuildAudio(msgEvent.getGuild().getId());

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setThumbnail("https://i.imgur.com/wHdSqH5.png");
        embed.setTitle("Pause");

        if (audio.getPlayer().getPlayingTrack() != null)
        {
            boolean paused = audio.getPlayer().isPaused();
            audio.getPlayer().setPaused(!paused);

            if (paused)
                embed.setDescription(audio.getPlayer().getPlayingTrack().getInfo().title+" resumed.");
            else
                embed.setDescription(audio.getPlayer().getPlayingTrack().getInfo().title+" paused.");
        }
        else
        {
            embed.setDescription("No track is currently playing.");
        }
        msgEvent.getChannel().sendMessage(embed.build()).queue();

    }
}
