package jara;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import configuration.CommandConfiguration;
import configuration.GlobalSettingsManager;
import event.GuildJoinHandler;
import event.GuildLeaveHandler;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

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
		CommandConfiguration[] commandConfigs = new CommandConfiguration[CommandRegister.getRegisterSize()];
		for (int i = 0; i<commandConfigs.length; i++)
		{
			commandConfigs[i] = new CommandConfiguration(CommandRegister.getRegister()[i], commandConfigMap.get(CommandRegister.getRegister()[i].getCommandKey()));
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
		catch (IndexOutOfBoundsException e)	//If the bot has no role
		{
			return Color.decode("#5967cf"); //Use a default theme.
		}
		
	}
}
