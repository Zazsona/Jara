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
        embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setThumbnail("https://i.imgur.com/wHdSqH5.png");
        embed.setTitle("Now Playing...");
        Audio audio = CmdUtil.getGuildAudio(msgEvent.getGuild().getId());

        StringBuilder descBuilder = new StringBuilder();
        AudioTrack currentTrack = audio.getPlayer().getPlayingTrack();
        descBuilder.append("Progress: ").append(CmdUtil.formatMillisecondsToHhMmSs(currentTrack.getPosition())).append("/").append(CmdUtil.formatMillisecondsToHhMmSs(currentTrack.getDuration()));
        descBuilder.append("\n");
        descBuilder.append("URL: ").append(currentTrack.getInfo().uri).append("\n");
        descBuilder.append("Livestream: ").append(currentTrack.getInfo().isStream).append("\n");
        descBuilder.append("=====\n");
        descBuilder.append(CmdUtil.formatAudioTrackDetails(currentTrack));


        embed.setDescription(descBuilder.toString());
        msgEvent.getChannel().sendMessage(embed.build()).queue();
    }
}
