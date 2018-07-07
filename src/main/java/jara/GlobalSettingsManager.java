package jara;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * 
 * When adding new methods to this class ****ALWAYS**** call getDirectory(), or getXFile() as this 
 * always ensures the directory has been set, and allows for OS specific organisation.
 * 
 * Also be sure to save any modifications via updateStoredSettings().
 * 
 */

public class GlobalSettingsManager
{
	private static File directory;
	private static GlobalSettingsJson globalSettings;
	
	public static File getDirectory()
	{
		if (directory != null)
		{
			Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
			String operatingSystem = System.getProperty("os.name").toLowerCase();
			if (operatingSystem.startsWith("windows"))
			{
				directory = new File(System.getProperty("user.home")+"\\AppData\\Roaming\\Jara\\");
			}
			else if (operatingSystem.startsWith("linux"))
			{
				directory = new File(System.getProperty("user.home")+"/.Jara/");
			}
			else
			{

				logger.info("An unsupported operating system is being used. Be aware some issues may occur."); //TODO: Move this to somewhere more logical?
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
	
	//================================= Global Config Tools =====================================================
	public static File getGlobalSettingsFile()
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		File settingsFile = new File(getDirectory().getAbsolutePath()+"settings.json");
		if (!settingsFile.exists())
		{
			logger.info("Settings file does not exist. Creating now.");
			try
			{
				settingsFile.createNewFile();
				//TODO: Call first time setup
				return settingsFile;
			} 
			catch (IOException e)
			{
				logger.error("An error occured when attempting to create the file.");
				e.printStackTrace();
				return null;
			}
		}
		else
		{
			return settingsFile;
		}
	}
	public static GlobalSettingsJson getGlobalSettings()
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		if (globalSettings != null)
		{
			return globalSettings;
		}
		else
		{
			try
			{
				File settingsFile = getGlobalSettingsFile();
				FileReader fileReader = new FileReader(settingsFile);
				Scanner scanner = new Scanner(fileReader);
				StringBuilder settingsFileDataBuilder = new StringBuilder();
				while (scanner.hasNext())
				{
					settingsFileDataBuilder.append(scanner.nextLine());
				}
				scanner.close();
				fileReader.close();
				Gson gson = new Gson();
				globalSettings = gson.fromJson(settingsFileDataBuilder.toString(), GlobalSettingsJson.class);
				return globalSettings;
			} 
			catch (IOException e)
			{
				logger.error("Unable to read settings file.");
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Jara was unable to access the settings file. Please ensure the program has sufficient file access permissions.");
				System.exit(0);
				return null;
			}
		}
	}
	public static void saveGlobalSettings()
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		try
		{
			File settingsFile = getGlobalSettingsFile();
			FileWriter fileWriter = new FileWriter(settingsFile, false);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			Gson gson = new Gson();
			printWriter.print(gson.toJson(globalSettings));
			printWriter.close();
			fileWriter.close();
		} 
		catch (IOException e)
		{
			logger.error("Unable to write to settings file.");
			e.printStackTrace();
		}

	}
	
	public static String getGlobalClientToken()
	{
		getGlobalSettings();
		return globalSettings.token;
	}
	public static void setNewGlobalClientToken(String token) //TODO: Encrypt this
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		String encryptedToken = token; //Encrypt here
		
		getGlobalSettings();
		globalSettings.token = token;
		saveGlobalSettings();
		logger.info("Bot token has now been set to "+token+". This will be reflected after a restart.");
	}
	
	public static boolean getGlobalCommandEnabledStatus(int commandNo)
	{
		getGlobalSettings();
		return globalSettings.commandConfig[commandNo].enabled;
	}
	public static void setGlobalCommandEnabledStatus(int commandNo, boolean newStatus)
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		getGlobalSettings();
		globalSettings.commandConfig[commandNo].enabled = newStatus;
		saveGlobalSettings();
		logger.info("Command "+globalSettings.commandConfig[commandNo].commandKey + "'s enabled status has been changed to "+newStatus+". This will be reflected after a restart.");
	}
	public static boolean getGlobalCommandEnabledStatus(String commandKey)
	{
		getGlobalSettings();
		for (CommandConfigJson commandSettings : globalSettings.commandConfig)
		{
			if (commandSettings.commandKey.equalsIgnoreCase(commandKey))
			{
				return commandSettings.enabled;
			}

		}
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		logger.debug("Tried to find key \""+ commandKey+"\", however, it does not appear to exist.");
		return false; //Can't enable your command if it doesn't exist. *taps forehead*

	}
	public static void setGlobalCommandEnabledStatus(String commandKey, boolean newStatus)
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		getGlobalSettings();
		boolean keyFound = false;
		for (CommandConfigJson commandSettings : globalSettings.commandConfig)
		{
			if (commandSettings.commandKey.equalsIgnoreCase(commandKey))
			{
				commandSettings.enabled = newStatus;
				keyFound = true;
			}

		}
		if (keyFound == false)
		{
			logger.debug("Tried to find key \""+ commandKey+"\", however, it does not appear to exist.");
		}
		else
		{
			saveGlobalSettings();
			logger.info("Command "+commandKey+ "'s enabled status has been changed to "+newStatus+". This will be reflected after a restart.");
		}

	}
	
	public static CommandConfigJson[] getGlobalCommandConfig()
	{
		ArrayList<CommandConfigJson> commandConfigList = new ArrayList<CommandConfigJson>();
		for (CommandConfigJson commandConfig : getGlobalSettings().commandConfig)
		{
			commandConfigList.add(commandConfig);
		}
		return (CommandConfigJson[]) commandConfigList.toArray();
	}
	public static CommandConfigJson[] getGlobalEnabledCommands()
	{
		ArrayList<CommandConfigJson> commandConfigList = new ArrayList<CommandConfigJson>();
		for (CommandConfigJson commandConfig : getGlobalSettings().commandConfig)
		{
			if (commandConfig.enabled == true)
			{
				commandConfigList.add(commandConfig);
			}
		}
		return (CommandConfigJson[]) commandConfigList.toArray();
	}
	public static CommandConfigJson[] getGlobalDisabledCommands()
	{
		ArrayList<CommandConfigJson> commandConfigList = new ArrayList<CommandConfigJson>();
		for (CommandConfigJson commandConfig : getGlobalSettings().commandConfig)
		{
			if (commandConfig.enabled == false)
			{
				commandConfigList.add(commandConfig);
			}
		}
		return (CommandConfigJson[]) commandConfigList.toArray();
	}
	//============================================================================================================
	
	//===================================== JSON Classes =========================================================
	private class GlobalSettingsJson
	{
		String token;
		CommandConfigJson[] commandConfig;
	}
	private class CommandConfigJson
	{
		String commandKey;
		boolean enabled;
	}

	//============================================================================================================
}
