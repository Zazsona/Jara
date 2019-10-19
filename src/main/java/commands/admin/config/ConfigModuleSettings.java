package commands.admin.config;

import commands.CmdUtil;
import configuration.GuildCommandLauncher;
import configuration.GuildSettings;
import jara.MessageManager;
import jara.ModuleAttributes;
import jara.ModuleRegister;
import module.ModuleConfig;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedList;

public class ConfigModuleSettings
{
    private final GuildSettings guildSettings;
    private final TextChannel channel;
    private final MessageManager msgManager;

    private static final Logger logger = LoggerFactory.getLogger(ConfigModuleSettings.class);

    public ConfigModuleSettings(GuildSettings guildSettings, TextChannel channel)
    {
        this.guildSettings = guildSettings;
        this.channel = channel;
        this.msgManager = new MessageManager();
    }

    public void parseAsParameters(GuildMessageReceivedEvent msgEvent, String[] parameters) throws IOException
    {
        if (parameters.length > 2)
        {
            ModuleAttributes ma = ModuleRegister.getModule(parameters[2]);
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

    private Collection<String> convertParametersToCollection(String[] parameters)
    {
        LinkedList<String> params = new LinkedList();
        for (int i = 3; i<parameters.length; i++)
        {
            params.add(parameters[i]);
        }
        params = (params.size() > 0) ? params : null;
        return params;
    }

    public void getModule(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        ModuleAttributes ma;
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        embed.setDescription("Please enter the module you would like to modify.");
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = msgManager.getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                if ((ma = ModuleRegister.getModule(msg.getContentDisplay())) != null)
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
            Logger logger = LoggerFactory.getLogger(GuildCommandLauncher.class);
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
