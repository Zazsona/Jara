package commands.toys.Quote;

import com.google.gson.Gson;
import commands.Command;
import commands.toys.Quote.Json.QuoteJson;
import commands.toys.Quote.Json.QuoteListJson;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Iterator;

public class DeleteQuote extends Command
{
    private QuoteListJson quoteListJson;
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        try
        {
            if (parameters.length > 2)
            {
                for (int i = 3; i<parameters.length; i++)
                {
                    parameters[2] += " "+parameters[i];
                }
            }
            deleteQuote(msgEvent.getGuild().getId(), parameters[2]);
            saveDeletion(msgEvent);
            msgEvent.getChannel().sendMessage("Quote deleted.").queue();
        }
        catch (IOException e)
        {
            msgEvent.getChannel().sendMessage("Error: Failed to delete quote. Does it exist?").queue();
        }

    }

    private boolean deleteQuote(String guildID, String quoteName) throws IOException
    {
        String JSON = new String(Files.readAllBytes(getQuoteFile(guildID).toPath()));
        Gson gson = new Gson();
        QuoteListJson quoteListJson = gson.fromJson(JSON, QuoteListJson.class);
        if (quoteListJson != null && quoteListJson.QuoteList.size() > 0)
        {
            for (QuoteJson quote : quoteListJson.QuoteList)
            {
                if (quote.name.equalsIgnoreCase(quoteName))
                {
                    quoteListJson.QuoteList.remove(quote);
                    return true;
                }
            }
        }
        throw new IOException("The quote does not exist.");
    }

    private void saveDeletion(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        Gson gson = new Gson();
        File quoteFile = getQuoteFile(msgEvent.getGuild().getId());

        PrintWriter printWriter = new PrintWriter(new FileOutputStream(quoteFile, false));
        printWriter.print(gson.toJson(quoteListJson));
        printWriter.close();
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
