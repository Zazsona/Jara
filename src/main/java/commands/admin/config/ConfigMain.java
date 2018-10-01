package commands.admin.config;

import commands.Command;
import configuration.GuildSettings;
import configuration.SettingsUtil;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class ConfigMain extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        TextChannel channel = msgEvent.getChannel();
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        if (parameters.length == 1)
        {
            embed.setDescription("Welcome to the Config\nTo select a menu, use `/config [selection]`.");
            embed.addField("Menus", "**GameSettings**\n**CommandSettings**\n**Reset**", true);
            channel.sendMessage(embed.build()).queue();
        }
        else if (parameters.length > 1)
        {
            final GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
            if (parameters[1].equalsIgnoreCase("gamesettings"))
            {
                new ConfigGameSettings(guildSettings, channel).showMenu(msgEvent);
            }
            else if (parameters[1].equalsIgnoreCase("commandsettings"))
            {
                new ConfigCommandSettings(guildSettings, channel).showMenu(msgEvent);
            }
            else if (parameters[1].equalsIgnoreCase("reset"))
            {
                //TODO: Make a guild setup wizard and link to it here.
            }
            else
            {
                embed.setDescription("Unknown menu: "+parameters[1]);
            }
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
