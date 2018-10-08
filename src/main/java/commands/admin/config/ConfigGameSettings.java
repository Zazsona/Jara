package commands.admin.config;

import configuration.GuildSettings;
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
    public ConfigGameSettings(GuildSettings guildSettings, TextChannel channel)
    {
        this.guildSettings = guildSettings;
        this.channel = channel;
    }

    public void showMenu(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        embed.setDescription("What would you like to modify?\n**Game Channels**\n**Game Category**\n**Channel Timeout**");
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
                    break;
                }
                else if (msgContent.equalsIgnoreCase("channel timeout") || msgContent.equalsIgnoreCase("channeltimeout"))
                {
                    modifyChannelTimeout(msgEvent);
                    break;
                }
                else if (msgContent.equalsIgnoreCase("game channels") || msgContent.equalsIgnoreCase("gamechannels"))
                {
                    modifyGameChannels(msgEvent);
                    break;
                }
                else
                {
                    embed.setDescription("Unrecognised category. Please try again.");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }

    private void modifyGameChannels(GuildMessageReceivedEvent msgEvent)
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
                    try
                    {
                        guildSettings.save();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        embed.setDescription("An error occurred when saving settings.");
                        channel.sendMessage(embed.build()).queue();
                    }

                    embed.setDescription("Game Channels has been enabled.");
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                else if (response.startsWith("n"))
                {
                    guildSettings.setUseGameChannels(false);
                    try
                    {
                        guildSettings.save();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        embed.setDescription("An error occurred when saving settings.");
                        channel.sendMessage(embed.build()).queue();
                    }
                    embed.setDescription("Game Channels has been disabled.");
                    channel.sendMessage(embed.build()).queue();
                    break;
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
    private void modifyGameCategory(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        StringBuilder descBuilder = new StringBuilder();
        if (!guildSettings.getGameCategoryId().equals(""))
        {
            descBuilder.append("Current value: **").append(guildSettings.getGameCategoryId()).append("**\n\n");
        }
        descBuilder.append("This setting will allow you to select which channel category the bot will create game channels in.\n\nPlease enter a channel category name or ID below.");
        embed.setDescription(descBuilder.toString());
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = new MessageManager().getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                if (msg.getContentDisplay().length() == 5 && msg.getContentDisplay().toLowerCase().endsWith("quit"))
                {
                    embed.setDescription("Config closed.");
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                String id = msg.getContentDisplay();
                if (Pattern.matches(id, "[0-9]*"))
                {
                    if (channel.getGuild().getCategoryById(id) == null)
                    {
                        embed.setDescription("Category does not exist: "+id);
                        channel.sendMessage(embed.build()).queue();
                    }
                    else
                    {
                        guildSettings.setGameCategoryId(id);
                        try
                        {
                            guildSettings.save();
                            embed.setDescription("Game Category Id set to: "+id);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            embed.setDescription("An error occurred when saving to config.");
                        }
                        finally
                        {
                            channel.sendMessage(embed.build()).queue();
                        }
                        break;
                    }
                }
                else
                {
                    if (channel.getGuild().getCategoriesByName(id, true).size() == 0)
                    {
                        embed.setDescription("Category does not exist: "+id);
                        channel.sendMessage(embed.build()).queue();
                    }
                    else
                    {
                        id = channel.getGuild().getCategoriesByName(id, true).get(0).getId();
                        guildSettings.setGameCategoryId(id);
                        try
                        {
                            guildSettings.save();
                            embed.setDescription("Game Category Id set to: "+id);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            embed.setDescription("An error occurred when saving to config.");
                        }
                        finally
                        {
                            channel.sendMessage(embed.build()).queue();
                        }
                        break;
                    }
                }
            }
        }


    }
    private void modifyChannelTimeout(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        StringBuilder descBuilder = new StringBuilder();
        if (guildSettings.getGameChannelTimeout().equals("0"))
        {
            descBuilder.append("Current value: **Disabled**");
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
                if (msg.getContentDisplay().length() == 5 && msg.getContentDisplay().toLowerCase().endsWith("quit"))
                {
                    embed.setDescription("Config closed.");
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                String timeout = msg.getContentDisplay();
                if (Pattern.matches(timeout, "[0-9]*"))
                {
                    guildSettings.setGameChannelTimeout(timeout);
                    try
                    {
                        guildSettings.save();
                        if (timeout.length() >= 3)
                        {
                            embed.setDescription("Game Channel Timeout set to a whopping "+timeout+"\n\nRemember, this feature can be disabled by setting minutes to 0.");
                        }
                        else
                        {
                            embed.setDescription("Game Channel Timeout set to: "+timeout);
                        }

                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        embed.setDescription("An error occurred when saving to config.");
                    }
                    finally
                    {
                        channel.sendMessage(embed.build()).queue();
                    }
                    break;
                }
                else
                {
                    embed.setDescription("Could not find a valid timeout value");
                    channel.sendMessage(embed.build());
                }
            }

        }
    }



}
