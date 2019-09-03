package commands.admin.config;

import commands.CmdUtil;
import configuration.CommandLauncher;
import module.ModuleConfig;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;

public class ConfigModuleSettings
{
    private static final Logger logger = LoggerFactory.getLogger(ConfigModuleSettings.class);
    public static HashMap<String, Class<? extends ModuleConfig>> configClassMap = new HashMap<>();

    public void loadConfig(GuildMessageReceivedEvent msgEvent, String[] parameters, String command) throws IOException
    {
        try
        {
            Class<? extends ModuleConfig> moduleConfigClass = configClassMap.get(command);
            if (moduleConfigClass != null)
            {
                moduleConfigClass.newInstance().run(msgEvent, parameters);
            }
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            msgEvent.getChannel().sendMessage("Sorry, I was unable to run the config.").queue();
            Logger logger = LoggerFactory.getLogger(CommandLauncher.class);
            logger.error("A config request was sent but could not be fulfilled.\nCommand: "+ Arrays.toString(parameters) +"\nGuild: "+msgEvent.getGuild().getId()+" ("+msgEvent.getGuild().getName()+")\nUser: "+msgEvent.getAuthor().getName()+"#"+msgEvent.getAuthor().getDiscriminator()+"Channel: "+msgEvent.getChannel().getId()+" ("+msgEvent.getChannel().getName()+")\nDate/Time: "+ LocalDateTime.now().toString()+"\n\nError: \n"+e.toString());
        }
        catch (NoSuchMethodError e)
        {
            logger.error("User attempted to load config for"+command+ ", but it is using an older API version, and is not supported.\n"+e.toString());
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            embedBuilder.setDescription("This module is outdated and cannot properly function.\nIt is recommended to disable this command.");
            msgEvent.getChannel().sendMessage(embedBuilder.build()).queue();
        }
    }

    public void addConfig(String command, Class<? extends ModuleConfig> clazz)
    {
        configClassMap.put(command, clazz);
    }
}
