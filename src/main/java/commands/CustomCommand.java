package commands;

import configuration.GuildSettingsJson;
import configuration.SettingsUtil;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class CustomCommand extends Command
{

    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        String key = getKey(msgEvent.getGuild().getId(), parameters[0]);
        GuildSettingsJson.CustomCommandConfig customCommand = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId()).getCustomCommand(key);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setTitle(key);
        embed.setDescription(customCommand.getMessage());
        msgEvent.getChannel().sendMessage(embed.build()).queue();
    }
    private String getKey(String guildID, String call)
    {
        return call.replaceFirst(SettingsUtil.getGuildCommandPrefix(guildID).toString(), "");
    }
    //TODO: Implement this properly (Tidy up, allow for multiple custom command types (Role assignment, message response, voice channel track)
}
