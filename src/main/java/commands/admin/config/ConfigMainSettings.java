package commands.admin.config;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.regex.Pattern;

public class ConfigMainSettings
{
    private final GuildSettings guildSettings;
    private final TextChannel channel;

    public ConfigMainSettings(GuildSettings guildSettings, TextChannel channel)
    {
        this.guildSettings = guildSettings;
        this.channel = channel;
    }

    public void modifyPrefix(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        StringBuilder descBuilder = new StringBuilder();
        descBuilder.append("**Current Value**: ").append(SettingsUtil.getGuildCommandPrefix(msgEvent.getGuild().getId()));
        descBuilder.append("\n\n").append("The prefix is the character used to summon the bot, and is entered before each command (E.g **/**Help)\nPlease enter a new prefix value.");
        embed.setDescription(descBuilder.toString());
        msgEvent.getChannel().sendMessage(embed.build()).queue();

        MessageManager mm = new MessageManager();
        while (true)
        {
            Message msg = mm.getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                if (msg.getContentDisplay().length() == 5 && msg.getContentDisplay().toLowerCase().endsWith("quit"))
                {
                    embed.setDescription("Config closed.");
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                String prefix = msg.getContentDisplay();
                if (prefix.length() == 1)
                {
                    GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
                    guildSettings.setCommandPrefix(prefix.charAt(0));
                    try
                    {
                        guildSettings.save();
                    }
                    catch (IOException e)
                    {
                        channel.sendMessage("An error occurred when saving.").queue();
                        e.printStackTrace();
                    }
                    SettingsUtil.refreshGuildCommandPrefix(msgEvent.getGuild().getId());
                    embed.setDescription("Prefix set to "+prefix);
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                else
                {
                    embed.setDescription("Invalid prefix. The prefix can only be 1 character long.");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }
}