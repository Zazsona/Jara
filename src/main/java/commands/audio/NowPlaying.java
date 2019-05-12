package commands.audio;

import audio.Audio;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.CmdUtil;
import commands.Command;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class NowPlaying extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setThumbnail("https://i.imgur.com/wHdSqH5.png");
        embed.setTitle("Now Playing...");
        Audio audio = CmdUtil.getGuildAudio(msgEvent.getGuild().getId());

        AudioTrack currentTrack = audio.getPlayer().getPlayingTrack();
        if (currentTrack == null)
        {
            embed.setDescription("No track is currently playing.");
        }
        else
        {
            StringBuilder descBuilder = new StringBuilder();
            descBuilder.append("Progress: ").append(CmdUtil.formatMillisecondsToHhMmSs(currentTrack.getPosition())).append("/").append(CmdUtil.formatMillisecondsToHhMmSs(currentTrack.getDuration()));
            descBuilder.append("\n");
            descBuilder.append("URL: ").append(currentTrack.getInfo().uri).append("\n");
            descBuilder.append("Livestream: ").append(currentTrack.getInfo().isStream).append("\n");
            descBuilder.append("=====\n");
            descBuilder.append(CmdUtil.formatAudioTrackDetails(currentTrack));
            embed.setDescription(descBuilder.toString());
        }
        msgEvent.getChannel().sendMessage(embed.build()).queue();
    }
}
