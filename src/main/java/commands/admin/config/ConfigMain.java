package commands.admin.config;

import commands.CmdUtil;
import module.Command;
import configuration.GuildSettings;
import configuration.SettingsUtil;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ConfigMain extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        TextChannel channel = msgEvent.getChannel();
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        try
        {
            if (!parseAsParameters(msgEvent, channel, embed, parameters))
            {
                MessageManager mm = new MessageManager();
                String embedDescription = "Welcome to the Config\nPlease select a menu, or say `quit` to cancel.";
                embed.setDescription(embedDescription);
                embed.addField("Menus", "**Prefix**\n**Audio**\n**Games**\n**Commands**\n**Setup**", true);
                channel.sendMessage(embed.build()).queue();
                while (true)
                {
                    Message msg = mm.getNextMessage(channel);
                    if (SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
                    {
                        String selection = msg.getContentDisplay();
                        final GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
                        if (selection.equalsIgnoreCase("prefix"))
                        {
                            new ConfigMainSettings(guildSettings, channel).modifyPrefix(msgEvent);
                        }
                        else if (selection.equalsIgnoreCase("audio"))
                        {
                            new ConfigAudioSettings(guildSettings, channel).showMenu(msgEvent);
                        }
                        else if (selection.equalsIgnoreCase("games"))
                        {
                            new ConfigGameSettings(guildSettings, channel).showMenu(msgEvent);
                        }
                        else if (selection.equalsIgnoreCase("commands"))
                        {
                            new ConfigCommandSettings(guildSettings, channel).getCommand(msgEvent);
                        }
                        else if (selection.equalsIgnoreCase("setup"))
                        {
                            new ConfigWizard(msgEvent, guildSettings, channel);
                            return;
                        }
                        else if (selection.equalsIgnoreCase("quit"))
                        {
                            embed.setDescription("Config closed.");
                            embed.clearFields();
                            channel.sendMessage(embed.build()).queue();
                            break;
                        }
                        else
                        {
                            embed.setDescription("Unknown menu: "+selection+". To quit, enter \"quit\".");
                            channel.sendMessage(embed.build()).queue();
                            embed.setDescription(embedDescription);
                            continue;
                        }
                        channel.sendMessage(embed.build()).queue();
                    }
                }
            }
        }
        catch (IOException e)
        {
            LoggerFactory.getLogger(this.getClass()).error(e.toString());
            embed.setDescription("An error occurred when saving settings.");
            channel.sendMessage(embed.build()).queue();
        }
    }

    private boolean parseAsParameters(GuildMessageReceivedEvent msgEvent, TextChannel channel, EmbedBuilder embed, String[] parameters) throws IOException
    {
        if (parameters.length > 1)
        {
            String selection = parameters[1].toLowerCase();
            final GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
            if (selection.equalsIgnoreCase("prefix"))
            {
                new ConfigMainSettings(guildSettings, channel).modifyPrefix(msgEvent);
            }
            else if (selection.equalsIgnoreCase("audio"))
            {
                new ConfigAudioSettings(guildSettings, channel).parseAsParameter(msgEvent, parameters);
            }
            else if (selection.equalsIgnoreCase("games"))
            {
                new ConfigGameSettings(guildSettings, channel).parseAsParameter(msgEvent, parameters);
            }
            else if (selection.equalsIgnoreCase("commands"))
            {
                new ConfigCommandSettings(guildSettings, channel).parseAsParameters(msgEvent, parameters);
            }
            else if (selection.equalsIgnoreCase("setup"))
            {
                new ConfigWizard(msgEvent, guildSettings, channel);
            }
            else
            {
                embed.setDescription("Unknown menu: "+selection+".");
                channel.sendMessage(embed.build()).queue();
            }
            return true;
        }
        return false;
    }

    public static EmbedBuilder getEmbedStyle(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setThumbnail("https://i.imgur.com/Hb8ET7G.png");
        embed.setTitle("= Config =");
        return embed;
    }
}
