package jara;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class FileManager
{
	/**
	 * 
	 * When adding new methods to this class ****ALWAYS**** call getDirectory(), as this 
	 * always ensures the directory has been set, and allows for OS specific organisation.
	 * 
	 */
	private static File directory;
	public File getDirectory()
	{
		if (directory != null)
		{
			Logger logger = LoggerFactory.getLogger(FileManager.class);
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
	public File getSettingsFile()
	{
		Logger logger = LoggerFactory.getLogger(FileManager.class);
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
	public SettingsJson getCurrentSettings()
	{
		Logger logger = LoggerFactory.getLogger(FileManager.class);
		try
		{
			File settingsFile = getSettingsFile();
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
			SettingsJson settings = gson.fromJson(settingsFileDataBuilder.toString(), SettingsJson.class);
			return settings;
		} 
		catch (IOException e)
		{
			logger.error("Unable to read settings file.");
			e.printStackTrace();
			return new SettingsJson();
		}

	}
	public void setSettings(SettingsJson newSettings)
	{
		Logger logger = LoggerFactory.getLogger(FileManager.class);
		try
		{
			File settingsFile = getSettingsFile();
			FileWriter fileWriter = new FileWriter(settingsFile, false);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			Gson gson = new Gson();
			printWriter.print(gson.toJson(newSettings));
			printWriter.close();
			fileWriter.close();
		} 
		catch (IOException e)
		{
			logger.error("Unable to write to settings file.");
			e.printStackTrace();
		}

	}
	private class SettingsJson
	{
		String token;
	}
	public void setNewClientToken(String token) //TODO: Encrypt this
	{
		token = token; //Encrypt here
		
		SettingsJson settings = getCurrentSettings();
		settings.token = token;
		setSettings(settings);

		
	}
}
