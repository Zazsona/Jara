package listeners;

import jara.Core;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ListenerManager
{
    private static ConcurrentLinkedQueue<CommandListener> commandListeners = new ConcurrentLinkedQueue<>();

    /**
     * Registers a new JaraListener, so it will handle events
     * @param listener the listener to register
     */
    public static synchronized void registerListener(JaraListener listener)
    {
        if (listener != null)
        {
            if (listener instanceof CommandListener)
            {
                commandListeners.add((CommandListener) listener);
            }
        }
    }

    /**
     * Deregisters a JaraListener, so it no longer triggers
     * @param listener the listener to register
     */
    public static synchronized void deregisterListener(JaraListener listener)
    {
        if (listener != null)
        {
            if (listener instanceof CommandListener)
            {
                commandListeners.remove(listener);
            }
        }
    }

    /**
     * Convenience method for {@link net.dv8tion.jda.api.JDA#addEventListener(Object...) }
     * @param listener the JDA listener
     */
    public static void registerListener(ListenerAdapter listener)
    {
        Core.getShardManagerNotNull().addEventListener(listener);
    }

    /**
     * Convenience method for {@link net.dv8tion.jda.api.JDA#removeEventListener(Object...) }
     * @param listener the JDA listener
     */
    public static void deregisterListener(ListenerAdapter listener)
    {
        Core.getShardManagerNotNull().removeEventListener(listener);
    }

    /**
     * Gets registered {@link CommandListener}s
     * @return the listeners
     */
    public static ConcurrentLinkedQueue<CommandListener> getCommandListeners()
    {
        return commandListeners;
    }
}
