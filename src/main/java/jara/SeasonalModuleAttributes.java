package jara;

import com.google.gson.Gson;
import commands.Help;
import module.Command;
import module.Load;
import module.ModuleConfig;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class SeasonalModuleAttributes extends ModuleAttributes implements Serializable
{
    private int startingDayOfYear;
    private int endingDayOfYear;

    public SeasonalModuleAttributes(String json)
    {
        super(json);

        Gson gson = new Gson();
        SeasonalModuleAttributes sma = gson.fromJson(json, this.getClass());

        startingDayOfYear = sma.startingDayOfYear;
        endingDayOfYear = sma.endingDayOfYear;
    }

    public SeasonalModuleAttributes(String keyArg, String descriptionArg, String[] aliasesArg, ModuleRegister.Category categoryArg, String targetVersionArg, boolean disableableArg, int startingDayoOfYear, int endingDayOfYear)
    {
        super(keyArg, descriptionArg, aliasesArg, categoryArg, targetVersionArg, disableableArg);
        this.startingDayOfYear = startingDayoOfYear;
        this.endingDayOfYear = endingDayOfYear;
    }

    public SeasonalModuleAttributes(String keyArg, String descriptionArg, String[] aliasesArg, ModuleRegister.Category categoryArg, String targetVersionArg, boolean disableableArg, int startingDayoOfYear, int endingDayOfYear, Class<? extends Command> commandClass, Help.HelpPage helpPage, Class<? extends ModuleConfig> moduleConfigClass, Class<? extends Load> loadClass)
    {
        super(keyArg, descriptionArg, aliasesArg, categoryArg, targetVersionArg, disableableArg, commandClass, helpPage, moduleConfigClass, loadClass);
        this.startingDayOfYear = startingDayoOfYear;
        this.endingDayOfYear = endingDayOfYear;
    }

    public boolean isActive(ZonedDateTime zdt)
    {
        int dayOfYear = zdt.getDayOfYear();
        return (dayOfYear >= startingDayOfYear && dayOfYear <= endingDayOfYear);
    }
}
