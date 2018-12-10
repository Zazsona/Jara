package jara;

import java.awt.*;
import java.util.HashMap;

import javax.security.auth.login.LoginException;

import configuration.SettingsUtil;
import exceptions.InvalidModuleException;
import gui.HeadedGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import configuration.CommandLauncher;
import event.GuildJoinHandler;
import event.GuildLeaveHandler;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Member;

public class Core //A class for covering the global manners of the bot.
{
	private static Logger logger = LoggerFactory.getLogger(Core.class);
	private static ShardManager shardManager;
	public static boolean initialiseDiscordConnection(String clientToken)
	{
		Logger logger = LoggerFactory.getLogger(Core.class);
	    try
		{
		    DefaultShardManagerBuilder shardManagerBuilder = new DefaultShardManagerBuilder();
		    shardManagerBuilder.setToken(clientToken);
		    shardManager = shardManagerBuilder.build();
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
		try
		{
			HashMap<String, CommandLauncher> commands = new HashMap<>();
			for (CommandAttributes ca : CommandRegister.getRegister())
			{
				CommandLauncher cl = new CommandLauncher(ca, SettingsUtil.getGlobalSettings().isCommandEnabled(ca.getCommandKey()));
				for (String alias : ca.getAliases())
				{
					commands.put(alias.toLowerCase(), cl);
				}
			}
			shardManager.addEventListener(new CommandHandler(commands));
		}
		catch (InvalidModuleException e)
		{
			logger.error(e.getMessage()+"\nA log has been created.");
			if (!GraphicsEnvironment.isHeadless())
			{
				HeadedGUI.showError(e.getMessage()+"\nA log has been created.");
			}
		}

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
