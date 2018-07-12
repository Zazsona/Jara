package jara;

import java.util.HashMap;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;

public class Core //A class for covering the global manners of the bot.
{
	static ShardManager shardManager;
	public static void initialiseDiscordConnection(String clientToken)
	{
		Logger logger = LoggerFactory.getLogger(Core.class);
	    try
		{
		    DefaultShardManagerBuilder shardManagerBuilder = new DefaultShardManagerBuilder();
		    shardManagerBuilder.setToken(clientToken);
		    shardManager = shardManagerBuilder.build();
		    logger.info("Logged in!");
		}
	    catch (LoginException e)
		{
	    	logger.error("Failed to log in. Is the token invalid?");
	    	e.printStackTrace();
		} 
	    catch (IllegalArgumentException e)
		{
	    	logger.error("No log in credentials provided. Make sure to set your client token.");
	    	e.printStackTrace();
		}
	}
	public static void enableCommands()
	{
		HashMap<String, Boolean> commandConfigMap = GlobalSettingsManager.getGlobalCommandConfigMap();
		CommandRegister commandRegister = new CommandRegister();
		CommandConfiguration[] commandConfigs = new CommandConfiguration[commandRegister.getRegisterSize()];
		for (int i = 0; i<commandConfigs.length; i++)
		{
			commandConfigs[i] = new CommandConfiguration(commandConfigMap.get(commandRegister.getRegister()[i].getCommandKey()), commandRegister.getRegister()[i].getAliases(), commandRegister.getRegister()[i].getCommandClass());
		}
		shardManager.addEventListener(new CommandHandler(commandConfigs));
	}
}
