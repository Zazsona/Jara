package commands.admin.config;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.TimeZone;

public class ConfigMainSettings
{
    private final GuildSettings guildSettings;
    private final TextChannel channel;

    public ConfigMainSettings(GuildSettings guildSettings, TextChannel channel)
    {
        this.guildSettings = guildSettings;
        this.channel = channel;
    }

    public void parseAsParameters(GuildMessageReceivedEvent msgEvent, String[] parameters) throws IOException
    {
        //TODO
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
                if (msg.getContentDisplay().equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || msg.getContentDisplay().equalsIgnoreCase("quit"))
                {
                    return;
                }
                String prefix = msg.getContentDisplay();
                if (prefix.length() == 1)
                {
                    if (prefix.equals("\\"))
                    {
                        channel.sendMessage("Sorry, that prefix is not supported.").queue();
                    }
                    else
                    {
                        GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
                        guildSettings.setCommandPrefix(prefix.charAt(0));
                        SettingsUtil.refreshGuildCommandPrefix(msgEvent.getGuild().getId());
                        embed.setDescription("Prefix set to "+prefix);
                        channel.sendMessage(embed.build()).queue();
                        break;
                    }
                }
                else
                {
                    embed.setDescription("Invalid prefix. The prefix can only be 1 character long. Please try again.");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }

    public void modifyTimeZone(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        embed.setDescription("Current Time Zone: "+guildSettings.getTimeZoneId().getDisplayName(TextStyle.FULL, Locale.ENGLISH)+"\n\nPlease enter your closest capital city, or time zone's TZDB name. (E.g. \"Europe/London\")\n[TZDB Name List](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)");
        channel.sendMessage(embed.build()).queue();
        MessageManager mm = new MessageManager();
        while (true)
        {
            Message msg = mm.getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                if (msg.getContentDisplay().equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || msg.getContentDisplay().equalsIgnoreCase("quit"))
                {
                    return;
                }
                String selectedTimeZone = parseTZDBName(mm, msg, msgEvent);
                if (selectedTimeZone != null)
                {
                    embed.setDescription("Time zone successfully set to "+guildSettings.getTimeZoneId().getDisplayName(TextStyle.FULL, Locale.ENGLISH)+"!");
                    channel.sendMessage(embed.build()).queue();
                    return;
                }
                else
                {
                    embed.setDescription("Unable to find a valid time zone.\nPlease try again, or use "+guildSettings.getCommandPrefix()+"quit to cancel.");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }

    private String parseTZDBName(MessageManager mm, Message inputMessage, GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        String input = inputMessage.getContentDisplay().replace(" ", "_");
        boolean tzdbInput = (input.contains("/"));
        if (tzdbInput)
        {
            for (String tzDbName : TimeZone.getAvailableIDs())
            {
                if (input.equalsIgnoreCase(tzDbName))
                {
                    guildSettings.setTimeZoneId(tzDbName);
                    return tzDbName;
                }
            }
        }
        else
        {
            for (String tzDbName : TimeZone.getAvailableIDs())
            {
                String formattedTzDbName = (tzDbName.contains("/")) ? tzDbName.substring(tzDbName.lastIndexOf("/")+1) : tzDbName;
                if (input.equalsIgnoreCase(formattedTzDbName))
                {

                    embed.setDescription("Are you in "+tzDbName.replace("_", "")+" ("+ZoneId.of(tzDbName).getDisplayName(TextStyle.FULL, Locale.ENGLISH)+")?\n[Y/N]");
                    channel.sendMessage(embed.build()).queue();
                    while (true)
                    {
                        Message confirmMessage = mm.getNextMessage(channel);
                        if (guildSettings.isPermitted(confirmMessage.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
                        {
                            String msgContent = confirmMessage.getContentDisplay();
                            if (msgContent.equalsIgnoreCase("y") || msgContent.equalsIgnoreCase("yes"))
                            {
                                guildSettings.setTimeZoneId(tzDbName);
                                return tzDbName;
                            }
                            else if (msgContent.equalsIgnoreCase("n") || msgContent.equalsIgnoreCase("no"))
                            {
                                break;
                            }
                            else if (msgContent.equalsIgnoreCase("quit") || msgContent.equalsIgnoreCase(guildSettings.getCommandPrefix()+"quit"))
                            {
                                return null;
                            }
                            else
                            {
                                embed.setDescription("Unknown response. Please enter yes or no.");
                                channel.sendMessage(embed.build()).queue();
                            }

                        }
                    }
                }
            }
        }
        return null;
    }
}
