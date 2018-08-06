package configuration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import javax.swing.JOptionPane;

import gui.ConsoleGUI;
import jara.CommandAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import configuration.JsonFormats.GlobalCommandConfigJson;
import configuration.JsonFormats.GlobalSettingsJson;
import jara.CommandRegister;

/**
 * 
 * When adding new methods to this class ****ALWAYS**** call getDirectory(), or getXFile() as this 
 * always ensures the directory has been set, and allows for OS specific organisation.
 * 
 * Also be sure to save any modifications via saveGlobalSettings().
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
	private static File getGuildSettingsFolder()
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
	private static File getGlobalSettingsFile()
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
	private static GlobalSettingsJson getGlobalSettings()
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
	
	public static File performFirstTimeSetup()
	{
		//TODO: Call some graphical GUIs 'n' shit.
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		File settingsFile = new File(getDirectory().getAbsolutePath()+"/settings.json");
		if (!settingsFile.exists())
		{
			String operatingSystem = System.getProperty("os.name");
			if (!(operatingSystem.startsWith("Windows") || operatingSystem.startsWith("Linux")))
			{
				logger.info("An unsupported operating system is being used. Be aware some issues may occur.");
			}
			try
			{
				settingsFile.createNewFile();
				FileWriter fileWriter = new FileWriter(settingsFile);
				PrintWriter printWriter = new PrintWriter(fileWriter);
				
				GlobalCommandConfigJson[] ccjs = new GlobalCommandConfigJson[CommandRegister.getRegisterSize()];
				String[] keys = CommandRegister.getAllCommandKeys();
				for (int i = 0; i<ccjs.length; i++)
				{
					ccjs[i] = new JsonFormats().new GlobalCommandConfigJson(keys[i], true);
				}
				globalSettings = new JsonFormats().new GlobalSettingsJson();
				globalSettings.setCommandConfig(ccjs);

				Gson gson = new Gson();
				printWriter.print(gson.toJson(globalSettings));

				printWriter.close();
				fileWriter.close();
				//TODO: Add Headless check
				ConsoleGUI.firstTimeSetupWizard();

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
		return globalSettings.getToken();
	}
	public static void setNewGlobalClientToken(String token) //TODO: Encrypt this
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		String encryptedToken = token; //Encrypt here
		
		getGlobalSettings();
		globalSettings.setToken(token);
		saveGlobalSettings();
		logger.info("Bot token has now been set to "+token+".");
	}

	public static boolean getGlobalCommandEnabledStatus(String commandKey)
	{
		getGlobalSettings();
		for (GlobalCommandConfigJson commandSettings : globalSettings.getCommandConfig())
		{
			if (commandSettings.getCommandKey().equalsIgnoreCase(commandKey))
			{
				return commandSettings.isEnabled();
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
		for (GlobalCommandConfigJson commandSettings : globalSettings.getCommandConfig())
		{
			if (commandSettings.getCommandKey().equalsIgnoreCase(commandKey))
			{
				commandSettings.setEnabled(newStatus);
				keyFound = true;
			}

		}
		if (!keyFound)
		{
			logger.debug("Tried to find key \""+ commandKey+"\", however, it does not appear to exist.");
		}
		else
		{
			saveGlobalSettings();
			logger.info("Command "+commandKey+ "'s enabled status has been changed to "+newStatus+"."); //TODO: Nope
		}
	}
	public static void setGlobalCategoryEnabledStatus(String categoryName, boolean newStatus)
	{
		setGlobalCategoryEnabledStatus(CommandRegister.getCommandCategory(categoryName), newStatus);
	}
	public static void setGlobalCategoryEnabledStatus(int categoryID, boolean newStatus)
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		if (CommandRegister.getCategoryName(categoryID) == null) //Simple verification check to ensure the id is valid.
		{
			logger.info("Could not alter category status as the specified category does not exist.");
			return;
		}
		for (CommandAttributes commandAttributes : CommandRegister.getCommandsInCategory(categoryID))
		{
			for (int i = 0; i<globalSettings.getCommandConfig().length; i++)
			{
				if (globalSettings.getCommandConfig()[i].getCommandKey().equalsIgnoreCase(commandAttributes.getCommandKey())) //TODO: Inefficient. Fix.
				{
					globalSettings.getCommandConfig()[i].setEnabled(newStatus);
				}
			}
		}
	}
	private static void addNewCommands()
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		if (globalSettings.getCommandConfig().length < CommandRegister.getRegisterSize())  //If our config has fewer commands than the total we know of...
		{
			ArrayList<String> keys = new ArrayList<>();																		//This is used to keep track of which keys still need to be added
			Collections.addAll(keys, CommandRegister.getAllCommandKeys());
			GlobalCommandConfigJson[] updatedCommandConfig = new GlobalCommandConfigJson[CommandRegister.getRegisterSize()];
			for (int i = 0; i<globalSettings.getCommandConfig().length; i++)
			{
				updatedCommandConfig[i] = globalSettings.getCommandConfig()[i];											//Import existing settings and note their keys have been recorded
				keys.remove(updatedCommandConfig[i].getCommandKey());
			}
			for (int i = 0; i<keys.size(); i++)																							//For the keys that remain (i.e, those that were not in the existing config...)
			{
				updatedCommandConfig[(updatedCommandConfig.length-1)-i] = new JsonFormats().new GlobalCommandConfigJson(keys.get(i), false);	//Add them, as disabled by default. (Better safe than sorry)
				keys.remove(keys.get(i));
			}
			globalSettings.setCommandConfig(updatedCommandConfig);
			//TODO: SHOW THE GUI TO SELECT ENABLED COMMANDS
			saveGlobalSettings();		
			for (File guildSettingsFile : getGuildSettingsFolder().listFiles())
			{
				try
				{
					FileReader fileReader = new FileReader(guildSettingsFile);
					Scanner scanner = new Scanner(fileReader);
					StringBuilder guildSettingsDataBuilder = new StringBuilder();
					while (scanner.hasNext())
					{
						guildSettingsDataBuilder.append(scanner.nextLine());
					}
					scanner.close();
					fileReader.close();

					Gson gson = new Gson();
					JsonFormats.GuildSettingsJson guildSettings = gson.fromJson(guildSettingsDataBuilder.toString(), JsonFormats.GuildSettingsJson.class);
					if (guildSettings.getCommandConfig().length < CommandRegister.getRegisterSize())
					{
						keys = new ArrayList<>();																		//This is used to keep track of which keys still need to be added
						Collections.addAll(keys, CommandRegister.getAllCommandKeys());
						JsonFormats.GuildCommandConfigJson[] updatedGuildCommandConfig = new JsonFormats.GuildCommandConfigJson[CommandRegister.getRegisterSize()];
						for (int i = 0; i<guildSettings.getCommandConfig().length; i++)
						{
							updatedGuildCommandConfig[i] = guildSettings.getCommandConfig()[i];											//Import existing settings and note their keys have been recorded
							keys.remove(updatedGuildCommandConfig[i].getCommandKey());
						}
						for (int i = 0; i<keys.size(); i++)																							//For the keys that remain (i.e, those that were not in the existing config...)
						{
							updatedGuildCommandConfig[(updatedGuildCommandConfig.length-1)-i] = new JsonFormats().new GuildCommandConfigJson(keys.get(i), false, new ArrayList<>());	//Add them, as disabled by default. (Better safe than sorry)
							keys.remove(keys.get(i));
						}

						guildSettings.setCommandConfig(updatedGuildCommandConfig);
						FileWriter fileWriter = new FileWriter(guildSettingsFile, false);
						PrintWriter printWriter = new PrintWriter(fileWriter);
						printWriter.print(gson.toJson(guildSettings));
						printWriter.close();
						fileWriter.close();
					}
				}
				catch (IOException e)
				{
					logger.error("Unable to read/write guild "+guildSettingsFile.getName().replace(".json", "")+"'s settings file.");
					e.printStackTrace();
				}
			}
		}
	}
	
	public static GlobalCommandConfigJson[] getGlobalCommandConfig()
	{
		return globalSettings.getCommandConfig();
	}
	public static GlobalCommandConfigJson[] getGlobalEnabledCommands()
	{
		ArrayList<GlobalCommandConfigJson> commandConfigList = new ArrayList<GlobalCommandConfigJson>();
		for (GlobalCommandConfigJson commandConfig : getGlobalSettings().getCommandConfig())
		{
			if (commandConfig.isEnabled())
			{
				commandConfigList.add(commandConfig);
			}
		}
		return commandConfigList.toArray(new GlobalCommandConfigJson[0]);
	}
	public static String[] getGlobalEnabledCommandKeys()
	{
		ArrayList<String> keys = new ArrayList<String>();
		for (GlobalCommandConfigJson config : getGlobalEnabledCommands())
		{
			keys.add(config.getCommandKey());
		}
		return keys.toArray(new String[0]);
	}
	public static GlobalCommandConfigJson[] getGlobalDisabledCommands()
	{
		ArrayList<GlobalCommandConfigJson> commandConfigList = new ArrayList<GlobalCommandConfigJson>();
		for (GlobalCommandConfigJson commandConfig : getGlobalSettings().getCommandConfig())
		{
			if (!commandConfig.isEnabled())
			{
				commandConfigList.add(commandConfig);
			}
		}
		return commandConfigList.toArray(new GlobalCommandConfigJson[0]);
	}
	public static String[] getGlobalDisabledCommandKeys()
	{
		ArrayList<String> keys = new ArrayList<String>();
		for (GlobalCommandConfigJson config : getGlobalDisabledCommands())
		{
			keys.add(config.getCommandKey());
		}
		return keys.toArray(new String[0]);
	}
	//============================================================================================================
}
