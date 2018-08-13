package jara;

import commands.Command;

public class CommandAttributes 
{
	private final String commandKey;
	private final Class<? extends Command> commandClass;
	private final String[] aliases; //Text strings that will call the command
	private final int category;
	private final boolean disableable;

	public CommandAttributes(String commandKeyArg, Class<? extends Command> commandClassArg, String[] aliasesArg, int categoryArg, boolean disableableArg)
	{
		commandKey = commandKeyArg;
		commandClass = commandClassArg;
		aliases = new String[aliasesArg.length+1];
		aliases[0] = commandKey;
		for (int i = 1; i<aliases.length; i++)
		{
			aliases[i] = aliasesArg[i-1];
		}
		category = categoryArg;
		disableable = disableableArg;
		
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
	 * that will call the command.
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
	 * Returns the class' category as its ID.<br>
	 * @return
	 * int - The id number
	 */
	public int getCategoryID()
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
}
