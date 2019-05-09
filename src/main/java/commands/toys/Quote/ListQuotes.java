package commands.toys.Quote;

import com.google.gson.Gson;
import commands.CmdUtil;
import commands.Command;
import commands.toys.Quote.Json.QuoteJson;
import commands.toys.Quote.Json.QuoteListJson;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ListQuotes extends Command
{

    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        try
        {
            Gson gson = new Gson();
            QuoteListJson quoteListJson = gson.fromJson(new String(Files.readAllBytes(getQuoteFile(msgEvent.getGuild().getId()).toPath())), QuoteListJson.class);
            if (quoteListJson != null && quoteListJson.QuoteList.size() > 0)
            {
                StringBuilder listBuilder = new StringBuilder();
                for (QuoteJson quote : quoteListJson.QuoteList)
                {
                    listBuilder.append(quote.name+" - "+quote.date);
                }
                embed.setDescription(listBuilder.toString());
            }
            else
            {
                embed.setDescription("You have no quotes!");
            }

        }
        catch (IOException e)
        {
            embed.setDescription("Error: Unable to access quotes.");
        }
        msgEvent.getChannel().sendMessage(embed.build()).queue();


    }

    private File getQuoteFile(String guildID) throws IOException
    {
        File quoteFile;
        String operatingSystem = System.getProperty("os.name").toLowerCase();
        if (operatingSystem.startsWith("windows"))
        {
            quoteFile = new File(System.getProperty("user.home")+"\\AppData\\Roaming\\Jara\\Quotes\\"+guildID+".json");
        }
        else
        {
            quoteFile = new File(System.getProperty("user.home")+"/.Jara/Quotes/"+guildID+".json");
        }
        if (!quoteFile.exists())
        {
            quoteFile.getParentFile().mkdirs();
            quoteFile.createNewFile();
        }
        return quoteFile;
    }
}
