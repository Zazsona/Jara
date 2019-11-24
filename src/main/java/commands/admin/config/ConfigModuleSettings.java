package commands.admin.config;

import commands.CmdUtil;
import configuration.GuildSettings;
import jara.CommandHandler;
import jara.MessageManager;
import jara.ModuleAttributes;
import jara.ModuleManager;
import module.ModuleConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class ConfigModuleSettings
{
    private final GuildSettings guildSettings;
    private final TextChannel channel;
    private final ConfigMain configMain;
    private final MessageManager msgManager;

    private static final Logger logger = LoggerFactory.getLogger(ConfigModuleSettings.class);

    /**
     * Constructor
     * @param guildSettings the guild settings to modify
     * @param channel the channel to run in
     * @param configMain the config root
     */
    public ConfigModuleSettings(GuildSettings guildSettings, TextChannel channel, ConfigMain configMain)
    {
        this.guildSettings = guildSettings;
        this.channel = channel;
        this.configMain = configMain;
        this.msgManager = new MessageManager();
    }

    /**
     * Runs through the config using the navigation options supplied in a single message
     * @param msgEvent context
     * @param parameters the parameters to parse
     * @throws IOException unable to write to file
     */
    public void parseAsParameters(GuildMessageReceivedEvent msgEvent, String[] parameters) throws IOException
    {
        if (parameters.length > 2)
        {
            ModuleAttributes ma = ModuleManager.getModule(parameters[2]);
            if (ma != null)
            {
                loadConfig(msgEvent, ma, convertParametersToCollection(parameters), false);
            }
            else
            {
                EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
                embed.setDescription("Unrecognised module. Please try again.");
                channel.sendMessage(embed.build()).queue();
            }
        }
        else
        {
            getModule(msgEvent);
        }
    }

    /**
     * Converts the active working parameters (i.e, strips the parameters used to get to this menu) as a collection
     * @param parameters the parameters
     * @return parameters yet to parse, as a collection
     */
    private Collection<String> convertParametersToCollection(String[] parameters)
    {
        LinkedList<String> params = new LinkedList();
        params.addAll(Arrays.asList(parameters).subList(3, parameters.length));
        params = (params.size() > 0) ? params : null;
        return params;
    }

    /**
     * Prompts the user to select a module, and loads the module's config
     * @param msgEvent context
     * @throws IOException unable to save data
     */
    public void getModule(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        ModuleAttributes ma;
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        embed.setDescription("Please enter the module you would like to modify.");
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = msgManager.getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), configMain.getModuleAttributes().getKey())) //If the message is from someone with config permissions
            {
                if ((ma = ModuleManager.getModule(msg.getContentDisplay())) != null)
                {
                    loadConfig(msgEvent, ma, null, false);
                    break;
                }
                else if (msg.getContentDisplay().equalsIgnoreCase("quit") || msg.getContentDisplay().equalsIgnoreCase(guildSettings.getCommandPrefix()+"quit"))
                {
                    return;
                }
                else
                {
                    embed.setDescription("Unrecognised module. Please try again.");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }

    /**
     * Loads a module's config
     * @param msgEvent context
     * @param ma attributes of the module
     * @param parameters parameters to pass to module config
     * @param isSetup if this is the setup wizard
     * @throws IOException unable to access files
     */
    public void loadConfig(GuildMessageReceivedEvent msgEvent, ModuleAttributes ma, Collection<String> parameters, boolean isSetup) throws IOException
    {
        try
        {
            EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
            Class<? extends ModuleConfig> moduleConfigClass = ma.getConfigClass();
            if (moduleConfigClass != null)
            {
                if (parameters == null)
                    moduleConfigClass.newInstance().run(msgEvent, guildSettings, channel, isSetup);
                else
                    moduleConfigClass.newInstance().parseAsParameters(msgEvent, parameters, guildSettings, channel);
            }
            else
            {
                embed.setDescription("No config is available for module: "+ma.getKey()+".");
                channel.sendMessage(embed.build()).queue();
            }
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            msgEvent.getChannel().sendMessage("Sorry, I was unable to run the config for "+ma.getKey()+".").queue();
            Logger logger = LoggerFactory.getLogger(CommandHandler.class);
            logger.error("A config request was sent but could not be fulfilled.\nModule: "+ ma.getKey() +"\nGuild: "+msgEvent.getGuild().getId()+" ("+msgEvent.getGuild().getName()+")\nUser: "+msgEvent.getAuthor().getName()+"#"+msgEvent.getAuthor().getDiscriminator()+"Channel: "+msgEvent.getChannel().getId()+" ("+msgEvent.getChannel().getName()+")\nDate/Time: "+ LocalDateTime.now().toString()+"\n\nError: \n"+e.toString());
        }
        catch (NoSuchMethodError e)
        {
            logger.error("User attempted to load config for "+ma.getKey()+ ", but it is using an older API version, and is not supported.\n"+e.toString());
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            embedBuilder.setDescription("This module is outdated and cannot properly function.\nIt is recommended to disable it.");
            msgEvent.getChannel().sendMessage(embedBuilder.build()).queue();
        }
    }
}
