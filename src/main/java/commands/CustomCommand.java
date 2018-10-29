package commands;

import commands.audio.Play;
import configuration.GuildSettingsJson;
import configuration.SettingsUtil;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;

import java.util.ArrayList;

public class CustomCommand extends Command
{

    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        try
        {
            String key = getKey(msgEvent.getGuild().getId(), parameters[0]);
            GuildSettingsJson.CustomCommandConfig customCommand = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).getCustomCommand(key);

            if (!customCommand.getMessage().equals(""))
            {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
                embed.setTitle(key);
                embed.setDescription(customCommand.getMessage());
                msgEvent.getChannel().sendMessage(embed.build()).queue();
            }
            if (customCommand.getRoles().size() > 0)
            {
                ArrayList<Role> roles = new ArrayList<>();
                for (String roleID : customCommand.getRoles())
                {
                    roles.add(msgEvent.getGuild().getRoleById(roleID));
                }
                msgEvent.getGuild().getController().addRolesToMember(msgEvent.getMember(), roles).queue();
            }
            if (!customCommand.getAudioLink().equals(""))
            {
                new Play().run(msgEvent, SettingsUtil.getGuildCommandPrefix(msgEvent.getGuild().getId())+"play", customCommand.getAudioLink());
            }
        }
        catch (NullPointerException e)
        {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            embed.setTitle("Error");
            embed.setDescription("You must specify a custom command key."); //Reeeeally I'd rather they not use this method anyway, but hey, no harm in having the option.
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
        catch (InsufficientPermissionException e)
        {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            embed.setTitle("Error");
            embed.setDescription("I do not have sufficient permissions / role hierarchy to fully perform the command.");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
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
