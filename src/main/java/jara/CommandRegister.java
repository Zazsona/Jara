package jara;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import commands.Command;
import commands.CustomCommand;
import commands.Help;
import commands.admin.CustomCommandManager;
import commands.admin.config.ConfigMain;
import commands.utility.*;

import static jara.CommandRegister.Category.*;

public class CommandRegister
{
	public enum Category
	{
		NOGROUP,
		GAMES,
		UTILITY,
		TOYS,
		AUDIO,
		ADMIN,
		SEASONAL
	}
	private static ArrayList<CommandAttributes> register;

	private static ArrayList<CommandAttributes> adminCommands;
	private static ArrayList<CommandAttributes> audioCommands;
	private static ArrayList<CommandAttributes> gamesCommands;
	private static ArrayList<CommandAttributes> toysCommands;
	private static ArrayList<CommandAttributes> utilityCommands;
	private static ArrayList<CommandAttributes> seasonalCommands;
	private static ArrayList<CommandAttributes> noGroupCommands;
	/*
	 * When implementing a new command, is is essential to add it to the getRegister() method. Otherwise, it will be ignored at run time.
	 */
	
	/**
	 * This method returns the command list of all programmed commands, with their classes and alias arrays.<br>
	 * @return
	 *CommandAttributes[] - An array of all programmed commands.
	 */
	public static CommandAttributes[] getRegister()
	{
		if (register == null)
		{
			register = new ArrayList<>();
			register.add(new CommandAttributes("About", "Shows Bot credits.", About.class, new String[] {"Credits", "Authors"}, UTILITY, false));
			register.add(new CommandAttributes("Config", "Modify Bot settings.", ConfigMain.class, new String[] {"Settings"}, ADMIN, false));
			register.add(new CommandAttributes("Help", "Shows command details.", Help.class, new String[] {"?", "commands"}, NOGROUP, false));
			register.add(new CommandAttributes("CustomCommandManager", "Manage custom commands.", CustomCommandManager.class, new String[] {"AddCustomCommand", "EditCustomCommand", "RemoveCustomCommand", "DeleteCustomCommand", "CustomCommands", "CCM"}, ADMIN, true));
			register.add(new CommandAttributes("CustomCommand", "Custom Command Template.", CustomCommand.class, new String[0], NOGROUP, false)); //This is the interface for converting custom commands to actual commands.
			addDefaultHelpPages(register);
			register.addAll(new ModuleManager().loadModules(register));			//Load mods
			register.sort(Comparator.comparing(CommandAttributes::getCommandKey)); //Sort the commands into alphabetical order based on their keys
		}
		return register.toArray(new CommandAttributes[0]);
	}

	/**
	 * Adds help pages for all the in-built (i.e not module) commands.
	 * @param partialRegister the register before adding modules.
	 */
	private static void addDefaultHelpPages(ArrayList<CommandAttributes> partialRegister)
	{
		for (int i = 0; i<partialRegister.size(); i++)
		{
			switch (partialRegister.get(i).getCommandKey())
			{
				case "About":
					Help.HelpPage AboutHelp = new Help.HelpPage("Shows the details about this bot.", new String[0]);
					for (String alias : register.get(i).getAliases())
					{
						Help.addPage(alias, AboutHelp);
					}
					break;
				case "Config":
					Help.HelpPage ConfigHelp = new Help.HelpPage("Modify bot settings. Instructions provided on the config menu.", new String[0]);
					for (String alias : register.get(i).getAliases())
					{
						Help.addPage(alias, ConfigHelp);
					}
					break;
				case "Help":
					Help.HelpPage HelpHelp = new Help.HelpPage("**General**\nList categories by using Help\n List commands in a category with Help (Category). Adding 'all' lists commands you don't have permission to use.\nUse Help (Command) to find out how to use a command.\n\n**Help Pages**\nAliases: Alternate ways to use the command\nParameters: Information to give to commands\nDescription: Detailed command information.", new String[] {"Help", "Help (Command)", "Help (Category) (all)"});
					for (String alias : register.get(i).getAliases())
					{
						Help.addPage(alias, HelpHelp);
					}
					break;
				case "CustomCommandManager":
					Help.HelpPage CCMHelp = new Help.HelpPage("Configure custom commands. Instructions provided on manager menu.", new String[0]);
					for (String alias : register.get(i).getAliases())
					{
						Help.addPage(alias, CCMHelp);
					}
					break;
				default:
					Help.HelpPage defaultHelp = new Help.HelpPage();
					for (String alias : register.get(i).getAliases())
					{
						Help.addPage(alias, defaultHelp);
					}
					break;
			}
		}
	}
	/**
	 * Returns all strings which can be used to trigger commands. 
	 * @return
	 * String[] - All command aliases
	 */
	public static String[] getAllCommandAliases()
	{
		getRegister();
		ArrayList<String> aliases = new ArrayList<>();
		for (CommandAttributes commandAttributes : register)
		{
			Collections.addAll(aliases, commandAttributes.getAliases());
		}
		return aliases.toArray(new String[0]);
	}
	/**
	 * Returns all Classes which are registered commands
	 * @return
	 * ArrayList<Class<? extends Command>> - The classes.
	 */
	public static ArrayList<Class<? extends Command>> getAllCommandClasses()
	{
		getRegister();
		ArrayList<Class<? extends Command>> classes = new ArrayList<>();
		for (CommandAttributes commandAttributes : register)
		{
			classes.add(commandAttributes.getCommandClass());
		}
		return classes;
	}
	/**
	 * Returns all registered command keys, which can be used to obtain other data about a command.
	 * 
	 * @return
	 * String[] - The keys
	 */
	public static String[] getAllCommandKeys()
	{
		getRegister();
		ArrayList<String> keys = new ArrayList<>();
		for (CommandAttributes commandAttributes : register)
		{
			keys.add(commandAttributes.getCommandKey());
		}
		return keys.toArray(new String[0]);
	}
	/**
	 * Returns the class' attributes. Note this does not include any configuration details, so is true for guild & global contexts.
	 * 
	 * @param alias - A command triggering string. Using the command's key is most efficient.
	 * @return
	 * CommandAttributes - All details about the class.<br>
	 * null - Key does not exist.
	 */
	public static CommandAttributes getCommand(String alias)
	{
		try
		{
			getRegister();
			int min = 0;
			int max = getRegisterSize();

			/*================================================================
			We first check for command keys, as this should be what the
			majority of requests use, thus saving us having to trawl through
			ALL aliases when we don't have to.
			================================================================*/

			while (min <= max)
			{
				int mid = (max+min)/2;

				if (getRegister()[mid].getCommandKey().compareToIgnoreCase(alias) < 0)
				{
					min = mid+1;
				}
				else if (getRegister()[mid].getCommandKey().compareToIgnoreCase(alias) > 0)
				{
					max = mid-1;
				}
				else if (getRegister()[mid].getCommandKey().compareToIgnoreCase(alias) == 0)
				{
					return getRegister()[mid];
				}
			}

			/*===============================================================
			Well shit, it's not a key.

			You can do many things while this runs.
			I find a fan favourite is annoying a friend.

			Other suggestions are:
			- Make a cuppa
			- Fix Northern Rail's train timetables
			- Play a flash game
			- Mark everything as duplicate on SO

			================================================================*/

			for (CommandAttributes commandAttributes : register) //TODO: This is still wank. TextVoiceChannels has anything behind the key break.
			{
				min = 0;
				max = commandAttributes.getAliases().length-1;
				while (min <= max)
				{
					int mid = (max+min)/2;
					if (commandAttributes.getAliases()[mid].compareToIgnoreCase(alias) < 0)
					{
						min = mid+1;
					}
					else if (commandAttributes.getAliases()[mid].compareToIgnoreCase(alias) > 0)
					{
						max = mid-1;
					}
					else if (commandAttributes.getAliases()[mid].compareToIgnoreCase(alias) == 0)
					{
						return commandAttributes;
					}
				}
			}
			return null; //Bad alias
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			return null; //Command is not in register.
		}

	}
	/**
	 * Returns the total count of all registered commands.
	 * @return
	 * int - # of registered commands.
	 */
	public static int getRegisterSize()
	{
		getRegister();
		return register.size();
	}
	/**
	 * Returns the class' attributes. Note this does not include any configuration details, so is true for guild & global contexts.
	 * 
	 * @param clazz - A class that extends Command
	 * @return
	 * CommandAttributes - All details about the class.<br>
	 * null - Class is not a registered command class.
	 */
	public static CommandAttributes getCommand(Class<? extends Command> clazz)
	{
		getRegister();
		for (CommandAttributes commandAttributes : register)
		{
			if (commandAttributes.getCommandClass().equals(clazz))
			{
				return commandAttributes;
			}
		}
		return null; //Invalid class
	}
	/**
	 * Returns the command's category ID.<br>
	 * @param key - Command key
	 * @return
	 * Category - The category ID.<br>
	 * null - Command key does not exist.
	 */
	public static Category getCommandCategory(String key)
	{
		return getCommand(key).getCategory();
	}
	/**
	 * Converts a category ID into a category name.
	 * @param id - The ID number for the category
	 * @return
	 * String - Category name
	 * null = Invalid id.
	 */
	public static String getCategoryName(Category id)
	{
		switch (id) 
		{
			case NOGROUP:
				return "No Group";
			case GAMES:
				return "Games";
			case UTILITY:
				return "Utility";
			case TOYS:
				return "Toys";
			case AUDIO:
				return "Audio";
			case ADMIN:
				return "Admin";
			case SEASONAL:
				return "Seasonal";
		}
		return null; //Invalid id.
	}
	/**
	 * Converts a category ID into a category name.
	 * @param name - The name of the category
	 * @return
	 * Category - Category ID
	 * null = Invalid name.
	 */
	public static Category getCategoryID(String name)
	{
		name = name.toLowerCase();
		switch (name)
		{
			case "nogroup":
				return NOGROUP;
			case "no group":
				return NOGROUP;
			case "games":
				return GAMES;
			case "utility":
				return UTILITY;
			case "toys":
				return TOYS;
			case "audio":
				return AUDIO;
			case "admin":
				return ADMIN;
			case "seasonal":
				return SEASONAL;
		}
		return null; //Invalid name.
	}

	/**
	 * Returns a list of all category names.
	 * @return
	 *ArrayList<String> - the names
	 */
	public static ArrayList<String> getCategoryNames()
	{
		ArrayList<String> names = new ArrayList<>();
		for (Category category : Category.values())
		{
			names.add(getCategoryName(category));
		}
		return names;
	}

	/**
	 * Returns the Command Attributes all all commands in this category.
	 * @param category
	 * @return CommandAttributes[] - Array of attributes.
	 */
	public static CommandAttributes[] getCommandsInCategory(Category category)
	{
		ArrayList<CommandAttributes> categoryCommands;
		switch (category)
		{
			case NOGROUP:
				categoryCommands = noGroupCommands;
				break;
			case GAMES:
				categoryCommands = gamesCommands;
				break;
			case UTILITY:
				categoryCommands = utilityCommands;
				break;
			case TOYS:
				categoryCommands = toysCommands;
				break;
			case AUDIO:
				categoryCommands = audioCommands;
				break;
			case ADMIN:
				categoryCommands = adminCommands;
				break;
			case SEASONAL:
				categoryCommands = seasonalCommands;
				break;
			default:
				return null;
		}
		if (categoryCommands != null)
		{
			return categoryCommands.toArray(new CommandAttributes[0]);
		}
		else
		{
			return generateCommandsInCategory(category);
		}
	}

	/**
	 * Generates the list of commands in this category. Use getCommandsInCategory() instead where possible for cached results.
	 * @param categoryID
	 * @return
	 */
	private static CommandAttributes[] generateCommandsInCategory(Category categoryID)
	{
		getRegister();
		ArrayList<CommandAttributes> categoryCommands = new ArrayList<>();
		for (CommandAttributes cmdAttributes : register)
		{
			if (cmdAttributes.getCategory().equals(categoryID))
			{
				categoryCommands.add(cmdAttributes);
			}
		}
		switch (categoryID)
		{
			case NOGROUP:
				noGroupCommands = categoryCommands;
				break;
			case GAMES:
				gamesCommands = categoryCommands;
				break;
			case UTILITY:
				utilityCommands = categoryCommands;
				break;
			case TOYS:
				toysCommands = categoryCommands;
				break;
			case AUDIO:
				audioCommands = categoryCommands;
				break;
			case ADMIN:
				adminCommands = categoryCommands;
				break;
			case SEASONAL:
				seasonalCommands = categoryCommands;
				break;
			default:
				return null;
		}
		return categoryCommands.toArray(new CommandAttributes[0]);
	}
	
}
