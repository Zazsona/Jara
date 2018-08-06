package configuration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import net.dv8tion.jda.core.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import commands.Command;
import configuration.JsonFormats.GuildCommandConfigJson;
import configuration.JsonFormats.GuildSettingsJson;
import jara.CommandRegister;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;


public class GuildSettingsManager
{
	private GuildSettingsJson guildSettings;
	private final String guildID;
	
	public GuildSettingsManager(String guildID)
	{
		this.guildID = guildID;
		getGuildSettings();
	}
	
	public File getDirectory()
	{
		return GlobalSettingsManager.getDirectory();
	}
	
	//==================================== Guild Specific Tools ==================================================
	private File getGuildSettingsFolder()
	{
		File guildSettingsFolder;
		guildSettingsFolder = new File(getDirectory().getAbsolutePath()+"/guilds/");
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
			logger.error("Guild "+guildID+"'s settings have gone missing, or never existed. Creating new config.");
			performNewGuildSetup();
		}
		return guildSettingsFile;
	}
	public void deleteGuildSettingsFile()
	{
		getGuildSettingsFile().delete();
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
	public File performNewGuildSetup()
	{
		File guildSettingsFile = new File(getGuildSettingsFolder().getAbsolutePath()+"/"+guildID+".json");
		if (!guildSettingsFile.exists())
		{
			try
			{
				guildSettingsFile.createNewFile();
				FileWriter fileWriter = new FileWriter(guildSettingsFile);
				PrintWriter printWriter = new PrintWriter(fileWriter);
				
				GuildCommandConfigJson[] ccjs = new GuildCommandConfigJson[CommandRegister.getRegisterSize()];
				String[] keys = CommandRegister.getAllCommandKeys();
				for (int i = 0; i<ccjs.length; i++)
				{
					ArrayList<String> roleIDs = new ArrayList<>();
					if (CommandRegister.getCommandCategory(keys[i]) != CommandRegister.ADMIN)
					{
						roleIDs.add(guildID); //Weird, huh? Discord's @everyone role id matches the guild's id. So this enables the command for all.
					}
					ccjs[i] = new JsonFormats().new GuildCommandConfigJson(keys[i], true, roleIDs);
				}
				guildSettings = new JsonFormats().new GuildSettingsJson();
				guildSettings.setGameCategoryID(""); //This will be set separately when configured by guild owner.
				guildSettings.setCommandConfig(ccjs);

				Gson gson = new Gson();
				printWriter.print(gson.toJson(guildSettings));
				
				printWriter.close();
				fileWriter.close();
				return guildSettingsFile;
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
	public String getGuildGameCategoryID()
	{
		return getGuildSettings().getGameCategoryID();
	}
	public void setGuildGameCategoryID(String gameCategoryID)
	{
		getGuildSettings().setGameCategoryID(gameCategoryID);
		saveGuildSettings();
	}
	public boolean getGuildCommandEnabledStatus(String commandKey)
	{
		for (GuildCommandConfigJson commandSettings : getGuildSettings().getCommandConfig())
		{
			if (commandSettings.getCommandKey().equalsIgnoreCase(commandKey))
			{
				return commandSettings.isEnabled();
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
		for (GuildCommandConfigJson commandSettings : getGuildSettings().getCommandConfig())
		{
			if (commandSettings.getCommandKey().equalsIgnoreCase(commandKey))
			{
				commandSettings.setEnabled(newStatus); //TODO: What if lockeded?
				keyFound = true;
			}

		}
		if (!keyFound)
		{
			logger.debug("Tried to find key \""+ commandKey+"\", however, it does not appear to exist.");
		}
		else
		{
			saveGuildSettings();
			logger.info("Command "+commandKey+ "'s enabled status has been changed to "+newStatus+" for guild "+guildID);
		}

	}
	public void addPermittedRole(String commandKey, String roleID)
	{
		for (int i = 0; i< getGuildSettings().getCommandConfig().length; i++)
		{
			if (commandKey.equalsIgnoreCase(getGuildSettings().getCommandConfig()[i].getCommandKey()))
			{
				if (!getGuildSettings().getCommandConfig()[i].getPermittedRoles().contains(roleID))
				{
					getGuildSettings().getCommandConfig()[i].getPermittedRoles().add(roleID);
					saveGuildSettings();
				}
			}
		}
	}
	public void removePermittedRole(String commandKey, String roleID)
	{
		for (int i = 0; i< getGuildSettings().getCommandConfig().length; i++)
		{
			if (commandKey.equalsIgnoreCase(getGuildSettings().getCommandConfig()[i].getCommandKey()))
			{
				getGuildSettings().getCommandConfig()[i].getPermittedRoles().remove(roleID);
				saveGuildSettings();
			}
		}
	}
	public ArrayList<String> getPermittedRoles(String commandKey)
	{
		for (int i = 0; i< getGuildSettings().getCommandConfig().length; i++)
		{
			if (commandKey.equalsIgnoreCase(getGuildSettings().getCommandConfig()[i].getCommandKey()))
			{
				return getGuildSettings().getCommandConfig()[i].getPermittedRoles();
			}
		}
		return null; //Invalid command key
	}
	public boolean isPermitted(Member member, String commandKey)
	{
		boolean permissionGranted = false;
		ArrayList<String> roleIDs = new ArrayList<>();
		for (Role role : member.getRoles())
		{
			roleIDs.add(role.getId());
		}
		for (GuildCommandConfigJson commandConfig : getGuildSettings().getCommandConfig())
		{
			if (commandConfig.getCommandKey().equalsIgnoreCase(commandKey))
			{
				if (!Collections.disjoint(commandConfig.getPermittedRoles(), roleIDs)) //TODO Fix
				{
					permissionGranted = true;
				}
			}
		}
		return permissionGranted;
	}
	public boolean isPermitted(Member member, Class<? extends Command> command)
	{
		return isPermitted(member, CommandRegister.getCommand(command).getCommandKey());
	}
	public ArrayList<String> getPermittedCommands(String roleID)
	{
		ArrayList<String> permittedCommandKeys = new ArrayList<String>();
		for (JsonFormats.GuildCommandConfigJson commandConfig : getGuildSettings().getCommandConfig())
		{
			if (commandConfig.getPermittedRoles().contains(roleID))
			{
				permittedCommandKeys.add(commandConfig.getCommandKey());
			}
		}
		return permittedCommandKeys;
	}
	
	/*public GuildCommandConfigJson[] getGuildCommandConfig()
	{
		ArrayList<GuildCommandConfigJson> commandConfigList = new ArrayList<GuildCommandConfigJson>();
		for (GuildCommandConfigJson commandConfig : getGuildSettings().getCommandConfig())
		{
			commandConfigList.add(commandConfig);
		}
		return commandConfigList.toArray(new GuildCommandConfigJson[commandConfigList.size()]);
	}*/
	public GuildCommandConfigJson[] getGuildEnabledCommands()
	{
		ArrayList<GuildCommandConfigJson> commandConfigList = new ArrayList<GuildCommandConfigJson>();
		for (GuildCommandConfigJson commandConfig : getGuildSettings().getCommandConfig())
		{
			if (commandConfig.isEnabled())
			{
				commandConfigList.add(commandConfig);
			}
		}
		return commandConfigList.toArray(new GuildCommandConfigJson[0]);
	}
	public GuildCommandConfigJson[] getGuildDisabledCommands()
	{
		ArrayList<GuildCommandConfigJson> commandConfigList = new ArrayList<GuildCommandConfigJson>();
		for (GuildCommandConfigJson commandConfig : getGuildSettings().getCommandConfig())
		{
			if (!commandConfig.isEnabled())
			{
				commandConfigList.add(commandConfig);
			}
		}
		return commandConfigList.toArray(new GuildCommandConfigJson[0]);
	}

	//============================================================================================================
	
}
