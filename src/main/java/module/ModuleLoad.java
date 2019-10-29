package module;

/**
 * Callback class for once the module is loaded.
 */
public abstract class ModuleLoad extends ModuleClass
{
    /**
     * The method called when the module is loaded.
     */
    public abstract void load();
}
