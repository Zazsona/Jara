package jara;

import java.util.HashMap;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commands.About;
import commands.CoinFlip;
import commands.EightBall;
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
	public static void enableCommands(HashMap<String, Boolean> enabledCommandList)
	{
		/*
		 * When adding command aliases, ensure that the first alias matches the command's command key.
		 */
		CommandConfiguration[] commandRegister = new CommandConfiguration[6];
		commandRegister[0] = new CommandConfiguration(enabledCommandList.get("Ping"), new String[] {"Ping", "Pong"}, Ping.class);
		commandRegister[1] = new CommandConfiguration(enabledCommandList.get("Report"), new String[] {"Report", "Info", "Status"}, Report.class);
		commandRegister[2] = new CommandConfiguration(enabledCommandList.get("About"), new String[] {"About", "Author", "Source"}, About.class);
		commandRegister[3] = new CommandConfiguration(enabledCommandList.get("CoinFlip"), new String[] {"CoinFlip", "FlipCoin", "Coin", "TossCoin", "CoinToss", "cf", "fc"}, CoinFlip.class);
		commandRegister[4] = new CommandConfiguration(enabledCommandList.get("EightBall"), new String[] {"EightBall", "8Ball", "HelixFossil"}, EightBall.class);
		commandRegister[4] = new CommandConfiguration(enabledCommandList.get("Joke"), new String[] {"Jokes", "Joke", "TellMeAJoke", "Comedy"}, EightBall.class);
		shardManager.addEventListener(new CommandHandler(commandRegister));
	}
}
