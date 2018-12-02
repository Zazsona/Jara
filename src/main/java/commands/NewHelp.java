package commands;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;

public class NewHelp extends Command
{
    public class HelpPage
    {
        String[] params = new String[0];
        String description = "No information has been provided for this command.";
    }

    private static HashMap<String, HelpPage> pageMap = new HashMap<>();

    public static void addPage(String key, HelpPage hp)
    {
        if (!pageMap.containsKey(key))
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("**Parameters**");
            for (String param : hp.params)
            {
                stringBuilder.append(param).append("\n");
            }
            stringBuilder.append("**Description\n**");
            stringBuilder.append(hp.description);

            pageMap.put(key, hp);
        }
        else
        {
            throw new IllegalArgumentException("That key has already been set.");
        }
    }

    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {

    }
}
