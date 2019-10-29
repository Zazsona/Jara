package commands.admin.config;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ConfigAudioSettings
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
    public ConfigAudioSettings(GuildSettings guildSettings, TextChannel channel, ConfigMain configMain)
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
    public void parseAsParameter(GuildMessageReceivedEvent msgEvent, String[] parameters) throws IOException
    {
        if (parameters.length > 2)
        {
            String request = parameters[2].toLowerCase();
            if (request.equalsIgnoreCase("skipvotes"))
            {
                modifySkipVotes(msgEvent);
            }
            else if (request.equalsIgnoreCase("voiceleaving"))
            {
                modifyVoiceLeaving(msgEvent);
            }
            else if (request.equalsIgnoreCase("queuelimits"))
            {
                modifyQueueLimits(msgEvent);
            }
            else
            {
                EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
                embed.setDescription("Unrecognised audio category.");
                channel.sendMessage(embed.build()).queue();
            }
        }
        else
        {
            showMenu(msgEvent);
        }
    }

    /**
     * Shows the main navigation menu for audio settings, and directs the user to a submenu or exiting.
     * @param msgEvent context
     * @throws IOException unable to write to file
     */
    public void showMenu(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        String embedDescription = "What would you like to modify?\nSay `quit` to close this menu.\n**Skip Votes**\n**Voice Leaving**\n**Queue Limits**";
        embed.setDescription(embedDescription);
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = new MessageManager().getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), configMain.getModuleAttributes().getKey())) //If the message is from someone with config permissions
            {
                String msgContent = msg.getContentDisplay();
                if (msgContent.equalsIgnoreCase("skip votes") || msgContent.equalsIgnoreCase("skipvotes"))
                {
                    modifySkipVotes(msgEvent);
                }
                else if (msgContent.equalsIgnoreCase("voice leaving") || msgContent.equalsIgnoreCase("voiceleaving"))
                {
                    modifyVoiceLeaving(msgEvent);
                }
                else if (msgContent.equalsIgnoreCase("queue limits") || msgContent.equalsIgnoreCase("queuelimits"))
                {
                    modifyQueueLimits(msgEvent);
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
     * Takes user input and modifies the percentage of people needing to vote to skip a playing track.
     * @param msgEvent context
     * @throws IOException unable to write to file
     */
    public void modifySkipVotes(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        StringBuilder descBuilder = new StringBuilder();
        if (!guildSettings.getGameCategoryId().equals(""))
        {
            descBuilder.append("Current value: **").append(guildSettings.getTrackSkipPercent()).append("%**\n\n");
        }
        descBuilder.append("This setting defines what percentage of people in a voice channel need to vote to skip the track.\nPlease enter a percentage.");
        embed.setDescription(descBuilder.toString());
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = new MessageManager().getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), configMain.getModuleAttributes().getKey())) //If the message is from someone with config permissions
            {
                String response = msg.getContentDisplay();
                response = response.replace("%", "");
                if (Pattern.matches("[0-9]*", response))
                {
                    int percentage = Integer.parseInt(response);
                    if (percentage < 0 || percentage > 100)
                    {
                        embed.setDescription("Invalid percentage value. Please enter a value between 0 and 100.");
                        channel.sendMessage(embed.build()).queue();
                    }
                    else
                    {
                        guildSettings.setTrackSkipPercent(Integer.parseInt(response));
                        embed.setDescription("Skip percentage has been set to "+response+"%.");
                        channel.sendMessage(embed.build()).queue();
                        break;
                    }
                }
                else if (response.equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || response.equalsIgnoreCase("quit"))
                {
                    return;
                }
                else
                {
                    embed.setDescription("Unknown percentage value. Please enter a percentage.");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }

    /**
     * Takes user input and modifies whether to have the bot leave the channel when nothing is playing
     * @param msgEvent context
     * @throws IOException unable to write to file
     */
    public void modifyVoiceLeaving(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        String descBuilder = "Current value: **" + guildSettings.isVoiceLeavingEnabled() + "**\n\n" +
                "This setting will make it so the bot leaves the voice channel when no audio is playing.\n\n Would you like to enable it? [Y/n]";
        embed.setDescription(descBuilder);
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = new MessageManager().getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), configMain.getModuleAttributes().getKey())) //If the message is from someone with config permissions
            {
                String response = msg.getContentDisplay().toLowerCase();
                if (response.startsWith("y"))
                {
                    guildSettings.setVoiceLeaving(true);
                    embed.setDescription("Voice Leaving has been enabled.");
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                else if (response.startsWith("n"))
                {
                    guildSettings.setVoiceLeaving(false);
                    embed.setDescription("Voice Leaving has been disabled.");
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                else if (response.equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || response.equalsIgnoreCase("quit"))
                {
                    return;
                }
                else
                {
                    embed.setDescription("Unknown response. Would you like to enable voice leaving? [Y/n]");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }

    /**
     * Opens a sub-menu, prompting the user to enter how many tracks a single role can queue, and writing it to file.
     * @param msgEvent context
     * @throws IOException unable to write to file
     */
    public void modifyQueueLimits(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        HashMap<Role, Integer> customRoles = new HashMap<>();
        int defaultLimit = guildSettings.getAudioQueueLimit(msgEvent.getGuild().getPublicRole());
        for (Role role : msgEvent.getGuild().getRoles())
        {
            int limit = guildSettings.getAudioQueueLimit(role);
            if (limit != defaultLimit)
            {
                customRoles.put(role, limit);
            }
        }
        StringBuilder descBuilder = new StringBuilder();
        descBuilder.append("Queue limits dictate how many tracks a member of that role can queue at a time.\nTo set/edit a role, enter the role name followed by the queue limit.\nUse the quit command to exit.\nE.g: everyone 2\n\n**Existing Settings:**\n");
        descBuilder.append("Everyone: ").append(defaultLimit).append("\n");
        for (Map.Entry<Role, Integer> roleLimit : customRoles.entrySet())
        {
            descBuilder.append(roleLimit.getKey().getName()).append(": ").append(roleLimit.getValue()).append("\n");
        }
        if (descBuilder.length() >= 1024)
        {
            descBuilder.setLength(1020);
            descBuilder.append("...");
        }
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        embed.setDescription(descBuilder.toString());
        channel.sendMessage(embed.build()).queue();
        MessageManager mm = new MessageManager();
        while (true)
        {
            try
            {
                Message msg = mm.getNextMessage(channel);
                if (guildSettings.isPermitted(msg.getMember(), configMain.getModuleAttributes().getKey())) //If the message is from someone with config permissions
                {
                    String response = msg.getContentDisplay().toLowerCase();
                    String[] responseWords = response.split(" ");
                    String roleName = response.substring(0, response.length()-responseWords[responseWords.length-1].length()).trim();
                    if (responseWords[0].equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || responseWords[0].equalsIgnoreCase("quit"))
                    {
                        return;
                    }
                    else
                    {
                        Role role = (roleName.equals("everyone")) ? msgEvent.getGuild().getPublicRole() : msgEvent.getGuild().getRolesByName(roleName, true).get(0);
                        int limit = Integer.parseInt(responseWords[responseWords.length-1]);
                        guildSettings.setAudioQueueLimit(role, limit);
                        embed.setDescription("Queue limit updated!");
                        channel.sendMessage(embed.build()).queue();
                        return;
                    }

                }
            }
            catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException e)
            {
                embed.setDescription("Invalid response format. To cancel, use the quit command.\n\nRequired: [Role name] [Limit]\nE.g: everyone 2");
                channel.sendMessage(embed.build()).queue();
            }
        }

    }
}
