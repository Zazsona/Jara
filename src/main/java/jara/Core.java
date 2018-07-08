package jara;

import java.util.HashMap;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commands.About;
import commands.CoinFlip;
import commands.EightBall;
import commands.Jokes;
import commands.Ping;
import commands.Report;
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
		/*
		 * When adding command aliases, ensure that the first alias matches the command's command key.
		 */
		HashMap<String, Boolean> commandConfigMap = GlobalSettingsManager.getGlobalCommandConfigMap();
		CommandConfiguration[] commandConfigs = new CommandConfiguration[commandConfigMap.size()];
		commandConfigs[0] = new CommandConfiguration(commandConfigMap.get("Ping"), new String[] {"Ping", "Pong"}, Ping.class);
		commandConfigs[1] = new CommandConfiguration(commandConfigMap.get("Report"), new String[] {"Report", "Info", "Status"}, Report.class);
		commandConfigs[2] = new CommandConfiguration(commandConfigMap.get("About"), new String[] {"About", "Author", "Source"}, About.class);
		commandConfigs[3] = new CommandConfiguration(commandConfigMap.get("CoinFlip"), new String[] {"CoinFlip", "FlipCoin", "Coin", "TossCoin", "CoinToss", "cf", "fc"}, CoinFlip.class);
		commandConfigs[4] = new CommandConfiguration(commandConfigMap.get("EightBall"), new String[] {"EightBall", "8Ball", "HelixFossil"}, EightBall.class);
		commandConfigs[4] = new CommandConfiguration(commandConfigMap.get("Jokes"), new String[] {"Jokes", "Joke", "TellMeAJoke", "Comedy"}, Jokes.class);
		shardManager.addEventListener(new CommandHandler(commandConfigs));
	}
}
