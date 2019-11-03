package module;

import commands.admin.config.ConfigMain;
import configuration.GuildSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.Collection;

/**
 * The class for interacting with Jara's Config command.
 */
public abstract class ModuleConfig extends ModuleClass
{
    /**
     * The entrance method for a config via the wizard.
     * @param msgEvent the context
     * @param guildSettings the guild's settings
     * @param channel the TextChannel to run in.
     * @param isSetup whether this method was called from the setup wizard
     * @throws IOException throw for errors writing to file
     */
    public abstract void run(GuildMessageReceivedEvent msgEvent, GuildSettings guildSettings, TextChannel channel, boolean isSetup) throws IOException;

    /**
     * The entrance method for a config, when being navigated through a single message.
     * @param msgEvent the context
     * @param parameters the parameters, not including those for previous menu navigation
     * @param guildSettings the guild's settings
     * @param channel the TextChannel to run in.
     * @throws IOException throw for errors writing to file
     */
    public abstract void parseAsParameters(GuildMessageReceivedEvent msgEvent, Collection<String> parameters, GuildSettings guildSettings, TextChannel channel) throws IOException;

    /**
     * Gets an embed matching the style of the config menus.
     * @param msgEvent context
     * @return the embed
     */
    protected EmbedBuilder getDefaultEmbedStyle(GuildMessageReceivedEvent msgEvent)
    {
        return ConfigMain.getEmbedStyle(msgEvent);
    }
}
