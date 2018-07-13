package event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import configuration.GuildSettingsManager;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class GuildJoinHandler extends ListenerAdapter 
{
	@Override
	public void onGuildJoin(GuildJoinEvent joinEvent)
	{
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.info("Joined guild "+joinEvent.getGuild().getId()+". Creating a config.");
		GuildSettingsManager guildSettings = new GuildSettingsManager(joinEvent.getGuild().getId());
		guildSettings.performNewGuildSetup();
		joinEvent.getGuild().getOwner().getUser().openPrivateChannel().complete().sendMessage("Hey there, I'm just sending you this to tell you I'm all ready to go.\nTo get started, use ```/config``` in your guild with a channel I have access to, there you will be able to enable commands and features!").queue();
		return;
	}
}

