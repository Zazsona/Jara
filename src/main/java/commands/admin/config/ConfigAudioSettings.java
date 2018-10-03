package commands.admin.config;

import configuration.GuildSettings;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.regex.Pattern;

public class ConfigAudioSettings
{
    private final GuildSettings guildSettings;
    private final TextChannel channel;
    public ConfigAudioSettings(GuildSettings guildSettings, TextChannel channel)
    {
        this.guildSettings = guildSettings;
        this.channel = channel;
    }

    public void showMenu(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        embed.setDescription("What would you like to modify?\n**Skip Votes**");
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = new MessageManager().getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                String msgContent = msg.getContentDisplay();
                if (msgContent.equalsIgnoreCase("skip votes") || msgContent.equalsIgnoreCase("skipvotes"))
                {
                    modifySkipVotes(msgEvent);
                    break;
                }
                else
                {
                    embed.setDescription("Unrecognised category. Please try again.");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }

    public void modifySkipVotes(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        StringBuilder descBuilder = new StringBuilder();
        if (!guildSettings.getGameCategoryId().equals(""))
        {
            descBuilder.append("Current value: **").append(guildSettings.getTrackSkipPercent()).append("%**\n\n");
        }
        descBuilder.append("This setting defines what percentage of people in a voice channel need to vote to skip the track.\nPlease enter a percentage.");
        embed.setDescription(descBuilder.toString());
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = new MessageManager().getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                String response = msg.getContentDisplay();
                response = response.replace("%", "");
                if (Pattern.matches("[0-9]*", response))
                {
                    guildSettings.setTrackSkipPercent(Integer.parseInt(response));
                    try
                    {
                        guildSettings.save();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        embed.setDescription("An error occurred when saving settings.");
                        channel.sendMessage(embed.build()).queue();
                    }
                    catch (NumberFormatException e)
                    {
                        embed.setDescription("Unknown percentage value. Please enter a percentage.");
                        channel.sendMessage(embed.build());
                    }

                    embed.setDescription("Skip percentage has been set to "+response+"%.");
                    channel.sendMessage(embed.build()).queue();
                }
                else
                {
                    embed.setDescription("Unknown percentage value. Please enter a percentage.");
                    channel.sendMessage(embed.build());
                }
            }
        }
    }
}
