package commands.admin.config;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import jara.MessageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;

public class ConfigMainSettings
{
    private final GuildSettings guildSettings;
    private final TextChannel channel;
    private final ConfigMain configMain;

    /**
     * Constructor
     * @param guildSettings the guild settings to modify
     * @param channel the channel to run on
     * @param configMain the config root
     */
    public ConfigMainSettings(GuildSettings guildSettings, TextChannel channel, ConfigMain configMain)
    {
        this.guildSettings = guildSettings;
        this.channel = channel;
        this.configMain = configMain;
    }

    /**
     * Runs through the config using the navigation options supplied in a single message
     * @param msgEvent context
     * @param parameters the parameters to parse
     * @throws IOException unable to write to file
     */
    public void parseAsParameters(GuildMessageReceivedEvent msgEvent, String[] parameters) throws IOException
    {
        if (parameters[1].equalsIgnoreCase("prefix"))
        {
            if (parameters.length > 2)
            {
                setPrefix(msgEvent, parameters[2]);
            }
            else
            {
                modifyPrefix(msgEvent);
            }
        }
        else if (parameters[1].equalsIgnoreCase("timezone"))
        {
            if (parameters.length > 2)
            {
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i<parameters.length; i++)
                {
                    sb.append(parameters[i]).append("_");
                }
                sb.setLength(sb.length()-1);
                setTimeZone(new MessageManager(), sb.toString(), msgEvent);
            }
            else
            {
                modifyTimeZone(msgEvent);
            }
        }
        else if (parameters[1].equalsIgnoreCase("whitelist") || parameters[1].equalsIgnoreCase("channel whitelist"))
        {
            if (parameters.length > 2)
            {
                if (parameters[2].startsWith("add") || parameters[2].startsWith("remove"))
                {
                    boolean isAdd = parameters[2].startsWith("add");
                    setWhitelistChannels(msgEvent, isAdd, msgEvent.getMessage().getMentionedChannels());
                }
                else if (parameters[2].equalsIgnoreCase("clear"))
                {
                    clearWhitelistChannels(msgEvent);
                }
            }
            else
            {
                manageWhitelistChannels(msgEvent, new MessageManager());
            }
        }
    }

    /**
     * Directs the user through setting the command prefix for the guild
     * @param msgEvent context
     * @throws IOException unable to write to file
     */
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
            if (guildSettings.isPermitted(msg.getMember(), configMain.getModuleAttributes().getKey())) //If the message is from someone with config permissions
            {
                if (msg.getContentDisplay().equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || msg.getContentDisplay().equalsIgnoreCase("quit"))
                {
                    return;
                }
                String prefix = msg.getContentDisplay();
                boolean success = setPrefix(msgEvent, prefix);
                if (success) break;
            }
        }
    }

    /**
     * Verifies the prefix character is valid
     * @param msgEvent context
     * @param prefix the prefix to attempt
     * @return boolean on success
     * @throws IOException unable to write changes
     */
    private boolean setPrefix(GuildMessageReceivedEvent msgEvent, String prefix) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        if (prefix.length() == 1)
        {
            if (prefix.equals("\\"))
            {
                channel.sendMessage("Sorry, that prefix is not supported. Please try again.").queue();
                return false;
            }
            else
            {
                guildSettings.setCommandPrefix(prefix.charAt(0));
                SettingsUtil.refreshGuildCommandPrefix(guildSettings.getGuildId());
                embed.setDescription("Prefix set to "+prefix);
                channel.sendMessage(embed.build()).queue();
                return true;
            }
        }
        else
        {
            embed.setDescription("Invalid prefix. The prefix can only be 1 character long. Please try again.");
            channel.sendMessage(embed.build()).queue();
            return false;
        }
    }

    /*TODO: One change worth considering is taking user input as, for example, BST or British Standard Time, and then comparing against the display name
    This would be a more expensive operation, as every Id would have to be converted to ZoneId, and then getting its display name in both short and full.
    However. This would not respect daylight savings.*/
    /**
     * Directs the user through setting the time zone for the guild
     * @param msgEvent context
     * @throws IOException unable to write to file
     */
    public void modifyTimeZone(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        embed.setDescription("Current Time Zone: "+guildSettings.getTimeZoneId().getDisplayName(TextStyle.FULL, Locale.ENGLISH)+"\n\nPlease enter your closest capital city, or time zone's TZDB name. (E.g. \"Europe/London\")\n[TZDB Name List](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)");
        channel.sendMessage(embed.build()).queue();
        MessageManager mm = new MessageManager();
        while (true)
        {
            Message msg = mm.getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), configMain.getModuleAttributes().getKey())) //If the message is from someone with config permissions
            {
                if (msg.getContentDisplay().equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || msg.getContentDisplay().equalsIgnoreCase("quit"))
                {
                    return;
                }
                else
                {
                    boolean success = setTimeZone(mm, msg.getContentDisplay(), msgEvent);
                    if (success)
                        return;
                }
            }
        }
    }

    /**
     * Verifies the timezone value is valid
     * @param mm the message manager
     * @param input the input to convert to an id
     * @param msgEvent context
     * @return boolean on success
     * @throws IOException unable to write to file
     */
    private boolean setTimeZone(MessageManager mm, String input, GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        input = input.replace(" ", "_");
        boolean tzdbInput = (input.contains("/"));
        boolean success = false;
        if (tzdbInput)
        {
            for (String tzDbName : TimeZone.getAvailableIDs())
            {
                if (input.equalsIgnoreCase(tzDbName))
                {
                    guildSettings.setTimeZoneId(tzDbName);
                    success = true;
                    break;
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
                    while (!success)
                    {
                        Message confirmMessage = mm.getNextMessage(channel);
                        if (guildSettings.isPermitted(confirmMessage.getMember(), configMain.getModuleAttributes().getKey())) //If the message is from someone with config permissions
                        {
                            String msgContent = confirmMessage.getContentDisplay();
                            if (msgContent.equalsIgnoreCase("y") || msgContent.equalsIgnoreCase("yes"))
                            {
                                guildSettings.setTimeZoneId(tzDbName);
                                success = true;
                                break;
                            }
                            else if (msgContent.equalsIgnoreCase("n") || msgContent.equalsIgnoreCase("no"))
                            {
                                break;
                            }
                            else if (msgContent.equalsIgnoreCase("quit") || msgContent.equalsIgnoreCase(guildSettings.getCommandPrefix()+"quit"))
                            {
                                return true;
                            }
                            else
                            {
                                embed.setDescription("Unknown response. Please enter yes or no.");
                                channel.sendMessage(embed.build()).queue();
                            }
                        }
                    }
                    if (success)
                        break;
                }
            }
        }

        if (success)
            embed.setDescription("Time zone successfully set to "+guildSettings.getTimeZoneId().getDisplayName(TextStyle.FULL, Locale.ENGLISH)+"!");
        else
            embed.setDescription("Unable to find a valid time zone.\nPlease try again, or use "+guildSettings.getCommandPrefix()+"quit to cancel.");
        
        channel.sendMessage(embed.build()).queue();
        return success;
    }



    public void manageWhitelistChannels(GuildMessageReceivedEvent msgEvent, MessageManager mm) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        StringBuilder db = embed.getDescriptionBuilder();
        db.append("The channel whitelist limits where commands can be used to the channels specified.\nUse clear to disable the whitelist.\n\nCurrent whitelisted channels: ");
        HashSet<String> channelIDs = guildSettings.getChannelWhitelist();
        if (channelIDs.size() > 0)
        {
            LinkedList<String> idsToClean = new LinkedList<>();
            for (String channelID : channelIDs)
            {
                TextChannel channel = msgEvent.getGuild().getTextChannelById(channelID);
                if (channel == null)
                    idsToClean.add(channelID);
                else
                    db.append(channel.getAsMention()).append(", ");
            }
            guildSettings.removeChannelsFromWhitelist(idsToClean.toArray(new String[0]));
        }
        else
        {
            db.append("All");
        }
        db.append("\n\n==========\n").append("**Controls**\nAdd [#Channel1], [#Channel2]...[#ChannelN]\nRemove [#Channel1], [#Channel2]...[#ChannelN]\nClear");
        msgEvent.getChannel().sendMessage(embed.build()).queue();
        while (true)
        {
            Message msg = mm.getNextMessage(msgEvent.getChannel(), msgEvent.getMember());
            String msgContent = msg.getContentDisplay().toLowerCase();
            if (msgContent.startsWith("add") || msgContent.startsWith("remove"))
            {
                boolean isAdd = msgContent.startsWith("add");
                setWhitelistChannels(msgEvent, isAdd, msg.getMentionedChannels());
                return;
            }
            else if (msgContent.equalsIgnoreCase("clear"))
            {
                clearWhitelistChannels(msgEvent);
                return;
            }
            else if (msgContent.equalsIgnoreCase("quit") || msgContent.equalsIgnoreCase(guildSettings.getCommandPrefix()+"quit"))
            {
                embed.setDescription("Menu closed.");
                msgEvent.getChannel().sendMessage(embed.build()).queue();
                return;
            }
            else
            {
                embed.setDescription("Invalid input. Please try again.");
                msgEvent.getChannel().sendMessage(embed.build()).queue();
            }
        }
    }

    private void setWhitelistChannels(GuildMessageReceivedEvent msgEvent, boolean isAdd, List<TextChannel> channels) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        if (channels.size() > 0)
        {
            String[] channelIDs = new String[channels.size()];
            for (int i = 0; i<channels.size(); i++)
            {
                channelIDs[i] = channels.get(i).getId();
            }

            if (isAdd)
                guildSettings.addChannelsToWhitelist(channelIDs);
            else
                guildSettings.removeChannelsFromWhitelist(channelIDs);

            embed.setDescription("Channel whitelist updated.");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
        else
        {
            embed.setDescription("No channels were provided.");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
    }

    private void clearWhitelistChannels(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        guildSettings.removeChannelsFromWhitelist(guildSettings.getChannelWhitelist().toArray(new String[0]));
        embed.setDescription("Whitelist cleared.");
        msgEvent.getChannel().sendMessage(embed.build()).queue();
    }
}
