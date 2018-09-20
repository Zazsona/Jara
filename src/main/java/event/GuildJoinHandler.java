package event;

import java.awt.Color;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class GuildJoinHandler extends ListenerAdapter 
{
	@Override
	public void onGuildJoin(GuildJoinEvent joinEvent)
	{
		Guild guild = joinEvent.getGuild();
		User owner = joinEvent.getGuild().getOwner().getUser();
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.info("Joined guild "+joinEvent.getGuild().getId());
		SettingsUtil.addNewGuild(joinEvent.getGuild().getId());
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(Core.getHighlightColour(joinEvent.getGuild().getSelfMember()));
		embed.setDescription("Hey there, "+owner.getName()+"!\nI'm just sending you this to tell you I'm all ready to go in "+guild.getName()+".\n" +
									 "\nTo get started, use `/config` in your guild with a channel I have access to, there you will be able to enable commands and features!\n\n" +
									 "**Quick Links to get you started:**\n" +
									 "[Source Code](https://github.com/Zazsona/Jara)\n" +
									 "[Command List](https://dothis.com)\n" +
									 "[Tutorial](https://dothis.com)\n"); //TODO
		owner.openPrivateChannel().complete().sendMessage(embed.build()).queue();
		return;
	}
}

