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
import jara.Core;
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
	/**
	 * Returns the base settings directory.
	 * @return
	 * File - The dir where Jara stores settings
	 */
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
	/**
	 * Returns the settings as they are stored in the file, or the current working settings if the file has already been read.
	 * @return
	 * GlobalSettingsJson - Settings as stored in file
	 */
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
	/**
	 * Saves any modifications to globalSettings back to file.
	 */
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

	/**
	 * This method will compare the configuration files for global settings as well as each individual guild against the Command register<br>
	 *     Any missing entries in the config files are then added with a default state.
	 */
	private static void addNewCommands()
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		if (getGlobalSettings().getCommandConfig().length < CommandRegister.getRegisterSize())  //If our config has fewer commands than the total we know of...
		{
			ArrayList<String> keys = new ArrayList<>();																		//This is used to keep track of which keys still need to be added
			Collections.addAll(keys, CommandRegister.getAllCommandKeys());
			GlobalCommandConfigJson[] updatedCommandConfig = new GlobalCommandConfigJson[CommandRegister.getRegisterSize()];
			for (int i = 0; i<getGlobalSettings().getCommandConfig().length; i++)
			{
				updatedCommandConfig[i] = getGlobalSettings().getCommandConfig()[i];											//Import existing settings and note their keys have been recorded
				keys.remove(updatedCommandConfig[i].getCommandKey());
			}
			for (int i = 0; i<keys.size(); i++)																							//For the keys that remain (i.e, those that were not in the existing config...)
			{
				updatedCommandConfig[(updatedCommandConfig.length-1)-i] = new JsonFormats().new GlobalCommandConfigJson(keys.get(i), false);	//Add them, as disabled by default. (Better safe than sorry)
				keys.remove(keys.get(i));
			}
			getGlobalSettings().setCommandConfig(updatedCommandConfig);
			saveGlobalSettings();

            //TODO: Headless check
            ConsoleGUI.manageNewCommands();

			for (File guildSettingsFile : getGuildSettingsDirectory().listFiles())
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
	//==============================================================================================================
	//==================================Getters & Setters===========================================================

	/**
	 * Returns the client token the bot is either currently using.<br>
	 * If the bot is not currently connected, it returns the token saved to config
	 * @return
	 * String - the token.
	 */
	public static String getClientToken()
	{
		if (Core.getShardManager() != null)
		{
			return Core.getShardManager().getApplicationInfo().getJDA().getToken();
		}
		else
		{
			return getGlobalSettings().getToken();
		}
	}

	/**
	 * Sets a new token for the bot.<br>
	 * It is highly recommended to restart the bot after running this, otherwise it will still use the old token.<br>
	 * <br>
	 * NOTE: This method will test the new token to ensure it is valid. If it is not valid it will launch a GUI prompt to correct it, holding the current thread.
	 *
	 * @param token - The new token
	 */
	public static void setClientToken(String token) //TODO: Encrypt this
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		String encryptedToken = token; //Encrypt here

		getGlobalSettings().setToken(token);
		saveGlobalSettings();
		logger.info("Bot token has now been set to "+token+".");
	}

	/**
	 * Checks if the command is enabled in the global settings
	 * @param commandKey - The command to check
	 * @return
	 * true - Command is enabled
	 * false - Command is disabled
	 */
	public static boolean isCommandEnabledGlobally(String commandKey)
	{
		for (GlobalCommandConfigJson commandSettings : getGlobalSettings().getCommandConfig())
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

	/**
	 * This method will attempt to set the command's status to newStatus.<br>
	 * Some commands cannot be disabled, however, as these would cause issues, such as /Help or /Config
	 * @param commandKey - The command to update
	 * @param newStatus - The new state for the command
	 */
	public static void setCommandEnabledGlobally(String commandKey, boolean newStatus)
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		boolean keyFound = false;
		boolean updatedState = !newStatus;
		for (GlobalCommandConfigJson commandSettings : getGlobalSettings().getCommandConfig())
		{
			if (commandSettings.getCommandKey().equalsIgnoreCase(commandKey))
			{
				updatedState = commandSettings.setEnabled(newStatus);
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
			if (updatedState == newStatus)
			{
				logger.info("Command "+commandKey+ "'s enabled state is now "+newStatus+".");
			}
			else
			{
				logger.info("Sorry, the status of "+commandKey+" cannot be changed.");
			}

		}
	}

	/**
	 * Sets all commands (where possible) in the category to the new status
	 * @param categoryName - The category name of commands to update
	 * @param newStatus - The new state
	 */
	public static void setCategoryEnabledGlobally(String categoryName, boolean newStatus)
	{
		setCategoryEnabledGlobally(CommandRegister.getCommandCategory(categoryName), newStatus);
	}
	/**
	 * Sets all commands (where possible) in the category to the new status
	 * @param categoryID - The category ID of commands to update
	 * @param newStatus - The new state
	 */
	public static void setCategoryEnabledGlobally(int categoryID, boolean newStatus)
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		if (CommandRegister.getCategoryName(categoryID) == null) //Simple verification check to ensure the id is valid.
		{
			logger.info("Could not alter category status as the specified category does not exist.");
			return;
		}
		for (CommandAttributes commandAttributes : CommandRegister.getCommandsInCategory(categoryID))
		{
			for (int i = 0; i<getGlobalSettings().getCommandConfig().length; i++)
			{
				if (getGlobalSettings().getCommandConfig()[i].getCommandKey().equalsIgnoreCase(commandAttributes.getCommandKey())) //TODO: Inefficient. Fix.
				{
					getGlobalSettings().getCommandConfig()[i].setEnabled(newStatus);
				}
			}
		}
	}

	/**
	 * Returns the command keys for all commands enabled globally
	 * @return
	 * String[] - Array of enabled command keys
	 */
	public static String[] getGloballyEnabledCommandKeys()
	{
		ArrayList<String> keys = new ArrayList<String>();
		for (GlobalCommandConfigJson config : getGlobalSettings().getCommandConfig())
		{
			if (config.isEnabled())
			{
				keys.add(config.getCommandKey());
			}
		}
		return keys.toArray(new String[0]);
	}
	/**
	 * Returns the command keys for all commands disabled globally
	 * @return
	 * String[] - Array of disabled command keys
	 */
	public static String[] getGloballyDisabledCommandKeys()
	{
		ArrayList<String> keys = new ArrayList<String>();
		for (GlobalCommandConfigJson config : getGlobalSettings().getCommandConfig())
		{
			if (!config.isEnabled())
			{
				keys.add(config.getCommandKey());
			}
		}
		return keys.toArray(new String[0]);
	}

	public static GlobalCommandConfigJson[] getGlobalCommandConfig()
	{
		return getGlobalSettings().getCommandConfig();
	}
	//============================================================================================================
}
