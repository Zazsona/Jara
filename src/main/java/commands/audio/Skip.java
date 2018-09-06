package commands.audio;

import audio.Audio;
import commands.CmdUtil;
import commands.Command;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Skip extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        Audio audio = CmdUtil.getGuildAudio(msgEvent.getGuild().getId());
        VoiceChannel vChannel = msgEvent.getMember().getVoiceState().getChannel();
        TextChannel tChannel = msgEvent.getChannel();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setThumbnail("https://i.imgur.com/wHdSqH5.png");
        embed.setTitle("Skip Track");

        if (audio.isAudioPlayingInGuild() && vChannel != null)
        {
            StringBuilder descBuilder = new StringBuilder();

            boolean skipRequestAdded = audio.registerSkipVote(msgEvent.getMember().getUser().getId());
            if (skipRequestAdded)
            {
                descBuilder.append("You have voted to skip.\n");
            }
            else
            {
                descBuilder.append("Skip vote cancelled.\n");
            }

            int skipVotesRequired = ((vChannel.getMembers().size()-1)/2); //We subtract 1 to ignore the bot. //TODO: Have the percentage of people voting be configurable.
            if (skipVotesRequired == 0) {skipVotesRequired++;} //This is just for formatting, as otherwise it'd appear as "1/0 skips". It has no bearing on functionality.

            descBuilder.append("\n");
            descBuilder.append("Skip Progress: ").append(audio.getSkipVotes()).append("/").append(skipVotesRequired);

            if (audio.getSkipVotes() >= skipVotesRequired)
            {
                descBuilder.append(" (Now skipping)");
                audio.getPlayer().stopTrack();
            }
            embed.setDescription(descBuilder.toString());
        }
        else if (!audio.isAudioPlayingInGuild())
        {
            embed.setDescription("No track is currently playing.");
        }
        else if (vChannel == null)
        {
            embed.setDescription("You must be in the channel to skip the track.");
        }

        tChannel.sendMessage(embed.build()).queue();

    }
}
