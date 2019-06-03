package event;

import configuration.SettingsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.IOException;

public class GuildLeaveHandler extends ListenerAdapter 
{
	@Override
	public void onGuildLeave(GuildLeaveEvent leaveEvent)
	{
		Logger logger = LoggerFactory.getLogger(getClass());
		try
		{
			logger.info("Oh dear! I've been kicked out of guild "+leaveEvent.getGuild().getName()+" ("+leaveEvent.getGuild().getId()+"). Deleting guild config.");
			SettingsUtil.getGuildSettings(leaveEvent.getGuild().getId()).delete();
			return;
		}
		catch (IOException e)
		{
			logger.error("Unable to remove guild settings for "+leaveEvent.getGuild().getName()+" ("+leaveEvent.getGuild().getId()+")");
		}

	}
}
