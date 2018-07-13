package event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import configuration.GuildSettingsManager;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class GuildLeaveHandler extends ListenerAdapter 
{
	@Override
	public void onGuildLeave(GuildLeaveEvent leaveEvent)
	{
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.info("Oh dear! I've been kicked out of guild "+leaveEvent.getGuild().getId()+". Deleting guild config.");
		GuildSettingsManager guildSettings = new GuildSettingsManager(leaveEvent.getGuild().getId());
		guildSettings.deleteGuildSettingsFile();
		return;
	}
}
