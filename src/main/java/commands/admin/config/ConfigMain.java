package commands.admin.config;

import commands.CmdUtil;
import module.ModuleCommand;
import configuration.GuildSettings;
import configuration.SettingsUtil;
import jara.MessageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class ConfigMain extends ModuleCommand
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
                embed.addField("Menus", "**Prefix**\n**Timezone**\n**Audio**\n**Games**\n**Commands**\n**Modules**\n**Setup**", true);
                channel.sendMessage(embed.build()).queue();
                while (true)
                {
                    Message msg = mm.getNextMessage(channel);
                    if (SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).isPermitted(msg.getMember(), getModuleAttributes().getKey())) //If the message is from someone with config permissions
                    {
                        String selection = msg.getContentDisplay();
                        final GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
                        if (selection.equalsIgnoreCase("prefix"))
                        {
                            new ConfigMainSettings(guildSettings, channel, this).modifyPrefix(msgEvent);
                        }
                        else if (selection.equalsIgnoreCase("timezone"))
                        {
                            new ConfigMainSettings(guildSettings, channel, this).modifyTimeZone(msgEvent);
                        }
                        else if (selection.equalsIgnoreCase("audio"))
                        {
                            new ConfigAudioSettings(guildSettings, channel, this).showMenu(msgEvent);
                        }
                        else if (selection.equalsIgnoreCase("games"))
                        {
                            new ConfigGameSettings(guildSettings, channel, this).showMenu(msgEvent);
                        }
                        else if (selection.equalsIgnoreCase("commands"))
                        {
                            new ConfigCommandSettings(guildSettings, channel, this).getCommand(msgEvent);
                        }
                        else if (selection.equalsIgnoreCase("modules"))
                        {
                            new ConfigModuleSettings(guildSettings, channel, this).getModule(msgEvent);
                        }
                        else if (selection.equalsIgnoreCase("setup"))
                        {
                            new ConfigWizard(msgEvent, guildSettings, channel, this);
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

    /**
     * Runs through the config using the navigation options supplied in a single message
     * @param msgEvent context
     * @param channel the channel to run in
     * @param embed the embed style
     * @param parameters the parameters to parse
     * @return boolean on success
     * @throws IOException unable to write to file
     */
    private boolean parseAsParameters(GuildMessageReceivedEvent msgEvent, TextChannel channel, EmbedBuilder embed, String[] parameters) throws IOException
    {
        if (parameters.length > 1)
        {
            String selection = parameters[1].toLowerCase();
            final GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
            if (selection.equalsIgnoreCase("prefix") || selection.equalsIgnoreCase("timezone"))
            {
                new ConfigMainSettings(guildSettings, channel, this).parseAsParameters(msgEvent, parameters);
            }
            else if (selection.equalsIgnoreCase("audio"))
            {
                new ConfigAudioSettings(guildSettings, channel, this).parseAsParameter(msgEvent, parameters);
            }
            else if (selection.equalsIgnoreCase("games"))
            {
                new ConfigGameSettings(guildSettings, channel, this).parseAsParameter(msgEvent, parameters);
            }
            else if (selection.equalsIgnoreCase("commands"))
            {
                new ConfigCommandSettings(guildSettings, channel, this).parseAsParameters(msgEvent, parameters);
            }
            else if (selection.equalsIgnoreCase("modules"))
            {
                new ConfigModuleSettings(guildSettings, channel, this).parseAsParameters(msgEvent, parameters);
            }
            else if (selection.equalsIgnoreCase("setup"))
            {
                new ConfigWizard(msgEvent, guildSettings, channel, this);
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

    /**
     * Gets the default embed style for configs
     * @param msgEvent context
     * @return a partially build EmbedBuilder
     */
    public static EmbedBuilder getEmbedStyle(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setThumbnail("https://i.imgur.com/Hb8ET7G.png");
        embed.setTitle("= Config =");
        return embed;
    }
}
