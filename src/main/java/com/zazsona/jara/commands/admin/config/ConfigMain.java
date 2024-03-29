package com.zazsona.jara.commands.admin.config;

import com.zazsona.jara.commands.CmdUtil;
import com.zazsona.jara.listeners.ConfigListener;
import com.zazsona.jara.listeners.ListenerManager;
import com.zazsona.jara.module.ModuleCommand;
import com.zazsona.jara.configuration.GuildSettings;
import com.zazsona.jara.configuration.SettingsUtil;
import com.zazsona.jara.MessageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

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
                embed.addField("Menus", "**General**\n**Audio**\n**Games**\n**Commands**\n**Modules**\n**Setup**", true);
                channel.sendMessage(embed.build()).queue();
                while (true)
                {
                    Message msg = mm.getNextMessage(channel);
                    if (SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).isPermitted(msg.getMember(), getModuleAttributes().getKey())) //If the message is from someone with config permissions
                    {
                        String selection = msg.getContentDisplay();
                        final GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
                        if (selection.equalsIgnoreCase("general"))
                        {
                            new ConfigGeneralSettings(guildSettings, channel, this).showMenu(msgEvent);
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
                            runUpdateListeners(msgEvent.getGuild().getId(), guildSettings);
                            return;
                        }
                        else if (selection.equalsIgnoreCase("quit"))
                        {
                            embed.setDescription("Config closed.");
                            embed.clearFields();
                            channel.sendMessage(embed.build()).queue();
                            runUpdateListeners(msgEvent.getGuild().getId(), guildSettings);
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
            runUpdateListeners(msgEvent.getGuild().getId(), SettingsUtil.getGuildSettings(msgEvent.getGuild().getId())); //Still firing this, in case any other changes did occur, or file corruption happened.
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
            if (selection.equalsIgnoreCase("general"))
            {
                new ConfigGeneralSettings(guildSettings, channel, this).parseAsParameters(msgEvent, parameters);
                runUpdateListeners(msgEvent.getGuild().getId(), guildSettings);
            }
            else if (selection.equalsIgnoreCase("commands"))
            {
                new ConfigCommandSettings(guildSettings, channel, this).parseAsParameters(msgEvent, parameters);
                runUpdateListeners(msgEvent.getGuild().getId(), guildSettings);
            }
            else if (selection.equalsIgnoreCase("modules"))
            {
                new ConfigModuleSettings(guildSettings, channel, this).parseAsParameters(msgEvent, parameters);
                runUpdateListeners(msgEvent.getGuild().getId(), guildSettings);
            }
            else if (selection.equalsIgnoreCase("setup"))
            {
                new ConfigWizard(msgEvent, guildSettings, channel, this);
                runUpdateListeners(msgEvent.getGuild().getId(), guildSettings);
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

    private void runUpdateListeners(String guildID, GuildSettings guildSettings)
    {
        ConcurrentLinkedQueue<ConfigListener> listeners = ListenerManager.getConfigListeners();
        if (listeners.size() > 0)
        {
            new Thread(() -> { listeners.forEach((v) -> v.onUpdate(guildID, guildSettings)); }).start();
        }
    }

    private void runResetListeners(String guildID)
    {
        ConcurrentLinkedQueue<ConfigListener> listeners = ListenerManager.getConfigListeners();
        if (listeners.size() > 0)
        {
            new Thread(() -> { listeners.forEach((v) -> v.onReset(guildID)); }).start();
        }
    }
}
