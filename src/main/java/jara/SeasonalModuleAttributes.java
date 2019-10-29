package jara;

import com.google.gson.Gson;
import commands.Help;
import module.ModuleCommand;
import module.ModuleLoad;
import module.ModuleConfig;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class SeasonalModuleAttributes extends ModuleAttributes implements Serializable
{
    private int startingDayOfYear;
    private int endingDayOfYear;

    /**
     * Constructor from a JSON string
     * @param json valid json matching this class' attributes.
     */
    public SeasonalModuleAttributes(String json)
    {
        super(json);

        Gson gson = new Gson();
        SeasonalModuleAttributes sma = gson.fromJson(json, this.getClass());

        startingDayOfYear = sma.startingDayOfYear;
        endingDayOfYear = sma.endingDayOfYear;
    }

    /**
     * Constructor for set values
     * @param keyArg the module's unique key
     * @param descriptionArg the module's short description
     * @param aliasesArg the module's aliases
     * @param categoryArg the module's category
     * @param targetVersionArg the module's target Jara version
     * @param disableableArg the module's ability to be disabled (This should only be used for in-built commands)
     * @param startingDayoOfYear the day of the year the module is first active
     * @param endingDayOfYear the last day of the year the module is active
     */
    public SeasonalModuleAttributes(String keyArg, String descriptionArg, String[] aliasesArg, ModuleManager.Category categoryArg, String targetVersionArg, boolean disableableArg, int startingDayoOfYear, int endingDayOfYear)
    {
        super(keyArg, descriptionArg, aliasesArg, categoryArg, targetVersionArg, disableableArg);
        this.startingDayOfYear = startingDayoOfYear;
        this.endingDayOfYear = endingDayOfYear;
    }

    /**
     * Constructor for set values
     * @param keyArg the module's unique key
     * @param descriptionArg the module's short description
     * @param aliasesArg the module's aliases
     * @param categoryArg the module's category
     * @param targetVersionArg the module's target Jara version
     * @param disableableArg the module's ability to be disabled (This should only be used for in-built commands)
     * @param startingDayoOfYear the day of the year the module is first active
     * @param endingDayOfYear the last day of the year the module is active
     * @param commandClass the class to execute a command
     * @param helpPage the help information
     * @param moduleConfigClass the module's config
     * @param loadClass the load callback
     */
    public SeasonalModuleAttributes(String keyArg, String descriptionArg, String[] aliasesArg, ModuleManager.Category categoryArg, String targetVersionArg, boolean disableableArg, int startingDayoOfYear, int endingDayOfYear, Class<? extends ModuleCommand> commandClass, Help.HelpPage helpPage, Class<? extends ModuleConfig> moduleConfigClass, Class<? extends ModuleLoad> loadClass)
    {
        super(keyArg, descriptionArg, aliasesArg, categoryArg, targetVersionArg, disableableArg, commandClass, helpPage, moduleConfigClass, loadClass);
        this.startingDayOfYear = startingDayoOfYear;
        this.endingDayOfYear = endingDayOfYear;
    }

    /**
     * Checks if this module is active in the specified time
     * @param zdt the time to check
     * @return true/false on active
     */
    public boolean isActive(ZonedDateTime zdt)
    {
        int dayOfYear = zdt.getDayOfYear();
        return (dayOfYear >= startingDayOfYear && dayOfYear <= endingDayOfYear);
    }
}
