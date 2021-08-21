package com.zazsona.jara.commands;

import com.zazsona.jara.configuration.SettingsUtil;
import com.zazsona.jara.configuration.guild.CustomCommandBuilder;
import com.zazsona.jara.module.ModuleCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import java.util.ArrayList;

public class CustomCommand extends ModuleCommand
{

    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        try
        {
            String key = getKey(msgEvent.getGuild().getId(), parameters[0]);
            CustomCommandBuilder customCommand = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).getCustomCommandSettings().getCommand(key);

            runMessage(msgEvent, customCommand);
            runRoles(msgEvent, customCommand);
            runAudio(msgEvent, customCommand);
        }
        catch (NullPointerException e)
        {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            embed.setTitle("Error");
            embed.setDescription("You must specify a custom command key."); //Reeeeally I'd rather they not use this method anyway, but hey, no harm in having the option.
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
        catch (InsufficientPermissionException e)
        {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            embed.setTitle("Error");
            embed.setDescription("I do not have sufficient permissions / role hierarchy to fully perform the command.");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
    }

    /**
     * Runs the audio associated with this command
     * @param msgEvent context
     * @param customCommand the command's data
     */
    private void runAudio(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand)
    {
        if (!customCommand.getAudioLink().equals(""))
        {
            if (customCommand.getMessage().equals("") || msgEvent.getMessage().getMember().getVoiceState().inVoiceChannel())
            {
                CmdUtil.getGuildAudio(msgEvent.getGuild().getId()).playWithFeedback(msgEvent.getMember(), customCommand.getAudioLink(), msgEvent.getChannel());
            }
        }
    }

    /**
     * Toggles the roles associated with this command
     * @param msgEvent context
     * @param customCommand the command's data
     */
    private void runRoles(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand)
    {
        if (customCommand.getRoles().size() > 0)
        {
            ArrayList<Role> roles = new ArrayList<>();
            for (String roleID : customCommand.getRoles())
            {
                Role role = msgEvent.getGuild().getRoleById(roleID);
                if (role != null)
                {
                    roles.add(role);
                }
            }
            msgEvent.getGuild().modifyMemberRoles(msgEvent.getMember(), roles, null).queue();
        }
    }

    /**
     * Prints the message associated with this command
     * @param msgEvent context
     * @param customCommand the command's data
     */
    private void runMessage(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand)
    {
        if (!customCommand.getMessage().equals(""))
        {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            try
            {
                String lcMessage = customCommand.getMessage().toLowerCase();
                String url = "";
                if (lcMessage.contains(".png") || lcMessage.contains(".jpg") || lcMessage.contains(".jpeg") || lcMessage.contains(".gif"))
                {
                    for (String word : customCommand.getMessage().split(" "))
                    {
                        String lcWord = word.toLowerCase();
                        if (lcWord.contains(".png") || lcWord.contains(".jpg") || lcWord.contains(".jpeg") || lcWord.contains(".gif"))
                        {
                            embed.setImage(word);
                            url = word;
                        }
                    }
                }
                embed.setDescription(customCommand.getMessage().replace(url, ""));
            }
            catch (IllegalArgumentException e)
            {
                embed.setImage(null);
                embed.setDescription(customCommand.getMessage());
            }
            finally
            {
                msgEvent.getChannel().sendMessage(embed.build()).queue();
            }
        }
    }

    /**
     * Returns the key used to call the command
     * @param guildID the guild in which the command was called
     * @param call the command request
     * @return the call key
     */
    private String getKey(String guildID, String call)
    {
        return call.replaceFirst(SettingsUtil.getGuildCommandPrefix(guildID).toString(), "");
    }
}
