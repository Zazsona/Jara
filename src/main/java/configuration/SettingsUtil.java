package configuration;

import gui.headed.HeadedGUIManager;
import jara.CommandAttributes;
import jara.CommandRegister;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SettingsUtil
{
    private static Logger logger = LoggerFactory.getLogger(SettingsUtil.class);
    private static File directory;
    private static GlobalSettings globalSettings;

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
                Application.launch(HeadedGUIManager.class);
            }
            if (!globalSettings.isSaved())
            {
                logger.info("User has cancelled setup. Aborting...");
                System.exit(0);
            }
        }
    }
    public void manageNewCommands()
    {
        HashMap<String, Boolean> commandConfig = globalSettings.getCommandConfig();
        if (CommandRegister.getRegisterSize() > commandConfig.size()) //Quick test, if the register is larger then we know config is missing some elements without having to check each key.
        {
            //TODO: Open update GUI
        }
        else //Otherwise...
        {
            for (CommandAttributes ca : CommandRegister.getRegister()) //Check each key
            {
                if (!commandConfig.containsKey(ca.getCommandKey())) //If the key isn't in the config, open updater.
                {
                    //TODO: Open update GUI
                }
            }
        }
        //We don't care if the config has commands not in the register, these will simply be ignored. This allows for backwards compatibility and (in some cases) transfers of settings from forked/modded versions of Jara.

    }
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
    private static File getGuildSettingsDirectory()
    {
        File guildSettingsFolder;
        guildSettingsFolder = new File(getDirectory().getAbsolutePath()+"/guilds/");
        if (!guildSettingsFolder.exists())
        {
            guildSettingsFolder.mkdirs();
        }
        return guildSettingsFolder;
    }
    //================================= Global Config Tools =====================================================

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
            logger.info("Settings file does not exist.");
            return null;
        }
        else
        {
            return settingsFile;
        }
    }
}
