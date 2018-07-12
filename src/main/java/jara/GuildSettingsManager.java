package jara;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;



public class GuildSettingsManager
{
	private File directory;
	private GuildSettingsJson guildSettings;
	private String guildID;
	
	public GuildSettingsManager(String guildID)
	{
		this.guildID = guildID;
		getGuildSettings();
	}
	
	public File getDirectory()
	{
		directory = GlobalSettingsManager.getDirectory();
		return directory;
	}
	
	//==================================== Guild Specific Tools ==================================================
	public File getGuildSettingsFolder()
	{
		File guildSettingsFolder;
		if (System.getProperty("os.name").startsWith("Windows"))
		{
			guildSettingsFolder = new File(getDirectory().getAbsolutePath()+"\\guilds\\");
		}
		else
		{
			guildSettingsFolder = new File(getDirectory().getAbsolutePath()+"/guilds/");
		}
		if (!guildSettingsFolder.exists())
		{
			guildSettingsFolder.mkdirs();
		}
		return guildSettingsFolder;
	}
	public File getGuildSettingsFile()
	{
		Logger logger = LoggerFactory.getLogger(GuildSettingsManager.class);
		File guildSettingsFile = new File(getGuildSettingsFolder().getAbsolutePath()+"/"+guildID+".json");
		if (!guildSettingsFile.exists())
		{
			try
			{
				guildSettingsFile.createNewFile();
				//TODO: Set file data
			} 
			catch (IOException e)
			{
				logger.error("Unable to create guild settings file.");
				e.printStackTrace();
				return null;
			}
		}
		return guildSettingsFile;
	}
	public GuildSettingsJson getGuildSettings()
	{
		if (guildSettings != null)
		{
			return guildSettings;
		}
		try
		{
			File guildSettingsFile = getGuildSettingsFile();
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
			guildSettings = gson.fromJson(guildSettingsDataBuilder.toString(), GuildSettingsJson.class);
			return guildSettings;
		} 
		catch (IOException e)
		{
			Logger logger = LoggerFactory.getLogger(GuildSettingsManager.class);
			logger.error("Unable to read guild settings file for guild id "+guildID);
			e.printStackTrace();
			return null;
		}

	}
	public void saveGuildSettings()
	{
		Logger logger = LoggerFactory.getLogger(GuildSettingsManager.class);
		try
		{
			File guildSettingsFile = getGuildSettingsFile();
			FileWriter fileWriter = new FileWriter(guildSettingsFile, false);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			Gson gson = new Gson();
			printWriter.print(gson.toJson(guildSettings));
			printWriter.close();
			fileWriter.close();
		} 
		catch (IOException e)
		{
			logger.error("Unable to write to guild "+guildID+"'s settings file.");
			e.printStackTrace();
		}
	}
	
	public String getGuildGameCategoryID()
	{
		return getGuildSettings().gameCategoryID;
	}
	public void setGuildGameCategoryID(String gameCategoryID)
	{
		GuildSettingsJson guildSettings = getGuildSettings();
		guildSettings.gameCategoryID = gameCategoryID;
		saveGuildSettings();
	}

	public boolean getGuildCommandEnabledStatus(int commandNo)
	{
		return getGuildSettings().commandConfig[commandNo].enabled;
	}
	public void setGuildCommandEnabledStatus(int commandNo, boolean newStatus)
	{
		Logger logger = LoggerFactory.getLogger(GuildSettingsManager.class);

		GuildSettingsJson guildSettings = getGuildSettings();
		guildSettings.commandConfig[commandNo].enabled = newStatus;
		saveGuildSettings();
		logger.info("Command "+guildSettings.commandConfig[commandNo].commandKey + "'s enabled status has been changed to "+newStatus+" for guild "+guildID);
	}
	public boolean getGuildCommandEnabledStatus(String commandKey)
	{
		for (CommandConfigJson commandSettings : getGuildSettings().commandConfig)
		{
			if (commandSettings.commandKey.equalsIgnoreCase(commandKey))
			{
				return commandSettings.enabled;
			}

		}
		Logger logger = LoggerFactory.getLogger(GuildSettingsManager.class);
		logger.debug("Tried to find key \""+ commandKey+"\", however, it does not appear to exist.");
		return false; //Can't enable your command if it doesn't exist. *taps forehead*

	}
	public void setGuildCommandEnabledStatus(String commandKey, boolean newStatus)
	{
		Logger logger = LoggerFactory.getLogger(GuildSettingsManager.class);
		boolean keyFound = false;
		GuildSettingsJson guildSettings = getGuildSettings();
		for (CommandConfigJson commandSettings : guildSettings.commandConfig)
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
			saveGuildSettings();
			logger.info("Command "+commandKey+ "'s enabled status has been changed to "+newStatus+" for guild "+guildID);
		}

	}
	
	public CommandConfigJson[] getGuildCommandConfig()
	{
		ArrayList<CommandConfigJson> commandConfigList = new ArrayList<CommandConfigJson>();
		for (CommandConfigJson commandConfig : getGuildSettings().commandConfig)
		{
			commandConfigList.add(commandConfig);
		}
		return commandConfigList.toArray(new CommandConfigJson[commandConfigList.size()]);
	}
	public CommandConfigJson[] getGuildEnabledCommands()
	{
		ArrayList<CommandConfigJson> commandConfigList = new ArrayList<CommandConfigJson>();
		for (CommandConfigJson commandConfig : getGuildSettings().commandConfig)
		{
			if (commandConfig.enabled == true)
			{
				commandConfigList.add(commandConfig);
			}
		}
		return commandConfigList.toArray(new CommandConfigJson[commandConfigList.size()]);
	}
	public CommandConfigJson[] getGuildDisabledCommands()
	{
		ArrayList<CommandConfigJson> commandConfigList = new ArrayList<CommandConfigJson>();
		for (CommandConfigJson commandConfig : getGuildSettings().commandConfig)
		{
			if (commandConfig.enabled == false)
			{
				commandConfigList.add(commandConfig);
			}
		}
		return commandConfigList.toArray(new CommandConfigJson[commandConfigList.size()]);
	}

	//============================================================================================================
	
	//===================================== JSON Classes =========================================================
	private class CommandConfigJson
	{
		String commandKey;
		boolean enabled;
	}
	private class GuildSettingsJson
	{
		String gameCategoryID;
		CommandConfigJson[] commandConfig;
	}
	//============================================================================================================
}
