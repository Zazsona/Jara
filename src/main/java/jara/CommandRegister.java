package jara;

import java.util.ArrayList;

import commands.About;
import commands.CoinFlip;
import commands.Command;
import commands.EightBall;
import commands.Jokes;
import commands.Ping;
import commands.Report;

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
		register.add(new CommandAttributes("Report", Report.class, new String[] {"Status", "Stats"}));
		register.add(new CommandAttributes("About", About.class, new String[] {"Credits", "Authors"}));
		register.add(new CommandAttributes("EightBall", EightBall.class, new String[] {"8ball", "helix", "fortune"}));
		register.add(new CommandAttributes("Jokes", Jokes.class, new String[] {"Joke", "Comedy"}));
		register.add(new CommandAttributes("CoinFlip", CoinFlip.class, new String[] {"FlipCoin", "Toss", "cf", "fc", "fiftyfifty", "flipacoin"}));
	}
	/**
	 * This method returns the command list of all programmed commands, with their classes and alias arrays.<br>
	 * @return
	 *CommandAttributes[] - An array of all programmed commands.
	 */
	public CommandAttributes[] getRegister()
	{
		return register.toArray(new CommandAttributes[register.size()]);
	}
	public String[] getAllCommandAliases()
	{
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
	public ArrayList<Class<? extends Command>> getAllCommandClasses()
	{
		ArrayList<Class<? extends Command>> classes = new ArrayList<Class<? extends Command>>();
		for (CommandAttributes commandAttributes : register)
		{
			classes.add(commandAttributes.getCommandClass());
		}
		return classes;
	}
	public String[] getAllCommandKeys()
	{
		ArrayList<String> keys = new ArrayList<String>();
		for (CommandAttributes commandAttributes : register)
		{
			keys.add(commandAttributes.getCommandKey());
		}
		return keys.toArray(new String[keys.size()]);
	}
	public CommandAttributes getCommand(String key)
	{
		for (CommandAttributes commandAttributes : register)
		{
			if (commandAttributes.getCommandKey().equalsIgnoreCase(key))
			{
				return commandAttributes;
			}
		}
		return null; //Bad key
	}
	public int getRegisterSize()
	{
		return register.size();
	}
	
}
