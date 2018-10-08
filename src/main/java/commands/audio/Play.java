package commands.audio;

import audio.Audio;
import audio.Audio.RequestResult;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import commands.CmdUtil;
import commands.Command;
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
        VoiceChannel vChannel = msgEvent.getMember().getVoiceState().getChannel();
        if (vChannel != null && ((vChannel.getUserLimit() == 0) || (vChannel.getUserLimit() > vChannel.getMembers().size())))
        {
            Audio audio = CmdUtil.getGuildAudio(msgEvent.getGuild().getId());
            TextChannel channel = msgEvent.getChannel();
            RequestResult result = audio.play(msgEvent.getMember(), parameters[1]);

            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            embed.setThumbnail("https://i.imgur.com/wHdSqH5.png");
            embed.setTitle("Now Playing...");

            StringBuilder descBuilder = new StringBuilder();
            AudioTrackInfo requestedTrackInfo = audio.getTrackQueue().get(audio.getTrackQueue().size()-1).getInfo(); //This always returns the last track (i.e, the one that was just requested)

            switch (result)
            {
                case REQUEST_NOW_PLAYING:
                    embed.setDescription(CmdUtil.formatAudioTrackDetails(audio.getTrackQueue().get(0)));
                    channel.sendMessage(embed.build()).queue();
                    break;

                case REQUEST_ADDED_TO_QUEUE:
                    descBuilder.append("Your request has been added to the queue.\n");
                    descBuilder.append("Position: "+audio.getTrackQueue().size()+"\n"); //So, index 1 is position 2, 2 is 3, etc. Should be more readable for non-programmers.
                    descBuilder.append("ETA: "+(((audio.getTotalQueuePlayTime()-requestedTrackInfo.length)/1000)/60)+" Minutes\n"); //This ETA is really rough. It abstracts to minutes and ignores the progress of the current track.
                    descBuilder.append("=====\n");
                    descBuilder.append(CmdUtil.formatAudioTrackDetails(audio.getTrackQueue().get(audio.getTrackQueue().size())));
                    embed.setDescription(descBuilder.toString());
                    channel.sendMessage(embed.build()).queue();
                    break;

                case REQUEST_RESULTED_IN_ERROR:
                    embed.setTitle("Error");
                    channel.sendMessage("An unexpected error occurred. Please try again. If the error persists, please notify your server owner.").queue();
                    channel.sendMessage(embed.build()).queue();
                    break;

                case REQUEST_IS_BAD:
                    embed.setTitle("No Track Found");
                    embed.setDescription("Sorry, but I couldn't find a track there.");
                    channel.sendMessage(embed.build()).queue();
                    break;

                case REQUEST_USER_NOT_IN_VOICE:
                    embed.setTitle("No Voice Channel Found");
                    embed.setDescription("I can't find you in any voice channels! Please make sure you're in one I have access to.");
                    channel.sendMessage(embed.build()).queue();
                    break;

                case REQUEST_CHANNEL_PERMISSION_DENIED:
                    embed.setTitle("Permission Denied");
                    embed.setDescription("I can't find you in any voice channels! Please make sure you're in one I have access to.");
                    channel.sendMessage(embed.build()).queue();
                    break;

                case REQUEST_CHANNEL_FULL:
                    embed.setTitle("Channel Full");
                    embed.setDescription("There's no space for me in that channel.");
                    channel.sendMessage(embed.build()).queue();
                    break;

                default:
                    embed.setTitle("Uh-Oh!");
                    embed.setDescription("An unexpected event occurred. You may experience some issues.");
                    channel.sendMessage(embed.build()).queue();
                    break;
            }
        }
        else
        {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            embed.setThumbnail("https://i.imgur.com/wHdSqH5.png");
            embed.setTitle("Error");
            embed.setDescription("You must be in a voice channel to use this command.");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }

    }
}
