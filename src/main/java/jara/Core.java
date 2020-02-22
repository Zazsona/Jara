package jara;

import java.util.Collections;
import java.util.HashSet;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import event.GuildJoinHandler;
import event.GuildLeaveHandler;

public class Core //A class for covering the global manners of the bot.
{
	private static Logger logger = LoggerFactory.getLogger(Core.class);
	private static ShardManager shardManager;

	private static final HashSet<String> supportedVersions = new HashSet<>();
	private static final String VERSION = "0.1";

	/**
	 * Gets the current Jara version
	 * @return the version
	 */
	public static String getVersion()
	{
		return VERSION;
	}

	/**
	 * Gets a set of all past Jara versions with which modules should be compatible with.
	 * If an update breaks the API, this set is to be wiped.
	 * @return set of back-compat versions
	 */
	public static HashSet<String> getSupportedVersions()
	{
		if (supportedVersions.size() == 0)
		{
			Collections.addAll(supportedVersions, "0.1", VERSION);
		}
		return supportedVersions;
	}


	/**
	 * Connect to the Discord network, and initiate the shards.
	 * @param clientToken the application token
	 * @return boolean on success
	 */
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

	/**
	 * Prepares the commands from all loaded modules to be executed
	 */
	public static void enableCommands()
	{
		shardManager.addEventListener(new CommandHandler());
	}

	/**
	 * Starts the handlers for Discord events.
	 */
	public static void startListeners()
	{
		shardManager.addEventListener(new GuildJoinHandler());
		shardManager.addEventListener(new GuildLeaveHandler());
	}

	/**
	 * Gets the {@link ShardManager}
	 * @return the shard manager
	 */
	@Nullable
	public static ShardManager getShardManager()
	{
		return shardManager;
	}

    /**
     * Gets the {@link ShardManager}. This will hold the calling thread until the shard manager has been built.
     * @return the shard manager, once built.
     */
	@NotNull
	public static ShardManager getShardManagerNotNull()
    {
        while (shardManager == null || shardManager.getStatus(0) != JDA.Status.CONNECTED)
        {
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
                logger.error(e.toString());
            }
        }
        return shardManager;
    }
}
