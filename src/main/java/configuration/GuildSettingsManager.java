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

import jara.CommandAttributes;
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

	/**
	 * Returns the base settings directory.
	 * @return
	 * File - The dir where Jara stores settings
	 */
	public File getDirectory()
	{
		return GlobalSettingsManager.getDirectory();
	}
	
	//==================================== Guild Specific Tools ==================================================

	/**
	 * Returns the directory which stores guild settings files.
	 * @return
	 * File - Guild Settings directory
	 */
	private File getGuildSettingsDirectory()
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
	 * Returns the file for this guild's settings data
	 * @return
	 * File - Guild settings file
	 */
	public File getGuildSettingsFile()
	{
		Logger logger = LoggerFactory.getLogger(GuildSettingsManager.class);
		File guildSettingsFile = new File(getGuildSettingsDirectory().getAbsolutePath()+"/"+guildID+".json");
		if (!guildSettingsFile.exists())
		{
			logger.error("Guild "+guildID+"'s settings have gone missing, or never existed. Creating new config.");
			performNewGuildSetup();
		}
		return guildSettingsFile;
	}

	/**
	 * Guess.
	 * @return
	 * true - File deleted
	 * false - File not deleted
	 */
	public boolean deleteGuildSettingsFile()
	{
		return getGuildSettingsFile().delete();
	}

	/**
	 * Returns the settings as they are stored in the file, or the current working settings if the file has already been read.
	 * @return
	 * GuildSettingsJson - Settings as stored in file
	 */
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

	/**
	 * Saves any modifications to guildSettings back to file.
	 */
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

	/**
	 * Creates a new file with default settings with which to store guild configuration with.
	 * @return
	 * File - the new guild data file
	 */
	public File performNewGuildSetup() //TODO: Make setup reflect global? (So globally disabled commands are... disabled.)
	{
		File guildSettingsFile = new File(getGuildSettingsDirectory().getAbsolutePath()+"/"+guildID+".json");
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

	/**
	 * The ID for the category in which new games are created in.
	 * @return
	 * String - ID of the game channel category
	 * "" - No category set.
	 */
	public String getGuildGameCategoryID()
	{
		return getGuildSettings().getGameCategoryID();
	}

	/**
	 * Sets the ID of the game category, where game channels are placed.
	 * @param gameCategoryID - the category ID
	 */
	public void setGuildGameCategoryID(String gameCategoryID)
	{
		getGuildSettings().setGameCategoryID(gameCategoryID);
		saveGuildSettings();
	}

	/**
	 * Returns whether the specified command is enabled for this guild.
	 * @param commandKey - The command to check
	 * @return
	 * true - Command is enabled
	 * false - Command is disabled
	 */
	public boolean isCommandEnabled(String commandKey)
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

	/**
	 * This method will attempt to set the command's status to newStatus.<br>
	 * Some commands cannot be disabled, however, as these would cause issues, such as /Help or /Config
	 * @param commandKey - The command to update
	 * @param newStatus - The new state for the command
	 */
	public void setCommandEnabled(String commandKey, boolean newStatus)
	{
		Logger logger = LoggerFactory.getLogger(GuildSettingsManager.class);
		boolean keyFound = false;
		boolean updatedState = !newStatus;
		for (GuildCommandConfigJson commandSettings : getGuildSettings().getCommandConfig())
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
			saveGuildSettings();
			if (updatedState == newStatus)
			{
				logger.info("Command "+commandKey+ "'s enabled status has been changed to "+newStatus+" for guild "+guildID);
			}
			else
			{
				logger.info("Could not change the status of "+commandKey+" in "+guildID+", it is a locked command.");
			}
		}

	}

	/**
	 * Adds permission for a role to use a command.<br>
	 *     For @everyone, use the guild's ID.
	 * @param commandKey - The command to update
	 * @param roleID - The role to add
	 */
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
	/**
	 * Removes permission for a role to use a command.<br>
	 *     For @everyone, use the guild's ID.
	 * @param commandKey - The command to update
	 * @param roleID - The role to remove
	 */
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

	/**
	 * Returns a list of roles that have permission to use this command (including the everyone role)
	 * @param commandKey - THe command to check
	 * @return
	 * ArrayList<String> - the role list
	 */
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

	/**
	 * Checks to see if the specified member has any roles which allow them to use the command
	 * @param member - The user to check
	 * @param commandKey - The command to check
	 * @return
	 * true - Member can use this command
	 * false - Member cannot use this command, or it does not exist
	 */
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
				if (!Collections.disjoint(commandConfig.getPermittedRoles(), roleIDs))
				{
					permissionGranted = true;
				}
			}
		}
		return permissionGranted;
	}
	/**
	 * Checks to see if the specified member has any roles which allow them to use the command
	 * @param member - The user to check
	 * @param command - The command to check
	 * @return
	 * true - Member can use this command
	 * false - Member cannot use this command, or it does not exist
	 */
	public boolean isPermitted(Member member, Class<? extends Command> command)
	{
		return isPermitted(member, CommandRegister.getCommand(command).getCommandKey());
	}

	/**
	 * Checks to see all commands a role can use.
	 * @param roleID - The role to check
	 * @return
	 * ArrayList<String> - A list of the command keys
	 */
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

	/**
	 * Returns the command keys for all commands enabled in this guild
	 * @return
	 * String[] - Array of enabled command keys
	 */
	public String[] getGuildEnabledCommandKeys()
	{
		ArrayList<String> keys = new ArrayList<String>();
		for (JsonFormats.GuildCommandConfigJson config : getGuildSettings().getCommandConfig())
		{
			if (config.isEnabled())
			{
				keys.add(config.getCommandKey());
			}
		}
		return keys.toArray(new String[0]);
	}
	/**
	 * Returns the command keys for all commands disabled in this guild
	 * @return
	 * String[] - Array of disabled command keys
	 */
	public String[] getGuildDisabledCommandKeys()
	{
		ArrayList<String> keys = new ArrayList<String>();
		for (JsonFormats.GuildCommandConfigJson config : getGuildSettings().getCommandConfig())
		{
			if (!config.isEnabled())
			{
				keys.add(config.getCommandKey());
			}
		}
		return keys.toArray(new String[0]);
	}

	/**
	 * Sets all commands (where possible) in the category to the new status
	 * @param categoryName - The category name of commands to update
	 * @param newStatus - The new state
	 */
	public void setCategoryEnabled(String categoryName, boolean newStatus)
	{
		setCategoryEnabled(CommandRegister.getCommandCategory(categoryName), newStatus);
	}
	/**
	 * Sets all commands (where possible) in the category to the new status
	 * @param categoryID - The category ID of commands to update
	 * @param newStatus - The new state
	 */
	public void setCategoryEnabled(int categoryID, boolean newStatus)
	{
		Logger logger = LoggerFactory.getLogger(GlobalSettingsManager.class);
		if (CommandRegister.getCategoryName(categoryID) == null) //Simple verification check to ensure the id is valid.
		{
			logger.info("Could not alter category status as the specified category does not exist.");
			return;
		}
		for (CommandAttributes commandAttributes : CommandRegister.getCommandsInCategory(categoryID))
		{
			for (int i = 0; i<getGuildSettings().getCommandConfig().length; i++)
			{
				if (getGuildSettings().getCommandConfig()[i].getCommandKey().equalsIgnoreCase(commandAttributes.getCommandKey())) //TODO: Inefficient. Fix.
				{
					getGuildSettings().getCommandConfig()[i].setEnabled(newStatus);
				}
			}
		}
	}

	//============================================================================================================
	
}
