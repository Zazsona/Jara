package jara;

import java.util.ArrayList;

import commands.Command;
import commands.Help;
import commands.config.Config;
import commands.games.Countdown;
import commands.standard.About;
import commands.standard.CoinFlip;
import commands.standard.EightBall;
import commands.standard.Jokes;
import commands.standard.Ping;
import commands.standard.Report;

public class CommandRegister
{
	public static final int NOGROUP = 0;
	public static final int GAMES = 1;
	public static final int STANDARD = 2;
	public static final int AUDIO = 3;
	public static final int ADMIN = 4;
	private static ArrayList<CommandAttributes> register;
	/**
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
			register = new ArrayList<CommandAttributes>();
			/*============================================
			 * 
			 * The layout for adding a new class should be quite simple.
			 * Simply create a new CommandAttributes class in the list, and pass the Command Key, Command Class, and then any aliases.
			 * All other operations (Adding them to settings, indexing them at boot, etc.) will be done automatically.
			 * 
			 * I also highly recommend you include details about the command in Help.class, such as parameters and what it does. Aliases and category will be done automatically.
			 * 
			 * NOTE: ALWAYS ADD YOUR NEW COMMAND AT THE END FOR PROPER UPDATING OF LEGACY CONFIGS
			 * ===========================================
			 */
			register.add(new CommandAttributes("Ping", Ping.class, new String[] {"Pong", "Test"}, STANDARD));
			register.add(new CommandAttributes("Report", Report.class, new String[] {"Status", "Stats"}, STANDARD));
			register.add(new CommandAttributes("About", About.class, new String[] {"Credits", "Authors"}, STANDARD));
			register.add(new CommandAttributes("EightBall", EightBall.class, new String[] {"8ball", "helix", "fortune"}, STANDARD));
			register.add(new CommandAttributes("Jokes", Jokes.class, new String[] {"Joke", "Comedy"}, STANDARD));
			register.add(new CommandAttributes("CoinFlip", CoinFlip.class, new String[] {"FlipCoin", "Toss", "cf", "fc", "fiftyfifty", "flipacoin"}, STANDARD));
			register.add(new CommandAttributes("Config", Config.class, new String[] {"Settings"}, ADMIN));
			register.add(new CommandAttributes("Countdown", Countdown.class, new String[] {"cd"}, GAMES));
			register.add(new CommandAttributes("Help", Help.class, new String[] {"?", "commands"}, NOGROUP)); //Does help REALLY need to be indexed in help?
		}
		return register.toArray(new CommandAttributes[register.size()]);
	}
	/*
	 * Returns all strings which can be used to trigger commands. 
	 * @return
	 * String[] - All command aliases
	 */
	public static String[] getAllCommandAliases()
	{
		getRegister();
		ArrayList<String> aliases = new ArrayList<String>();
		for (CommandAttributes commandAttributes : register)
		{
			for (String alias : commandAttributes.getAliases())
			{
				aliases.add(alias);
			}
		}
		return aliases.toArray(new String[aliases.size()]); 
	}
	/*
	 * Returns all Classes which are registered commands
	 * @return
	 * ArrayList<Class<? extends Command>> - The classes.
	 * 
	 */
	public static ArrayList<Class<? extends Command>> getAllCommandClasses()
	{
		getRegister();
		ArrayList<Class<? extends Command>> classes = new ArrayList<Class<? extends Command>>();
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
		ArrayList<String> keys = new ArrayList<String>();
		for (CommandAttributes commandAttributes : register)
		{
			keys.add(commandAttributes.getCommandKey());
		}
		return keys.toArray(new String[keys.size()]);
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
		getRegister();
		for (CommandAttributes commandAttributes : register)					//================================================================
		{
			if (commandAttributes.getCommandKey().equalsIgnoreCase(alias))		//We first check for command keys, as this should be what the
			{																	//majority of requests use, thus saving us having to trawl through
				return commandAttributes;										//ALL aliases when we don't have to.
			}
		}																		//================================================================
		
																				//Well shit, it's not a key.
		boolean match = false;
		for (CommandAttributes commandAttributes : register)					//================================================================
		{
			for (String regAlias : commandAttributes.getAliases())				//You can do many things while this runs.
			{																	//I find a fan favourite is annoying a fellow human.
				if (alias.equalsIgnoreCase(regAlias))							
				{																//Other suggestions are:
					match = true;												//Make a cuppa
					break;														//Find out what a "It's coming home" is.
				}																//Fix Northern Rail's train timetables
			}																	//Play a flash game
			if (match == true)													//Rent a dog
			{																	//Mark everything as duplicate on SO
				return commandAttributes;										//Come up with a faster algorithm.
			}
		}																		//================================================================
		return null; //Bad alias
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
	 * @param class - A class that extends Command
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
	 * 0 = NOGROUP<br>
	 * 1 = GAMES<br>
	 * 2 = STANDARD<br>
	 * 3 = AUDIO<br>
	 * 4 = CONFIG<br>
	 * @param key
	 * @return
	 * 0-4 - The category ID.<br>
	 * -1 - Command key does not exist.
	 */
	public static int getCommandCategory(String key)
	{
		getRegister();
		for (CommandAttributes commandAttributes : register)
		{
			if (commandAttributes.getCommandKey().equalsIgnoreCase(key))
			{
				return commandAttributes.getCategoryID();
			}
		}
		return -1; //Invalid key
	}
	/**
	 * Converts a category ID into a category name.
	 * @param id
	 * @return
	 * String - Category name
	 * null = Invalid id.
	 */
	public static String getCategoryName(int id)
	{
		switch (id) 
		{
		case 0:
			return "No Group";
		case 1:
			return "Games";
		case 2:
			return "Standard";
		case 3:
			return "Audio";
		case 4:
			return "Config";
		}
		return null; //Invalid id.
	}
	public static CommandAttributes[] getCommandsInCategory(int categoryID)
	{
		ArrayList<CommandAttributes> cmdsInCat = new ArrayList<CommandAttributes>();
		for (CommandAttributes cmdAttributes : register)
		{
			if (cmdAttributes.getCategoryID() == categoryID)
			{
				cmdsInCat.add(cmdAttributes);
			}
		}
		return cmdsInCat.toArray(new CommandAttributes[cmdsInCat.size()]);
	}
	
}
