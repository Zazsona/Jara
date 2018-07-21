package configuration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import jara.CommandRegister;

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
		if (directory == null)
		{
			Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
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
	
	//================================= Global Config Tools =====================================================
	public static File getGlobalSettingsFile()
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		File settingsFile = new File(getDirectory().getAbsolutePath()+"/settings.json");
		if (!settingsFile.exists())
		{
			logger.error("Settings file does not exist.");
			JOptionPane.showMessageDialog(null, "The global settings file has disappeared, and is required. Please restart the program to run first time setup again.");
			System.exit(0);
			return null;
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
				addNewCommands();
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
	
	public static File performFirstTimeSetup(String token) //TODO: Remove
	{
		//TODO: Call some GUIs 'n' shit.
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		File settingsFile = new File(getDirectory().getAbsolutePath()+"/settings.json");
		if (!settingsFile.exists())
		{
			String operatingSystem = System.getProperty("os.name");
			if (!operatingSystem.startsWith("Windows") || !operatingSystem.startsWith("Linux"))
			{
				logger.info("An unsupported operating system is being used. Be aware some issues may occur.");
			}
			try
			{
				settingsFile.createNewFile();
				FileWriter fileWriter = new FileWriter(settingsFile);
				PrintWriter printWriter = new PrintWriter(fileWriter);
				
				CommandRegister commandRegister = new CommandRegister();
				CommandConfigJson[] ccjs = new CommandConfigJson[commandRegister.getRegisterSize()];
				String[] keys = commandRegister.getAllCommandKeys();
				for (int i = 0; i<ccjs.length; i++)
				{
					ccjs[i] = new CommandConfigJson();
					ccjs[i].commandKey = keys[i];
					ccjs[i].enabled = true;
				}
				globalSettings = new GlobalSettingsManager.GlobalSettingsJson();
				globalSettings.token = token;
				globalSettings.commandConfig = ccjs.clone();

				Gson gson = new Gson();
				printWriter.print(gson.toJson(globalSettings));
				
				printWriter.close();
				fileWriter.close();
				return settingsFile;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		else
		{
			return null;
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
	
	public static void addNewCommands()
	{
		CommandRegister commandRegister = new CommandRegister();
		if (globalSettings.commandConfig.length < commandRegister.getRegisterSize()) 
		{
			CommandConfigJson[] updatedCommandConfig = new CommandConfigJson[commandRegister.getRegisterSize()]; 
			for (int i = 0; i<globalSettings.commandConfig.length; i++)		//For every known command setting...
			{
				updatedCommandConfig[i] = globalSettings.commandConfig[i];		//Put that in the new config
			}
			String keys[] = commandRegister.getAllCommandKeys();
			for (int j = globalSettings.commandConfig.length; j<commandRegister.getRegisterSize(); j++)	//For the missing entries (based on size discrepancy)...
			{
				updatedCommandConfig[j] = new CommandConfigJson();
				updatedCommandConfig[j].commandKey = keys[j];																		//Create new entries
				updatedCommandConfig[j].enabled = false; //We will show the GUI later, and false should make it clearer what is new.
			}
			globalSettings.commandConfig = updatedCommandConfig.clone();
			//TODO: SHOW THE GUI TO SELECT ENABLED COMMANDS
			saveGlobalSettings();		
		}
	}
	
	public static CommandConfigJson[] getGlobalCommandConfig()
	{
		ArrayList<CommandConfigJson> commandConfigList = new ArrayList<CommandConfigJson>();
		for (CommandConfigJson commandConfig : getGlobalSettings().commandConfig)
		{
			commandConfigList.add(commandConfig);
		}
		return commandConfigList.toArray(new CommandConfigJson[commandConfigList.size()]);
	}
	public static HashMap<String, Boolean> getGlobalCommandConfigMap()
	{
		HashMap<String, Boolean> commandMap = new HashMap<String, Boolean>();
		for (CommandConfigJson commandConfig : getGlobalSettings().commandConfig)
		{
			commandMap.put(commandConfig.commandKey, commandConfig.enabled);
		}
		return commandMap;
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
		return commandConfigList.toArray(new CommandConfigJson[commandConfigList.size()]);
	}
	public static String[] getGlobalEnabledCommandKeys()
	{
		ArrayList<String> keys = new ArrayList<String>();
		for (CommandConfigJson config : getGlobalEnabledCommands())
		{
			keys.add(config.commandKey);
		}
		return keys.toArray(new String[keys.size()]);
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
		return commandConfigList.toArray(new CommandConfigJson[commandConfigList.size()]);
	}
	public static String[] getGlobalDisabledCommandKeys()
	{
		ArrayList<String> keys = new ArrayList<String>();
		for (CommandConfigJson config : getGlobalDisabledCommands())
		{
			keys.add(config.commandKey);
		}
		return keys.toArray(new String[keys.size()]);
	}
	//============================================================================================================
	
	//===================================== JSON Classes =========================================================
	private static class GlobalSettingsJson
	{
		String token;
		CommandConfigJson[] commandConfig;
	}
	private static class CommandConfigJson
	{
		String commandKey;
		boolean enabled;
	}

	//============================================================================================================
}
