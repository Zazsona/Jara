package commands;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.GuildUnavailableException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Timer;
import java.util.TimerTask;

public abstract class Command //A base class to build commands from.
{
	public abstract void run(GuildMessageReceivedEvent msgEvent, String... parameters);
}
