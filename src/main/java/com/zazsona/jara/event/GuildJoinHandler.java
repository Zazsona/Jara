package com.zazsona.jara.event;

import com.zazsona.jara.commands.CmdUtil;
import com.zazsona.jara.configuration.SettingsUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the bot being added to new guilds.
 */
public class GuildJoinHandler extends ListenerAdapter
{
	@Override
	public void onGuildJoin(GuildJoinEvent joinEvent)
	{
		Guild guild = joinEvent.getGuild();
		User owner = joinEvent.getGuild().getOwner().getUser();
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.info("Joined guild "+joinEvent.getGuild().getId());
		SettingsUtil.getGuildSettings(joinEvent.getGuild().getId()); //This creates the guild settings.
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(CmdUtil.getHighlightColour(joinEvent.getGuild().getSelfMember()));
		embed.setDescription("Hey there, "+owner.getName()+"!\nI'm just sending you this to tell you I'm all ready to go in "+guild.getName()+".\n" +
									 "\nTo get started, use `/config` in your guild with a channel I have access to, there you will be able to enable commands and features!\n\n" +
									 "**Quick Links to get you started:**\n" +
									 "[Source Code](https://github.com/Zazsona/Jara)\n" +
									 "[Modules](https://github.com/Zazsona/JaraModules)\n" +
									 "[Tutorial](https://dothis.com)\n"); //TODO
		owner.openPrivateChannel().complete().sendMessage(embed.build()).queue();
		return;
	}
}

