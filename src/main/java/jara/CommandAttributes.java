package jara;

import commands.Command;

public class CommandAttributes 
{
	private final String commandKey;
	private final Class<? extends Command> commandClass;
	private final String[] aliases; //Text strings that will call the command
	private final CommandRegister.Category category;
	private final boolean disableable;
	private final String description;

	public CommandAttributes(String commandKeyArg, String descriptionArg, Class<? extends Command> commandClassArg, String[] aliasesArg, CommandRegister.Category categoryArg, boolean disableableArg)
	{
		commandKey = commandKeyArg;
		description = descriptionArg;
		commandClass = commandClassArg;
		aliases = new String[aliasesArg.length+1];
		aliases[0] = commandKey;
		System.arraycopy(aliasesArg, 0, aliases, 1, aliases.length - 1);
		category = categoryArg;
		disableable = disableableArg;

		//Sort aliases alphabetically
		for (int i = 1; i<getAliases().length; i++)
		{
			int index = i-1;
			String element = getAliases()[i];
			while ((index > -1) && (element.compareTo(getAliases()[index]) < 0))
			{
				getAliases()[index+1] = getAliases()[index];
				index--;
			}
			getAliases()[index+1] = element;
		}
	}

	/**
	 * Simple get for the command's key. This is unique to this command and can be used to identify it.
	 * @return
	 * String - The key
	 */
	public String getCommandKey()
	{
		return commandKey;
	}
	/**
	 * 
	 * Simple get for all the different text strings
	 * that will call the command. Sorted alphabetically.
	 * 
	 * @return
	 * String[] - List of all command aliases
	 */
	public String[] getAliases()
	{
		return aliases;
	}
	/**
	 * 
	 * Simple get method for the command's corresponding class.
	 * Do not use this method for instantiating a command as it
	 * does not perform an enabled check. 
	 * 
	 * Instead, use execute() in CommandConfiguration
	 * 
	 * @return
	 * Class<? extends Command> - The command class.
	 */
	public Class<? extends Command> getCommandClass()
	{
		return commandClass;
	}
	/**
	 * Returns the command's category.<br>
	 * @return
	 * Category - The category
	 */
	public CommandRegister.Category getCategory()
	{
		return category;
	}

	/**
	 * Simple get which returns the name of the command's category
	 * @return
	 * String - Category name
	 */
	public String getCategoryName()
	{
		return CommandRegister.getCategoryName(category);
	}
	/**
	 * Simple state check which specifies if this command is able to be disabled
	 * @return
	 * true - Can be disabled/enabled freely
	 * false - Locked. This command should not be disableable.
	 */
	public boolean isDisableable()
	{
		return disableable;
	}

	/**
	 * Returns a small description of the command, suitable for lists. Use /help for full instructions.
	 * @return
	 * String - the description.
	 */
	public String getDescription()
	{
		return description;
	}
}
