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

    public void showMenu(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        String embedDescription = "What would you like to modify?\nSay `quit` to close this menu.\n**Skip Votes**\n**Voice Leaving**";
        embed.setDescription(embedDescription);
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
                }
                else if (msgContent.equalsIgnoreCase("voice leaving") || msgContent.equalsIgnoreCase("voiceleaving"))
                {
                    modifyVoiceLeaving(msgEvent);
                }
                else if (msgContent.equalsIgnoreCase("quit"))
                {
                    return;
                }
                else
                {
                    embed.setDescription("Unrecognised category. Please try again.");
                    channel.sendMessage(embed.build()).queue();
                    embed.setDescription(embedDescription);
                }
                channel.sendMessage(embed.build()).queue();
            }
        }
    }

    private void modifySkipVotes(GuildMessageReceivedEvent msgEvent) throws IOException
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
                    int percentage = Integer.parseInt(response);
                    if (percentage < 0 || percentage > 100)
                    {
                        embed.setDescription("Invalid percentage value. Please enter a value between 0 and 100.");
                        channel.sendMessage(embed.build()).queue();
                    }
                    else
                    {
                        guildSettings.setTrackSkipPercent(Integer.parseInt(response));
                        embed.setDescription("Skip percentage has been set to "+response+"%.");
                        channel.sendMessage(embed.build()).queue();
                        break;
                    }
                }
                else
                {
                    embed.setDescription("Unknown percentage value. Please enter a percentage.");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }

    private void modifyVoiceLeaving(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        String descBuilder = "Current value: **" + guildSettings.isVoiceLeavingEnabled() + "**\n\n" +
                "This setting will make it so the bot leaves the voice channel when no audio is playing.\n\n Would you like to enable it? [Y/n]";
        embed.setDescription(descBuilder);
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = new MessageManager().getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                String response = msg.getContentDisplay().toLowerCase();
                if (response.startsWith("y"))
                {
                    guildSettings.setVoiceLeaving(true);
                    embed.setDescription("Voice Leaving has been enabled.");
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                else if (response.startsWith("n"))
                {
                    guildSettings.setVoiceLeaving(false);
                    embed.setDescription("Voice Leaving has been disabled.");
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                else
                {
                    embed.setDescription("Unknown response. Would you like to enable voice leaving? [Y/n]");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }
}
