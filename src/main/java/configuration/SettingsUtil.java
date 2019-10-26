package configuration;

import commands.CmdUtil;
import gui.HeadedGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class with convenience methods and memory management for configuration data.
 */
public class SettingsUtil
{
    private static final Logger logger = LoggerFactory.getLogger(SettingsUtil.class);
    private static File directory;
    private static GlobalSettings globalSettings;

    private static HashMap<String, GuildSettings> guildSettingsMap = new HashMap<>();
    private static HashMap<String, Long> guildSettingsLastCall = new HashMap<>();
    private static HashMap<String, Character> guildCommandPrefix = new HashMap<>();

    /**
     * Loads the global settings.<br>
     *     If no global settings exist, the setup GUI will be launched.
     */
    public static void initialise()
    {
        try
        {
            globalSettings = new GlobalSettings();
            boolean success = globalSettings.restore();
            if (!success)
            {
                HeadedGUI.performFirstTimeSetup();
            }
            Timer guildCleanTimer = new Timer();
            guildCleanTimer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    cleanInactiveGuilds();
                }
            }, 1000*60*30, 1000*60*30);
        }
        catch (IOException e)
        {
            logger.error("Unable to access file system to establish settings.");
        }
    }

    /**
     * Returns the global settings.
     * @return the global settings
     */
    public static GlobalSettings getGlobalSettings()
    {
        return globalSettings;
    }

    /**
     * Returns the base settings directory.
     * @return the directory where settings are stored
     */
    public static File getDirectory()
    {
        if (directory == null)
        {
            Logger logger = LoggerFactory.getLogger(SettingsUtil.class);
            String operatingSystem = System.getProperty("os.name").toLowerCase();
            if (operatingSystem.startsWith("windows"))
            {
                directory = new File(System.getProperty("user.home")+"\\AppData\\Roaming\\Jara\\");
            }
            else
            {
                directory = new File("/usr/share/Jara/");
            }
            if (!directory.exists())
            {
                try
                {
                    directory.mkdirs();
                }
                catch (SecurityException e)
                {
                    logger.error(e.toString());
                }
            }
        }
        return directory;
    }

    /**
     * Gets the directory to save module configs, saves, etc in.
     * @return the directory for modules
     */
    public static File getModuleDataDirectory()
    {
        File dir = new File(getDirectory().getPath()+"/ModuleData/");
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * Gets the {@link GuildSettings} for the guild denoted by ID
     * @param guildID the guild to get settings for
     * @return the guild's settings
     */
    public static GuildSettings getGuildSettings(String guildID)
    {
        if (guildSettingsMap.containsKey(guildID))
        {
            guildSettingsLastCall.replace(guildID, Instant.now().toEpochMilli());
            return guildSettingsMap.get(guildID);
        }
        else
        {
            GuildSettings guildSettings;
            try
            {
                guildSettings = new GuildSettings(guildID);
                guildSettingsMap.put(guildID, guildSettings);
                guildSettingsLastCall.put(guildID, Instant.now().toEpochMilli());
                return guildSettings;
            }
            catch (IOException e)
            {
                logger.error(e.toString());
                return null; //TODO: Something proper.
            }
        }
    }

    /**
     * Gets the command prefix for the guild specified.<br>
     *     This method caches only the command prefix, reducing memory costs compared to getting the whole guild settings.
     * @param guildID the id of the guild
     * @return the command invocation character
     */
    public static Character getGuildCommandPrefix(String guildID)
    {
        if (guildCommandPrefix.containsKey(guildID))
        {
            return guildCommandPrefix.get(guildID);
        }
        else
        {
            Character commandPrefix = getGuildSettings(guildID).getCommandPrefix();
            guildCommandPrefix.put(guildID, commandPrefix);
            return commandPrefix;
        }
    }

    /**
     * Reloads the cached Guild command prefixes. Only really useful for if it has just been changed in settings.
     * @param guildID the guild prefix to refresh
     */
    public static void refreshGuildCommandPrefix(String guildID)
    {
        guildCommandPrefix.remove(guildID);
        getGuildCommandPrefix(guildID);
    }

    /**
     * This method will de-reference any guild settings for guilds which have not been used within the last hour.<br>
     * This is automatically performed every 30 minutes.
     */
    private static void cleanInactiveGuilds() //It's either this, or the mop & bucket. And there's some dirty shit in those guilds.
    {
        Thread.currentThread().setName("Guild Cleaner");
        Iterator guildIterator = guildSettingsLastCall.entrySet().iterator();
        int guildsRemoved = 0;
        while (guildIterator.hasNext())
        {
            HashMap.Entry<String, Long> guildCall = (HashMap.Entry<String, Long>) guildIterator.next();
            if ((Instant.now().toEpochMilli() - guildCall.getValue()) >= 1000*60*60)
            {
                guildsRemoved++;
                guildSettingsMap.remove(guildCall.getKey());
                guildSettingsLastCall.remove(guildCall.getKey());
                if (CmdUtil.getGuildAudio(guildCall.getKey()).getTrackQueue().size() == 0)
                {
                    CmdUtil.clearGuildAudio(guildCall.getKey());
                }
            }
        }
        logger.info("Cleared "+guildsRemoved+" guild settings from memory.");
    }
}
