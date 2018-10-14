package jara;

import java.util.ArrayList;
import java.util.Collections;

import commands.Command;
import commands.CustomCommand;
import commands.Help;
import commands.admin.AddCommand;
import commands.admin.config.ConfigMain;
import commands.audio.*;
import commands.games.Countdown;
import commands.games.CountdownConundrum;
import commands.games.Hangman;
import commands.toys.*;
import commands.utility.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

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
		ADMIN
	}
	private static ArrayList<CommandAttributes> register;

	private static ArrayList<CommandAttributes> adminCommands;
	private static ArrayList<CommandAttributes> audioCommands;
	private static ArrayList<CommandAttributes> gamesCommands;
	private static ArrayList<CommandAttributes> toysCommands;
	private static ArrayList<CommandAttributes> utilityCommands;
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
			/*============================================
			 * 
			 * The layout for adding a new class should be quite simple.
			 * Simply create a new CommandAttributes class in the list, and pass the Command properties.
			 * All other operations (Adding them to settings, indexing them at boot, etc.) will be done automatically.
			 * 
			 * I also highly recommend you include details about the command in Help, such as parameters and what it does. Aliases and category will be done automatically.
			 * ===========================================
			 */
			register.add(new CommandAttributes("Ping", "Tests the connection.", Ping.class, new String[] {"Pong", "Test"}, UTILITY, true));
			register.add(new CommandAttributes("Report", "Displays Bot stats.", Report.class, new String[] {"Status", "Stats"}, UTILITY, true));
			register.add(new CommandAttributes("About", "Shows Bot credits.", About.class, new String[] {"Credits", "Authors"}, UTILITY, false));
			register.add(new CommandAttributes("EightBall", "Tells your fortune.", EightBall.class, new String[] {"8ball", "helix", "fortune"}, TOYS, true));
			register.add(new CommandAttributes("Jokes", "This command is a joke.", Jokes.class, new String[] {"Joke", "Comedy"}, TOYS, true));
			register.add(new CommandAttributes("CoinFlip", "Flips a coin.", CoinFlip.class, new String[] {"FlipCoin", "Toss", "cf", "fc", "fiftyfifty", "flipacoin"}, UTILITY, true));
			register.add(new CommandAttributes("Config", "Modify Bot settings.", ConfigMain.class, new String[] {"Settings"}, ADMIN, false));
			register.add(new CommandAttributes("Countdown", "The classic word making game.", Countdown.class, new String[] {"cd"}, GAMES, true));
			register.add(new CommandAttributes("Help", "Shows command details.", Help.class, new String[] {"?", "commands"}, NOGROUP, false)); //Does help REALLY need to be indexed in help?
            register.add(new CommandAttributes("Randomizer", "Randomises numbers.", Randomizer.class, new String[] {"Randomise", "Randomize", "Randomiser", "Roulette", "Picker", "Selector"}, UTILITY, true));
			register.add(new CommandAttributes("Say", "Make the bot echo.", Say.class, new String[] {"Speak", "Talk"}, TOYS, true));
			register.add(new CommandAttributes("Timecard", "Spongebob Timecards.", Timecard.class, new String[] {"SpongebobCard", "Timescreen"}, TOYS, true));
			register.add(new CommandAttributes("CountdownConundrum", "Solve the anagram.", CountdownConundrum.class, new String[] {"CC", "Anagram"}, GAMES, true));
			register.add(new CommandAttributes("IsItWednesdayMyDudes", "Well? Is it?", IsItWednesdayMyDudes.class, new String[] {"ItIsWednesdayMyDudes", "ItIsNotWednesdayMyDudes", "Wednesday", "IIWMD", "IINWMD", "WednesdayFrog", "IsItWednesdayMyDudes", "ItIsWeds", "ItIsNotWeds"}, TOYS, true));
			register.add(new CommandAttributes("WouldYouRather", "Find out who your mates really are.", WouldYouRather.class, new String[] {"WouldYouRather?", "WYR", "WYR?"}, TOYS, true));
			register.add(new CommandAttributes("Hangman", "Guess the word.", Hangman.class, new String[] {"hang"}, GAMES, true));
			register.add(new CommandAttributes("Play", "Plays music.", Play.class, new String[] {"PlayAudio", "Music", "Track", "Radio", "Stream", "DJ"}, AUDIO, true));
			register.add(new CommandAttributes("Skip", "Votes to skip the track", Skip.class, new String[] {"Stop", "Pass", "Next"}, AUDIO, true));
			register.add(new CommandAttributes("ForceSkip", "Forces the track to skip.", ForceSkip.class, new String[] {"AdminSkip", "InstaSkip", "FireTheDJ", "ForceNext"}, AUDIO, true));
			register.add(new CommandAttributes("NowPlaying", "Current track details.", NowPlaying.class, new String[] {"NP", "CP", "CurrentlyPlaying", "TrackInfo", "SongInfo", "MusicInfo", "AudioInfo"}, AUDIO, true));
			register.add(new CommandAttributes("Pause", "Pauses current track.", Pause.class, new String[] {"Resume"}, AUDIO, true));
			register.add(new CommandAttributes("Replay", "Adds current track to queue again.", Replay.class, new String[] {"Repeat"}, AUDIO, true));
			register.add(new CommandAttributes("AddCommand", "Adds a custom command.", AddCommand.class, new String[] {"CustomCommand"}, ADMIN, true));
			register.add(new CommandAttributes("CustomCommand", "Custom Command Template.", CustomCommand.class, new String[0], NOGROUP, false)); //TODO: Make this disableable

			register.sort((ca1, ca2) ->
						  {
							  try
							  {
								  int charIndex = 0;
								  char ca1KeyChar = ca1.getCommandKey().charAt(charIndex);
								  char ca2KeyChar = ca2.getCommandKey().charAt(charIndex);
								  while (ca1KeyChar == ca2KeyChar)
								  {
									  charIndex++;
									  ca1KeyChar = ca1.getCommandKey().charAt(charIndex);
									  ca2KeyChar = ca2.getCommandKey().charAt(charIndex);
								  }
								  return Character.compare(ca1KeyChar, ca2KeyChar);
							  }
							  catch (IndexOutOfBoundsException e) //This will throw if two commands have the same key. This should NEVER occur.
							  {
								  return 0;
							  }

						  });
		}
		return register.toArray(new CommandAttributes[0]);
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
	 * 
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
		getRegister();
		int min = 0;
		int max = getRegisterSize();
		int charIndex = 0;
		/*================================================================
		We first check for command keys, as this should be what the
		majority of requests use, thus saving us having to trawl through
		ALL aliases when we don't have to.
		================================================================*/
		while (min <= max)
		{
			int mid = (max+min)/2;

			if (getRegister()[mid].getCommandKey().charAt(charIndex) < alias.charAt(charIndex))
			{
				min = mid+1;
				charIndex = 0;
			}
			else if (getRegister()[mid].getCommandKey().charAt(charIndex) > alias.charAt(charIndex))
			{
				max = mid-1;
				charIndex = 0;
			}
			else if (getRegister()[mid].getCommandKey().charAt(charIndex) == alias.charAt(charIndex))
			{
				if (getRegister()[mid].getCommandKey().equalsIgnoreCase(alias))
				{
					return getRegister()[mid];
				}
				charIndex++;
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
		- Implement this better

		================================================================*/

		boolean match = false;
		for (CommandAttributes commandAttributes : register)
		{
			for (String regAlias : commandAttributes.getAliases())
			{
				if (alias.equalsIgnoreCase(regAlias))							
				{
					match = true;
					break;
				}
			}
			if (match)
			{
				return commandAttributes;
			}
		}
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
		return getCommand(key).getCategoryID();
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
		}
		return null; //Invalid nsme.
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
	 * @param categoryID
	 * @return CommandAttributes[] - Array of attributes.
	 */
	public static CommandAttributes[] getCommandsInCategory(Category categoryID)
	{
		ArrayList<CommandAttributes> categoryCommands;
		switch (categoryID)
		{
			default:
				return null;
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
		}
		if (categoryCommands != null)
		{
			return adminCommands.toArray(new CommandAttributes[register.size()]);
		}
		else
		{
			return generateCommandsInCategory(categoryID);
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
			if (cmdAttributes.getCategoryID() == categoryID)
			{
				categoryCommands.add(cmdAttributes);
			}
		}
		switch (categoryID)
		{
			default:
				return null;
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
		}
		return categoryCommands.toArray(new CommandAttributes[0]);
	}

	/**
	 * Opens the help page to for the specified command.
	 *
	 * @param msgEvent
	 * @param clazz
	 */
	public static void sendHelpInfo(GuildMessageReceivedEvent msgEvent, Class<? extends Command> clazz)
	{
		new Help().run(msgEvent, "/?", CommandRegister.getCommand(clazz).getCommandKey());
		/*
		 * So, technically this is fine, as help is *always* enabled and cannot be disabled. But generally calling commands like this is a bad idea, as they may be disabled.
		 * This also saves us having to copy command usage info for each command, which could be a problem as commands change.
		 */
	}
	
}
