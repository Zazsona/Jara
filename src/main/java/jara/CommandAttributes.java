package jara;

import commands.Command;

public class CommandAttributes 
{
	private String commandKey;
	private Class<? extends Command> commandClass;
	private String[] aliases; //Text strings that will call the command

	public CommandAttributes(String commandKeyArg, Class<? extends Command> commandClassArg, String[] aliasesArg)
	{
		commandKey = commandKeyArg;
		commandClass = commandClassArg;
		aliases = aliasesArg.clone();
	}
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
}
