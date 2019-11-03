package configuration;

import jara.ModuleAttributes;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;

public class GuildCustomCommandLauncher extends GuildCommandLauncher
{
    /**
     * Constructor
     * @param attributes the attributes of the custom command this launches
     */
    public GuildCustomCommandLauncher(ModuleAttributes attributes)
    {
        super(attributes); //Enabled state is not used here
    }

    @Override
    public void execute(GuildMessageReceivedEvent msgEvent, String...parameters)
    {
        GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
        if (guildSettings.isCommandEnabled(attributes.getKey()))
        {
            if (guildSettings.isPermitted(msgEvent.getMember(), attributes.getKey()))
            {
                Runnable commandRunnable = () -> {
                    try
                    {
                        attributes.getCommandClass().newInstance().run(msgEvent, parameters);
                    }
                    catch (InstantiationException | IllegalAccessException e)
                    {
                        msgEvent.getChannel().sendMessage("Sorry, I was unable to run the command.").queue();
                        Logger logger = LoggerFactory.getLogger(GuildCommandLauncher.class);
                        logger.error("A custom command request was sent but could not be fulfilled.\nCommand: "+ Arrays.toString(parameters) +"\nGuild: "+msgEvent.getGuild().getId()+" ("+msgEvent.getGuild().getName()+")\nUser: "+msgEvent.getAuthor().getName()+"#"+msgEvent.getAuthor().getDiscriminator()+"Channel: "+msgEvent.getChannel().getId()+" ("+msgEvent.getChannel().getName()+")\nDate/Time: "+ LocalDateTime.now().toString()+"\n\nError: \n"+e.toString());
                    }
                };
                Thread commandThread = new Thread(commandRunnable);
                commandThread.setName(msgEvent.getGuild().getName()+"-"+attributes.getKey()+"-Thread");
                commandThread.start();
                return;
            }
            else
            {
                msgEvent.getChannel().sendMessage("You do not have permission to use this command.").queue();
            }
        }
        else
        {
            msgEvent.getChannel().sendMessage("This command is disabled.").queue();
        }
    }
}
