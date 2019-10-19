package module;

import commands.admin.config.ConfigMain;
import configuration.GuildSettings;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.Collection;

public abstract class ModuleConfig
{
    /**
     * The entrance method for a config via the wizard.
     * @param msgEvent the context
     * @throws IOException throw for errors writing to file
     */
    public abstract void run(GuildMessageReceivedEvent msgEvent, GuildSettings guildSettings, TextChannel channel, boolean isSetup) throws IOException;

    /**
     * The entrance method for a config, when being navigated through a single message.
     * @param msgEvent the context
     * @param parameters the parameters, not including those for previous menu navigation
     * @throws IOException throw for errors writing to file
     */
    public abstract void parseAsParameters(GuildMessageReceivedEvent msgEvent, Collection<String> parameters, GuildSettings guildSettings, TextChannel channel) throws IOException;

    protected EmbedBuilder getDefaultEmbedStyle(GuildMessageReceivedEvent msgEvent)
    {
        return ConfigMain.getEmbedStyle(msgEvent);
    }
}
