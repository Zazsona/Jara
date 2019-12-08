package jara;

import com.google.gson.Gson;
import commands.Help;
import module.ModuleCommand;
import module.ModuleLoad;
import module.ModuleConfig;

public class ModuleAttributes
{
	private final String key;
	private final String[] aliases;
	private final ModuleManager.Category category;
	private final boolean disableable;
	private final String targetVersion;
	private final String description;
	private Class<? extends ModuleCommand> commandClass;
	private Help.HelpPage helpPage;
	private Class<? extends ModuleConfig> configClass;
	private Class<? extends ModuleLoad> loadClass;
	private boolean isCustomCommand;

	/**
	 * Constructor from a JSON string
	 * @param json valid json matching this class' attributes.
	 */
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
		isCustomCommand = false;
		targetVersion = ma.targetVersion;

		//Sort aliases alphabetically
		for (int i = 1; i<getAliases().length; i++)
		{
			int index = i-1;
			String element = getAliases()[i];
			while ((index > -1) && (element.compareToIgnoreCase(getAliases()[index]) < 0))
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

	/**
	 * Constructor for set values
	 * @param keyArg the module's unique key
	 * @param descriptionArg the module's short description
	 * @param aliasesArg the module's aliases
	 * @param categoryArg the module's category
	 * @param targetVersionArg the module's target Jara version
	 * @param disableableArg the module's ability to be disabled (This should only be used for in-built commands)
	 * @param customCommandArg defines whether this command is a custom command, rather than a global module.
	 */
	public ModuleAttributes(String keyArg, String descriptionArg, String[] aliasesArg, ModuleManager.Category categoryArg, String targetVersionArg, boolean disableableArg, boolean customCommandArg)
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
		isCustomCommand = isCustomCommand;

		//Sort aliases alphabetically
		for (int i = 1; i<getAliases().length; i++)
		{
			int index = i-1;
			String element = getAliases()[i];
			while ((index > -1) && (element.compareToIgnoreCase(getAliases()[index]) < 0))
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

	/**
	 * Constructor for set values
	 * @param keyArg the module's unique key
	 * @param descriptionArg the module's short description
	 * @param aliasesArg the module's aliases
	 * @param categoryArg the module's category
	 * @param targetVersionArg the module's target Jara version
	 * @param disableableArg the module's ability to be disabled (This should only be used for in-built commands)
	 * @param customCommandArg defines whether this command is a custom command, rather than a global module.
	 * @param commandClass the class to execute a command
	 * @param helpPage the help information
	 * @param moduleConfigClass the module's config
	 * @param loadClass the load callback
	 *
	 */
	public ModuleAttributes(String keyArg, String descriptionArg, String[] aliasesArg, ModuleManager.Category categoryArg, String targetVersionArg, boolean disableableArg, boolean customCommandArg, Class<? extends ModuleCommand> commandClass, Help.HelpPage helpPage, Class<? extends ModuleConfig> moduleConfigClass, Class<? extends ModuleLoad> loadClass)
	{
		key = keyArg;
		description = descriptionArg;
		aliases = new String[aliasesArg.length+1];
		aliases[0] = key;
		System.arraycopy(aliasesArg, 0, aliases, 1, aliases.length - 1);
		category = categoryArg;
		disableable = disableableArg;
		isCustomCommand = customCommandArg;
		targetVersion = targetVersionArg;

		//Sort aliases alphabetically
		for (int i = 1; i<getAliases().length; i++)
		{
			int index = i-1;
			String element = getAliases()[i];
			while ((index > -1) && (element.compareToIgnoreCase(getAliases()[index]) < 0))
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
	 * This geta a unique key for this module which can be used to identify it.
	 * @return the key
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * Alphabetical array of alternate strings that can be used to summon the module, including the key
	 * @return the aliases
	 */
	public String[] getAliases()
	{
		return aliases;
	}

	/**
	 * Gets the class responsible for the module's command.<br>
	 *     Instantiating this class will ignore any permissions settings in the guild.
	 * 
	 * @return the command class, or null if the module has no command.
	 */
	public Class<? extends ModuleCommand> getCommandClass()
	{
		return commandClass;
	}

	/**
	 * Gets the config class for this module
	 * @return the config class, or null if none exists.
	 */
	public Class<? extends ModuleConfig> getConfigClass()
	{
		return configClass;
	}

	/**
	 * Gets the class which specifies the callback method to run when the module is loaded.
	 * @return the class, or null if none exists.
	 */
	public Class<? extends ModuleLoad> getLoadClass()
	{
		return loadClass;
	}

	/**
	 * Gets the {@link commands.Help.HelpPage} for this module
	 * @return the help page
	 */
	public Help.HelpPage getHelpPage()
	{
		return helpPage;
	}

	/**
	 * Returns the module's {@link ModuleManager.Category}.<br>
	 * @return the category
	 */
	public ModuleManager.Category getCategory()
	{
		return category;
	}

	/**
	 * Simple get which returns the name of the module's {@link ModuleManager.Category}
	 * @return the name of the category
	 */
	public String getCategoryName()
	{
		return ModuleManager.getCategoryName(category);
	}
	/**
	 * Gets if this module can be disabled per guild.
	 * @return true/false for disable
	 */
	public boolean isDisableable()
	{
		return disableable;
	}

	/**
	 * Returns a small description of the module, suitable for lists.
	 * @return the description
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
	 * Sets the {@link commands.Help.HelpPage} to that specified. For the default help page, pass null.
	 * @param helpPage the help page to set
	 * @return the new help page
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
	 * Unless you're trying to pull some hack job, you probably shouldn't touch this.
	 * @param clazz the class to set
	 */
	public void setCommandClass(Class<? extends ModuleCommand> clazz)
	{
		this.commandClass = clazz;
	}

	/**
	 * Sets the config class.
	 * Unless you're trying to pull some hack job, you probably shouldn't touch this.
	 * @param clazz the class to set
	 */
	public void setConfigClass(Class<? extends ModuleConfig> clazz)
	{
		this.configClass = clazz;
	}

	/**
	 * Sets the load class.
	 * Unless you're trying to pull some hack job, you probably shouldn't touch this.
	 * @param clazz the class to set
	 */
	public void setLoadClass(Class<? extends ModuleLoad> clazz)
	{
		this.loadClass = clazz;
	}

	/**
	 * Gets if this module is in fact a guild-specific custom command.
	 * @return true on custom command, false on module
	 */
	public boolean isCustomCommand()
	{
		return isCustomCommand;
	}
}
