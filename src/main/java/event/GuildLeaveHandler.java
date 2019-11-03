package event;

import configuration.SettingsUtil;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * Handles the bot getting removes from a guild
 */
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
