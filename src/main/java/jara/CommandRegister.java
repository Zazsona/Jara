package jara;

import java.util.ArrayList;

import commands.Command;
import commands.Ping;

public class CommandRegister
{
	ArrayList<CommandAttributes> register;
	/**
	 * When implementing a new command, is is essential to add it to this method. Otherwise, it will be ignored at run time.
	 */
	public CommandRegister()
	{
		register = new ArrayList<CommandAttributes>();
		/*============================================
		 * 
		 * The layout for adding a new class should be quite simple.
		 * Simply create a new CommandAttributes class in the list, and pass the Command Key, Command Class, and then any aliases.
		 * All other operations (Adding them to settings, indexing them at boot, etc.) will be done automatically.
		 * 
		 * ===========================================
		 */
		register.add(new CommandAttributes("Ping", Ping.class, new String[] {"Pong", "Test"}));
	}
	
	
	/**
	 * This method returns the command list of all programmed commands, with their classes and alias arrays.<br>
	 * @return
	 *CommandAttributes[] - An array of all programmed commands.
	 */
	public CommandAttributes[] getRegister()
	{
		return (CommandAttributes[]) register.toArray();
	}
	public String[] getAllCommandAliases()
	{
		
	}
	public Class<? extends Command> getAllCommandClasses()
	{
		
	}
	public String[] getAllCommandKeys()
	{
		
	}
	
}
