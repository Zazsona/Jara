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

public class ConfigGameSettings
{
    private final GuildSettings guildSettings;
    private final TextChannel channel;

    /**
     * Constructor
     * @param guildSettings the guild settings to modify
     * @param channel the channel to run in
     */
    public ConfigGameSettings(GuildSettings guildSettings, TextChannel channel)
    {
        this.guildSettings = guildSettings;
        this.channel = channel;
    }

    /**
     * Runs through the config using the navigation options supplied in a single message
     * @param msgEvent context
     * @param parameters the parameters to parse
     * @throws IOException unable to write to file
     */
    public void parseAsParameter(GuildMessageReceivedEvent msgEvent, String[] parameters) throws IOException
    {
        if (parameters.length > 2)
        {
            String request = parameters[2].toLowerCase();
            if (request.equalsIgnoreCase("gamecategory"))
            {
                modifyGameCategory(msgEvent);
            }
            else if (request.equalsIgnoreCase("channeltimeout"))
            {
                modifyChannelTimeout(msgEvent);
            }
            else if (request.equalsIgnoreCase("gamechannels"))
            {
                modifyGameChannels(msgEvent);
            }
            else if (request.equalsIgnoreCase("concurrentgames"))
            {
                modifyConcurrentGameInChannel(msgEvent);
            }
            else
            {
                EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
                embed.setDescription("Unrecognised game category.");
                channel.sendMessage(embed.build()).queue();
            }
        }
        else
        {
            showMenu(msgEvent);
        }
    }

    /**
     * Shows the main navigation menu for game settings, and directs the user to a submenu or exiting.
     * @param msgEvent context
     * @throws IOException unable to write to file
     */
    public void showMenu(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        String embedDescription = "What would you like to modify?\nSay `quit` to close this menu.\n**Game Channels**\n**Game Category**\n**Channel Timeout**\n**Concurrent Games**";
        embed.setDescription(embedDescription);
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = new MessageManager().getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                String msgContent = msg.getContentDisplay();
                if (msgContent.equalsIgnoreCase("Game category") || msgContent.equalsIgnoreCase("gamecategory"))
                {
                    modifyGameCategory(msgEvent);
                }
                else if (msgContent.equalsIgnoreCase("channel timeout") || msgContent.equalsIgnoreCase("channeltimeout"))
                {
                    modifyChannelTimeout(msgEvent);
                }
                else if (msgContent.equalsIgnoreCase("game channels") || msgContent.equalsIgnoreCase("gamechannels"))
                {
                    modifyGameChannels(msgEvent);
                }
                else if (msgContent.equalsIgnoreCase("concurrent games") || msgContent.equalsIgnoreCase("concurrentgames"))
                {
                    modifyConcurrentGameInChannel(msgEvent);
                }
                else if (msgContent.equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || msgContent.equalsIgnoreCase("quit"))
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

    /**
     * Takes user input and modifies whether to create unique channels for each game.
     * @param msgEvent context
     * @throws IOException unable to write to file
     */
    public void modifyGameChannels(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        StringBuilder descBuilder = new StringBuilder();
        if (!guildSettings.getGameCategoryId().equals(""))
        {
            descBuilder.append("Current value: **").append(guildSettings.isGameChannelsEnabled()).append("**\n\n");
        }
        descBuilder.append("This setting will allow the bot to create a new channel for each game, reducing clutter. Please note a channel category must be set to use this feature.\n\n Would you like to enable it? [Y/n]");
        embed.setDescription(descBuilder.toString());
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = new MessageManager().getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                String response = msg.getContentDisplay().toLowerCase();
                if (response.startsWith("y"))
                {
                    guildSettings.setUseGameChannels(true);
                    embed.setDescription("Game Channels has been enabled.");
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                else if (response.startsWith("n"))
                {
                    guildSettings.setUseGameChannels(false);
                    embed.setDescription("Game Channels has been disabled.");
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                else if (response.equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || response.equalsIgnoreCase("quit"))
                {
                    return;
                }
                else
                {
                    embed.setDescription("Unknown response. Would you like to enable game channels? [Y/n]");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
        if (guildSettings.isGameChannelsEnabled() && guildSettings.getGameCategoryId().equals(""))
        {
            modifyGameCategory(msgEvent);
        }
    }

    /**
     * Takes user input and modifies which channel category to create game channels in
     * @param msgEvent context
     * @throws IOException unable to write to file
     */
    public void modifyGameCategory(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        StringBuilder descBuilder = new StringBuilder();
        if (!guildSettings.getGameCategoryId().equals(""))
        {
            descBuilder.append("Current value: **").append(msgEvent.getGuild().getCategoryById(guildSettings.getGameCategoryId()).getName()).append("**\n\n");
        }
        descBuilder.append("This setting will allow you to select which channel category the bot will create game channels in.\n\nPlease enter a channel category name or ID below.");
        embed.setDescription(descBuilder.toString());
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = new MessageManager().getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                String id = msg.getContentDisplay().trim();
                if (Pattern.matches("^[0-9]+$", id))
                {
                    if (channel.getGuild().getCategoryById(id) == null)
                    {
                        embed.setDescription("Category ID does not exist: "+id);
                        channel.sendMessage(embed.build()).queue();
                    }
                    else
                    {
                        guildSettings.setGameCategoryId(id);
                        embed.setDescription("Game Category set to "+msgEvent.getGuild().getCategoryById(id).getName());
                        channel.sendMessage(embed.build()).queue();
                        break;
                    }
                }
                else
                {
                    if (id.equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || id.equalsIgnoreCase("quit"))
                    {
                        return;
                    }
                    else if (channel.getGuild().getCategoriesByName(id, true).size() == 0)
                    {
                        embed.setDescription("Category does not exist: "+id+". Please try again.");
                        channel.sendMessage(embed.build()).queue();
                    }
                    else
                    {
                        id = channel.getGuild().getCategoriesByName(id, true).get(0).getId();
                        guildSettings.setGameCategoryId(id);
                        embed.setDescription("Game Category set to "+msgEvent.getGuild().getCategoryById(id).getName());
                        channel.sendMessage(embed.build()).queue();
                        break;
                    }
                }
            }
        }


    }

    /**
     * Takes user input and modifies the time it takes for a game channel with no activity to be deleted
     * @param msgEvent context
     * @throws IOException unable to write to file
     */
    public void modifyChannelTimeout(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        StringBuilder descBuilder = new StringBuilder();
        if (guildSettings.getGameChannelTimeout().equals("0"))
        {
            descBuilder.append("Current value: **Disabled**\n\n");
        }
        else
        {
            descBuilder.append("Current value: **").append(guildSettings.getGameChannelTimeout()).append("**\n\n");
        }
        descBuilder.append("How long after the last message a game channel is deleted.\nPlease enter the number of minutes channels should last for after the last message, or 0 to disable this feature.");
        embed.setDescription(descBuilder.toString());
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = new MessageManager().getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                String timeout = msg.getContentDisplay();
                if (Pattern.matches("[0-9]*", timeout))
                {
                    guildSettings.setGameChannelTimeout(timeout);
                    if (timeout.length() >= 10)
                    {
                        embed.setDescription("Game Channel Timeout set to a whopping "+timeout+"\n\nRemember, this feature can be disabled by setting minutes to 0.");
                    }
                    else
                    {
                        embed.setDescription("Game Channel Timeout set to: "+timeout);
                    }
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                else if (timeout.equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || timeout.equalsIgnoreCase("quit"))
                {
                    return;
                }
                else
                {
                    embed.setDescription("Could not find a valid timeout value. Please try again.");
                    channel.sendMessage(embed.build()).queue();
                }
            }

        }
    }

    /**
     * Takes user input and modifies if multiple games can run in a single channel
     * @param msgEvent context
     * @throws IOException unable to write to file
     */
    public void modifyConcurrentGameInChannel(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        String descBuilder = "Current value: **" + guildSettings.isConcurrentGameInChannelAllowed() + "**\n\n" +
                "This setting modified the ability to have multiple games run simultaneously in the same channel. The setting is only relevant if game channels are disabled.\n\n Enable concurrent games in a single channel? [Y/n]";
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
                    guildSettings.setConcurrentGameInChannelAllowed(true);
                    embed.setDescription("Concurrent games have been enabled.");
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                else if (response.startsWith("n"))
                {
                    guildSettings.setConcurrentGameInChannelAllowed(false);
                    embed.setDescription("Concurrent games have been disabled.");
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                else if (response.equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || response.equalsIgnoreCase("quit"))
                {
                    return;
                }
                else
                {
                    embed.setDescription("Unknown response. Would you like to enable concurrent games in a channel? [Y/n]");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }

    }


}
