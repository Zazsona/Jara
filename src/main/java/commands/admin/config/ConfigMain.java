package commands.admin.config;

import commands.Command;
import configuration.GuildSettings;
import configuration.SettingsUtil;
import jara.Core;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;

public class ConfigMain extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        TextChannel channel = msgEvent.getChannel();
        MessageManager mm = new MessageManager();
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        String embedDescription = "Welcome to the Config\nPlease select a menu, or say `quit` to cancel.";
        embed.setDescription(embedDescription);
        embed.addField("Menus", "**Prefix**\n**AudioSettings**\n**GameSettings**\n**CommandSettings**\n**Reset**", true);
        channel.sendMessage(embed.build()).queue();
        try
        {
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
                    else if (selection.equalsIgnoreCase("audiosettings"))
                    {
                        new ConfigAudioSettings(guildSettings, channel).showMenu(msgEvent);
                    }
                    else if (selection.equalsIgnoreCase("gamesettings"))
                    {
                        new ConfigGameSettings(guildSettings, channel).showMenu(msgEvent);
                    }
                    else if (selection.equalsIgnoreCase("commandsettings"))
                    {
                        new ConfigCommandSettings(guildSettings, channel).showMenu(msgEvent);
                    }
                    else if (selection.equalsIgnoreCase("reset"))
                    {
                        //TODO: Make a guild setup wizard and link to it here.
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
                    }
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            embed.setDescription("An error occurred when saving settings.");
            channel.sendMessage(embed.build()).queue();
        }
    }

    public static EmbedBuilder getEmbedStyle(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setThumbnail("https://i.imgur.com/Hb8ET7G.png");
        embed.setTitle("= Config =");
        return embed;
    }
}
