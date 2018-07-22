package event;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import configuration.GuildSettingsManager;
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
		GuildSettingsManager guildSettings = new GuildSettingsManager(joinEvent.getGuild().getId());
		guildSettings.performNewGuildSetup();
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(Core.getHighlightColour(joinEvent.getGuild().getSelfMember()));
		embed.setDescription("Hey there, "+owner.getName()+"!\nI'm just sending you this to tell you I'm all ready to go in "+guild.getName()+".\n\nTo get started, use `/config` in your guild with a channel I have access to, there you will be able to enable commands and features!");
		owner.openPrivateChannel().complete().sendMessage(embed.build()).queue(); //TODO: Make this prettier.
		return;
	}
}
