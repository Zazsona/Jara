package configuration;

import gui.HeadedGUI;
import gui.headed.HeadedGUIUtil;
import jara.CommandAttributes;
import jara.CommandRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class SettingsUtil
{
    private static final Logger logger = LoggerFactory.getLogger(SettingsUtil.class);
    private static File directory;
    private static GlobalSettings globalSettings;

    private static HashMap<String, GuildSettings> guildSettingsMap = new HashMap<>();
    private static HashMap<String, Long> guildSettingsLastCall = new HashMap<>();

    public static void initialise()
    {
        try
        {
            globalSettings = new GlobalSettings();
            globalSettings.restore();
        }
        catch (IOException | NullPointerException e) //This fires if settings do not exist.
        {
            if (GraphicsEnvironment.isHeadless())
            {
                //TODO: Terminal ver.
            }
            else
            {
                HeadedGUI.performFirstTimeSetup();
            }
            if (!HeadedGUIUtil.isSetupComplete())
            {
                logger.info("User has cancelled setup. Aborting...");
                System.exit(0);
            }
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

    /**
     * Returns the global settings.
     * @return
     */
    public static GlobalSettings getGlobalSettings()
    {
        return globalSettings;
    }
    /**
     * Returns the base settings directory.
     * @return
     * File - The dir where Jara stores settings
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
                directory = new File(System.getProperty("user.home")+"/.Jara/");
            }
            if (!directory.exists())
            {
                try
                {
                    directory.mkdirs();
                }
                catch (SecurityException e)
                {
                    logger.error("Jara has run into a file security error. Does it have permissions to read/write files & directories?");
                    e.printStackTrace();
                }
            }
        }
        return directory;
    }
    /**
     * Returns the directory which stores guild settings files.
     * @return
     * File - Guild Settings directory
     */
    public static File getGuildSettingsDirectory()
    {
        File guildSettingsFolder;
        guildSettingsFolder = new File(getDirectory().getAbsolutePath()+"/guilds/");
        if (!guildSettingsFolder.exists())
        {
            guildSettingsFolder.mkdirs();
        }
        return guildSettingsFolder;
    }
    /**
     * Returns the file where global settings are stored.
     * @return
     * File - Global settings file
     */
    public static File getGlobalSettingsFile()
    {
        File settingsFile = new File(getDirectory().getAbsolutePath()+"/settings.json");
        if (!settingsFile.exists())
        {
            logger.info("Settings file does not exist. Creating it...");
            try
            {
                settingsFile.createNewFile();
                return settingsFile;
            }
            catch (IOException e)
            {
                logger.error("Could not create settings file.");
                return null;
            }
        }
        else
        {
            return settingsFile;
        }
    }

    /**
     * @param guildID
     * @return
     */
    public static File getGuildSettingsFile(String guildID)
    {
        return new File(getGuildSettingsDirectory().getPath()+"/"+guildID+".json");
    }

    public static void addNewGuild(String guildId)
    {
        try
        {
            File guildFile = getGuildSettingsFile(guildId);
            if (guildFile.exists())
            {
                guildFile.delete();
            }
            guildFile.createNewFile();
            GuildSettings guildSettings = getGuildSettings(guildId);
            guildSettings.setDefaultSettings();
            guildSettings.save();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public static GuildSettings getGuildSettings(String guildID)
    {
        if (guildSettingsMap.containsKey(guildID))
        {
            guildSettingsLastCall.replace(guildID, Instant.now().toEpochMilli());
            return guildSettingsMap.get(guildID);
        }
        else
        {
            GuildSettings guildSettings = new GuildSettings(guildID);
            try
            {
                guildSettings.restore();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (NullPointerException e) //In theory this will never occur, but better safe than sorry.
            {
                guildSettings.setDefaultSettings();
            }
            guildSettingsMap.put(guildID, guildSettings);
            guildSettingsLastCall.put(guildID, Instant.now().toEpochMilli());
            return guildSettings;
        }
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
            if ((Instant.now().toEpochMilli() - guildCall.getValue()) >= 1000*60)
            {
                guildsRemoved++;
                guildSettingsMap.remove(guildCall.getKey());
                guildSettingsLastCall.remove(guildCall.getKey());
            }
        }
        logger.info("Cleared "+guildsRemoved+" guild settings from memory.");
    }
}
