package jara;

import java.awt.Color;

import javax.security.auth.login.LoginException;

import configuration.SettingsUtil;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import configuration.CommandConfiguration;
import event.GuildJoinHandler;
import event.GuildLeaveHandler;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Member;

public class Core //A class for covering the global manners of the bot.
{
	private static ShardManager shardManager;
	public static boolean initialiseDiscordConnection(String clientToken)
	{
		Logger logger = LoggerFactory.getLogger(Core.class);
	    try
		{
		    DefaultShardManagerBuilder shardManagerBuilder = new DefaultShardManagerBuilder();
		    shardManagerBuilder.setToken(clientToken);
		    shardManager = shardManagerBuilder.build();
		    logger.info("Logged in!");
		    return true;
		}
	    catch (LoginException | IllegalArgumentException e)
		{
	    	logger.error("Failed to log in.");
	    	e.printStackTrace();
	    	return false;
		}
	}
	public static void enableCommands()
	{
		CommandConfiguration[] commandConfigs = new CommandConfiguration[CommandRegister.getRegisterSize()];
		for (int i = 0; i<commandConfigs.length; i++)
		{
			commandConfigs[i] = new CommandConfiguration(CommandRegister.getRegister()[i], SettingsUtil.getGlobalSettings().isCommandEnabled(CommandRegister.getRegister()[i].getCommandKey()));
		}
		shardManager.addEventListener(new CommandHandler(commandConfigs));
	}
	public static void startListeners()
	{
		shardManager.addEventListener(new GuildJoinHandler());
		shardManager.addEventListener(new GuildLeaveHandler());
	}
	public static ShardManager getShardManager()
	{
		return shardManager;
	}
	public static Color getHighlightColour(Member selfMember)
	{
		try
		{
			return selfMember.getRoles().get(0).getColor(); //Try to set it to the bot's primary role colour
		}
		catch (IndexOutOfBoundsException | NullPointerException e)	//If the bot has no role
		{
			return Color.decode("#5967cf"); //Use a default theme.
		}
	}
}
