package jara;

import com.google.gson.Gson;
import commands.Help;
import module.Command;
import module.Load;
import module.ModuleConfig;

public class ModuleAttributes
{
	private final String key;
	private final String[] aliases; //Text strings that will call the command
	private final ModuleRegister.Category category;
	private final boolean disableable;
	private final String targetVersion;
	private final String description;
	private Class<? extends Command> commandClass;
	private Help.HelpPage helpPage;
	private Class<? extends ModuleConfig> configClass;
	private Class<? extends Load> loadClass;

	public ModuleAttributes(String json)
	{
		Gson gson = new Gson();
		ModuleAttributes ma = gson.fromJson(json, this.getClass());

		key = ma.key;
		description = ma.description;
		aliases = new String[ma.aliases.length+1];
		aliases[0] = key;
		System.arraycopy(ma.aliases, 0, aliases, 1, aliases.length - 1);
		category = ma.category;
		disableable = true;
		targetVersion = ma.targetVersion;

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

		this.commandClass = null;
		setHelpPage(null);
		this.configClass = null;
		this.loadClass = null;
	}

	public ModuleAttributes(String keyArg, String descriptionArg, String[] aliasesArg, ModuleRegister.Category categoryArg, String targetVersionArg, boolean disableableArg)
	{
		key = keyArg;
		description = descriptionArg;
		aliases = new String[aliasesArg.length+1];
		aliases[0] = key;
		if (aliasesArg.length > 0)
		{
			System.arraycopy(aliasesArg, 0, aliases, 1, aliases.length - 1);
		}
		category = categoryArg;
		disableable = disableableArg;
		targetVersion = targetVersionArg;

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

		this.commandClass = null;
		setHelpPage(null);
		this.configClass = null;
		this.loadClass = null;
	}

	public ModuleAttributes(String keyArg, String descriptionArg, String[] aliasesArg, ModuleRegister.Category categoryArg, String targetVersionArg, boolean disableableArg, Class<? extends Command> commandClass, Help.HelpPage helpPage, Class<? extends ModuleConfig> moduleConfigClass, Class<? extends Load> loadClass)
	{
		key = keyArg;
		description = descriptionArg;
		aliases = new String[aliasesArg.length+1];
		aliases[0] = key;
		System.arraycopy(aliasesArg, 0, aliases, 1, aliases.length - 1);
		category = categoryArg;
		disableable = disableableArg;
		targetVersion = targetVersionArg;

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

		this.commandClass = commandClass;
		setHelpPage(helpPage);
		this.configClass = moduleConfigClass;
		this.loadClass = loadClass;
	}

	/**
	 * Simple get for the module's key. This is unique to this module and can be used to identify it.
	 * @return
	 * String - The key
	 */
	public String getKey()
	{
		return key;
	}
	/**
	 * 
	 * Simple get for all the different text strings
	 * that will call the module. Sorted alphabetically.
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
	 * Simple get method for the module's corresponding command class.
	 * Instantiating a command from this method does not perform any config checks.
	 * 
	 * Instead, use execute() in {@link configuration.CommandLauncher}
	 * 
	 * @return
	 * Class<? extends Command> - The command class.
	 * null - No command associated with this module
	 */
	public Class<? extends Command> getCommandClass()
	{
		return commandClass;
	}

	/**
	 * Gets the config class for this module
	 * @return
	 * Class extending {@link ModuleConfig} - The config class
	 * null - No config associated with this module
	 */
	public Class<? extends ModuleConfig> getConfigClass()
	{
		return configClass;
	}

	/**
	 * Gets the load class for this module
	 * @return
	 * Class extending {@link Load} - The load class
	 * null - No load function with this module
	 */
	public Class<? extends Load> getLoadClass()
	{
		return loadClass;
	}

	/**
	 * Gets the help page for this module
	 * @return
	 * {@link commands.Help.HelpPage} - the HelpPage
	 */
	public Help.HelpPage getHelpPage()
	{
		return helpPage;
	}

	/**
	 * Returns the module's category.<br>
	 * @return
	 * Category - The category
	 */
	public ModuleRegister.Category getCategory()
	{
		return category;
	}

	/**
	 * Simple get which returns the name of the module's category
	 * @return
	 * String - Category name
	 */
	public String getCategoryName()
	{
		return ModuleRegister.getCategoryName(category);
	}
	/**
	 * Simple state check which specifies if this module is able to be disabled
	 * @return
	 * true - Can be disabled/enabled freely
	 * false - Locked. This command should not be disableable.
	 */
	public boolean isDisableable()
	{
		return disableable;
	}

	/**
	 * Returns a small description of the module, suitable for lists.
	 * @return
	 * String - the description.
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Returns the Jara version this module was built for.
	 * @return the target version
	 */
	public String getTargetVersion()
	{
		return this.targetVersion;
	}

	/**
	 * Sets the Help Page to that specified. For the default help page, pass null.
	 * @param helpPage
	 * @return
	 * The new {@link commands.Help.HelpPage}
	 */
	public Help.HelpPage setHelpPage(Help.HelpPage helpPage)
	{
		if (helpPage != null)
		{
			this.helpPage = helpPage;
		}
		else if (this.helpPage == null)
		{
			this.helpPage = new Help.HelpPage();
		}
		return this.helpPage;
	}

	/**
	 * Sets the command class.
	 * This shouldn't be needed after module loading.
	 * @param clazz
	 */
	public void setCommandClass(Class<? extends Command> clazz)
	{
		this.commandClass = clazz;
	}

	/**
	 * Sets the config class.
	 * This shouldn't be needed after module loading.
	 * @param clazz
	 */
	public void setConfigClass(Class<? extends ModuleConfig> clazz)
	{
		this.configClass = clazz;
	}

	/**
	 * Sets the load class.
	 * This shouldn't be needed after module loading.
	 * @param clazz
	 */
	public void setLoadClass(Class<? extends Load> clazz)
	{
		this.loadClass = clazz;
	}
}
