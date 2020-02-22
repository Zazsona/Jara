package listeners;

import jara.Core;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sun.security.krb5.Config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ListenerManager
{
    private static ConcurrentLinkedQueue<CommandListener> commandListeners = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<AudioListener> audioListeners = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<ConfigListener> configListeners = new ConcurrentLinkedQueue<>();

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
            if (listener instanceof AudioListener)
            {
                audioListeners.add((AudioListener) listener);
            }
            if (listener instanceof ConfigListener)
            {
                configListeners.add((ConfigListener) listener);
            }
        }
    }

    /**
     * Deregisters a JaraListener, so it no longer triggers
     * @param listener the listener to remove
     */
    public static synchronized void deregisterListener(JaraListener listener)
    {
        if (listener != null)
        {
            if (listener instanceof CommandListener)
            {
                commandListeners.remove(listener);
            }
            if (listener instanceof AudioListener)
            {
                audioListeners.remove(listener);
            }
            if (listener instanceof ConfigListener)
            {
                configListeners.remove(listener);
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

    /**
     * Gets registered {@link AudioListener}s
     * @return the listeners
     */
    public static ConcurrentLinkedQueue<AudioListener> getAudioListeners()
    {
        return audioListeners;
    }

    /**
     * Gets registered {@link ConfigListener}s
     * @return the listeners
     */
    public static ConcurrentLinkedQueue<ConfigListener> getConfigListeners()
    {
        return configListeners;
    }
}
