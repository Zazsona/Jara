package jara;

import commands.Command;

public class CommandConfiguration
{
	private boolean enabledState; //Whether the config allows this command to be used
	private String[] aliases; //Text strings that will call the command
	private Class<? extends Command> commandClass;
	
	public CommandConfiguration(boolean enabledStateArg, String[] aliasesArg, Class<? extends Command> commandClassArg)
	{
		enabledState = enabledStateArg;
		aliases = aliasesArg;
		commandClass = commandClassArg;
	}
	
	public Class<? extends Command> getCommandClass()
	{
		return commandClass;
	}
	
	public String[] getAliases()
	{
		return aliases;
	}
	
	public boolean isEnabled()
	{
		return enabledState;
	}
}
