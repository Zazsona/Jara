package module;

import jara.ModuleAttributes;
import jara.ModuleManager;

public class ModuleClass
{
    private ModuleAttributes moduleAttributes;
    /**
     * Gets the {@link ModuleAttributes} for this module.
     * @return the module attributes
     */
    public ModuleAttributes getModuleAttributes()
    {
        if (moduleAttributes == null)
            moduleAttributes = ModuleManager.getModule(getClass());
        
        return moduleAttributes;
    }

}
