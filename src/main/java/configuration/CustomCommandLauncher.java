package configuration;

import jara.CommandAttributes;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;

public class CustomCommandLauncher extends CommandLauncher
{
    public CustomCommandLauncher(CommandAttributes attributes)
    {
        super(attributes, true); //Enabled state is not used here
    }

    @Override
    public void execute(GuildMessageReceivedEvent msgEvent, String...parameters)
    {
        GuildSettings guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
        if (guildSettings.isCommandEnabled(attributes.getCommandKey()))
        {
            if (guildSettings.isPermitted(msgEvent.getMember(), parameters[0].replace(SettingsUtil.getGuildCommandPrefix(msgEvent.getGuild().getId()).toString(), "").toLowerCase()))
            {
                Runnable commandRunnable = () -> {
                    try
                    {
                        attributes.getCommandClass().newInstance().run(msgEvent, parameters);
                    }
                    catch (InstantiationException | IllegalAccessException e)
                    {
                        msgEvent.getChannel().sendMessage("Sorry, I was unable to run the command.").queue();
                        Logger logger = LoggerFactory.getLogger(CommandLauncher.class);
                        logger.error("A custom command request was sent but could not be fulfilled.\nCommand: "+ Arrays.toString(parameters) +"\nGuild: "+msgEvent.getGuild().getId()+" ("+msgEvent.getGuild().getName()+")\nUser: "+msgEvent.getAuthor().getName()+"#"+msgEvent.getAuthor().getDiscriminator()+"Channel: "+msgEvent.getChannel().getId()+" ("+msgEvent.getChannel().getName()+")\nDate/Time: "+ LocalDateTime.now().toString()+"\n\nError: \n"+e.toString());
                    }
                };
                Thread commandThread = new Thread(commandRunnable);
                commandThread.setName(msgEvent.getGuild().getName()+"-"+attributes.getCommandKey()+"-Thread");
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
