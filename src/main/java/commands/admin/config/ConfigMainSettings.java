package commands.admin.config;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;

public class ConfigMainSettings
{
    private final GuildSettings guildSettings;
    private final TextChannel channel;

    public ConfigMainSettings(GuildSettings guildSettings, TextChannel channel)
    {
        this.guildSettings = guildSettings;
        this.channel = channel;
    }

    public void modifyPrefix(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        String descBuilder = "**Current Value**: " + SettingsUtil.getGuildCommandPrefix(msgEvent.getGuild().getId()) +
                "\n\n" + "The prefix is the character used to summon the bot, and is entered before each command (E.g **/**Help)\nPlease enter a new prefix value.";
        embed.setDescription(descBuilder);
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
                    SettingsUtil.refreshGuildCommandPrefix(msgEvent.getGuild().getId());
                    embed.setDescription("Prefix set to "+prefix);
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                else
                {
                    embed.setDescription("Invalid prefix. The prefix can only be 1 character long. Please try again.");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }
}
