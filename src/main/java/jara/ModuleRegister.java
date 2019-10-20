package jara;

import java.util.*;

import module.Command;
import commands.CustomCommand;
import commands.Help;
import commands.admin.CustomCommandManager;
import commands.admin.config.ConfigMain;
import commands.utility.*;
import org.apache.commons.collections4.list.UnmodifiableList;

import static jara.ModuleRegister.Category.*;

public class ModuleRegister
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
	private static ArrayList<ModuleAttributes> register;
	private static ArrayList<ModuleAttributes> commandModules;

	private static ArrayList<ModuleAttributes> adminModules;
	private static ArrayList<ModuleAttributes> audioModules;
	private static ArrayList<ModuleAttributes> gamesModules;
	private static ArrayList<ModuleAttributes> toysModules;
	private static ArrayList<ModuleAttributes> utilityModules;
	private static ArrayList<ModuleAttributes> seasonalModules;
	private static ArrayList<ModuleAttributes> noGroupModules;
	
	/**
	 * This method returns the command list of all programmed commands, with their classes and alias arrays.<br>
	 * @return
	 */
	private static void prepareModules()
	{
		if (register == null)
		{
			register = new ArrayList<>();
			Help.HelpPage AboutHelp = new Help.HelpPage("Shows the details about this bot.");
			register.add(new ModuleAttributes("About", "Shows Bot credits.", new String[] {"Credits", "Authors"}, UTILITY, Core.getVersion(), false, About.class, AboutHelp, null, null));
			Help.HelpPage ConfigHelp = new Help.HelpPage("Modify bot settings. Instructions provided on the config menu.");
			register.add(new ModuleAttributes("Config", "Modify Bot settings.", new String[] {"Settings"}, ADMIN, Core.getVersion(), false, ConfigMain.class, ConfigHelp, null, null));
			Help.HelpPage HelpHelp = new Help.HelpPage("**General**\nList categories by using Help\n List commands in a category with Help (Category). Adding 'all' lists commands you don't have permission to use.\nUse Help (Command) to find out how to use a command.\n\n**Help Pages**\nAliases: Alternate ways to use the command\nParameters: Information to give to commands\nDescription: Detailed command information.", "Help", "Help (Command)", "Help (Category) (all)");
			register.add(new ModuleAttributes("Help", "Shows command details.", new String[] {"?", "commands"}, NOGROUP, Core.getVersion(), false, Help.class, HelpHelp, null, null));
			Help.HelpPage CCMHelp = new Help.HelpPage("Configure custom commands. Instructions provided on manager menu.", "ccm", "ccm [SubMenu] (Command)", "ccm [SubMenu] [Command] [CommandSubMenu] (Value)");
			register.add(new ModuleAttributes("CustomCommandManager", "Manage custom commands.", new String[] {"AddCustomCommand", "EditCustomCommand", "RemoveCustomCommand", "DeleteCustomCommand", "CustomCommands", "CCM"}, ADMIN, Core.getVersion(), true, CustomCommandManager.class, CCMHelp, null, null));
			register.add(new ModuleAttributes("CustomCommand", "Custom Command Template.", new String[0], NOGROUP, Core.getVersion(), false, CustomCommand.class, new Help.HelpPage(), null, null)); //This is the interface for converting custom commands to actual commands.
			register.addAll(ModuleManager.loadModules(register));			//Load modules
			register.sort(Comparator.comparing(ModuleAttributes::getKey)); //Sort the commands into alphabetical order based on their keys
		}
	}

	/**
	 * Gets an unmodifiable list of all modules.
	 * @return
	 */
	public static List<ModuleAttributes> getModules()
	{
		prepareModules();
		return Collections.unmodifiableList(register);
	}
	/**
	 * Returns all strings which can be used to trigger commands. 
	 * @return
	 * All module aliases
	 */
	public static ArrayList<String> getModuleAliases()
	{
		prepareModules();
		ArrayList<String> aliases = new ArrayList<>();
		for (ModuleAttributes moduleAttributes : register)
		{
			Collections.addAll(aliases, moduleAttributes.getAliases());
		}
		return aliases;
	}
	/**
	 * Returns all registered command keys, which can be used to obtain other data about a command.
	 * @return
	 * The keys
	 */
	public static ArrayList<String> getModuleKeys()
	{
		prepareModules();
		ArrayList<String> keys = new ArrayList<>();
		for (ModuleAttributes moduleAttributes : register)
		{
			keys.add(moduleAttributes.getKey());
		}
		return keys;
	}

	/**
	 * Gets all modules that have a command class
	 * @return
	 */
	public static ArrayList<ModuleAttributes> getCommandModules()
	{
		if (commandModules == null)
		{
			prepareModules();
			ArrayList<ModuleAttributes> commandModulesBuilder = new ArrayList<>();
			for (ModuleAttributes ma : register)
			{
				if (ma.getCommandClass() != null)
				{
					commandModulesBuilder.add(ma);
				}
			}
			commandModules = commandModulesBuilder;
		}
		return commandModules;
	}

	/**
	 * Gets the keys of all command modules.
	 * @return
	 */
	public static ArrayList<String> getCommandModuleKeys()
	{
		ArrayList<String> keys = new ArrayList<>();
		for (ModuleAttributes ma : getCommandModules())
		{
			keys.add(ma.getKey());
		}
		return keys;
	}
	/**
	 * Returns the class' attributes. Note this does not include any configuration details, so is true for guild & global contexts.
	 * 
	 * @param alias - A command triggering string. Using the command's key is most efficient.
	 * @return
	 * CommandAttributes - All details about the class.<br>
	 * null - Key does not exist.
	 */
	public static ModuleAttributes getModule(String alias)
	{
		try
		{
			prepareModules();
			int min = 0;
			int max = getRegisterSize()-1;

			/*================================================================
			We first check for command keys, as this should be what the
			majority of requests use, thus saving us having to trawl through
			ALL aliases when we don't have to.
			================================================================*/

			while (min <= max)
			{
				int mid = (int) Math.floor((min+max)/2);
				ModuleAttributes ma = register.get(mid);
				if (ma.getKey().compareToIgnoreCase(alias) < 0)
				{
					min = mid+1;
				}
				else if (ma.getKey().compareToIgnoreCase(alias) > 0)
				{
					max = mid-1;
				}
				else if (ma.getKey().compareToIgnoreCase(alias) == 0)
				{
					return ma;
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

			for (ModuleAttributes moduleAttributes : register)
			{
				min = 0;
				max = moduleAttributes.getAliases().length-1;
				while (min <= max)
				{
					int mid = (int) Math.floor((min+max)/2);
					String moduleAlias = moduleAttributes.getAliases()[mid].toLowerCase();
					if (moduleAlias.compareToIgnoreCase(alias) < 0)
					{
						min = mid+1;
					}
					else if (moduleAlias.compareToIgnoreCase(alias) > 0)
					{
						max = mid-1;
					}
					else if (moduleAlias.compareToIgnoreCase(alias) == 0)
					{
						return moduleAttributes;
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
	 * Returns the total count of all registered modules.
	 * @return
	 * int - # of registered commands.
	 */
	public static int getRegisterSize()
	{
		prepareModules();
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
	public static ModuleAttributes getModule(Class<? extends Command> clazz)
	{
		prepareModules();
		if (clazz != null)
		{
			for (ModuleAttributes moduleAttributes : register)
			{
				if (clazz.equals(moduleAttributes.getCommandClass()))
				{
					return moduleAttributes;
				}
			}
		}
		return null; //Invalid class
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
	 * Returns the Module Attributes all all modules in this category.
	 * @param category
	 */
	public static ArrayList<ModuleAttributes> getModulesInCategory(Category category)
	{
		ArrayList<ModuleAttributes> categoryModules;
		switch (category)
		{
			case NOGROUP:
				categoryModules = noGroupModules;
				break;
			case GAMES:
				categoryModules = gamesModules;
				break;
			case UTILITY:
				categoryModules = utilityModules;
				break;
			case TOYS:
				categoryModules = toysModules;
				break;
			case AUDIO:
				categoryModules = audioModules;
				break;
			case ADMIN:
				categoryModules = adminModules;
				break;
			case SEASONAL:
				categoryModules = seasonalModules;
				break;
			default:
				return null;
		}
		if (categoryModules != null)
		{
			return categoryModules;
		}
		else
		{
			return generateModulesInCategory(category);
		}
	}

	/**
	 * Generates the list of commands in this category. Use getModulesInCategory() instead where possible for cached results.
	 * @param categoryID
	 * @return
	 */
	private static ArrayList<ModuleAttributes> generateModulesInCategory(Category categoryID)
	{
		prepareModules();
		ArrayList<ModuleAttributes> categoryModules = new ArrayList<>();
		for (ModuleAttributes moduleAttributes : register)
		{
			if (moduleAttributes.getCategory().equals(categoryID))
			{
				categoryModules.add(moduleAttributes);
			}
		}
		switch (categoryID)
		{
			case NOGROUP:
				noGroupModules = categoryModules;
				break;
			case GAMES:
				gamesModules = categoryModules;
				break;
			case UTILITY:
				utilityModules = categoryModules;
				break;
			case TOYS:
				toysModules = categoryModules;
				break;
			case AUDIO:
				audioModules = categoryModules;
				break;
			case ADMIN:
				adminModules = categoryModules;
				break;
			case SEASONAL:
				seasonalModules = categoryModules;
				break;
			default:
				return null;
		}
		return categoryModules;
	}
	
}
