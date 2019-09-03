package jara;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import javax.security.auth.login.LoginException;

import configuration.SettingsUtil;
import exceptions.InvalidModuleException;
import gui.HeadedGUI;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import configuration.CommandLauncher;
import event.GuildJoinHandler;
import event.GuildLeaveHandler;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;

public class Core //A class for covering the global manners of the bot.
{
	private static Logger logger = LoggerFactory.getLogger(Core.class);
	private static ShardManager shardManager;

	private static final HashSet<String> supportedVersions = new HashSet<>();
	private static final String VERSION = "0.1";

	/**
	 * Gets the current Jara version
	 * @return
	 */
	public static String getVersion()
	{
		return VERSION;
	}

	/**
	 * Gets a set of all past Jara versions with which modules and saves should be compatible with.
	 * If an update breaks the API, this set is to be wiped.
	 * @return
	 */
	public static HashSet<String> getSupportedVersions()
	{
		if (supportedVersions.size() == 0)
		{
			Collections.addAll(supportedVersions, "0.1", VERSION);
		}
		return supportedVersions;
	}


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
	    	logger.error(e.toString());
	    	return false;
		}
	}
	public static void enableCommands()
	{
		try
		{
			HashMap<String, CommandLauncher> commands = new HashMap<>();
			for (ModuleAttributes ma : ModuleRegister.getCommandModules())
			{
				CommandLauncher cl = new CommandLauncher(ma, SettingsUtil.getGlobalSettings().isModuleEnabled(ma.getKey()));
				for (String alias : ma.getAliases())
				{
					commands.put(alias.toLowerCase(), cl);
				}
			}
			shardManager.addEventListener(new CommandHandler(commands));
		}
		catch (InvalidModuleException e)
		{
			logger.error(e.toString()+"\nA log has been created.");
			if (!GraphicsEnvironment.isHeadless())
			{
				HeadedGUI.showError(e.toString()+"\nA log has been created.");
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
}
