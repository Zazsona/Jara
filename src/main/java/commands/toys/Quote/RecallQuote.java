package commands.toys.Quote;

import com.google.gson.Gson;
import commands.CmdUtil;
import commands.Command;
import commands.toys.Quote.Json.QuoteJson;
import commands.toys.Quote.Json.QuoteListJson;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class RecallQuote extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        if (parameters.length > 2)
        {
            for (int i = 2; i<parameters.length; i++)
            {
                parameters[1] += " "+parameters[i];
            }
        }
        try
        {
            QuoteJson quote = getQuoteByName(msgEvent.getGuild().getId(), parameters[1]);
            EmbedBuilder formattedQuote = formatQuote(msgEvent.getGuild().getSelfMember(), quote);
            if (quote.attachmentUrl != null)
            {
                formattedQuote.setTitle(quote.name, quote.attachmentUrl);
                formattedQuote.setImage(quote.attachmentUrl);
            }
            msgEvent.getChannel().sendMessage(formattedQuote.build()).queue();
        }
        catch (IOException e)
        {
            EmbedBuilder failEmbed = new EmbedBuilder();
            failEmbed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            failEmbed.setDescription("Error: Unable to locate quote.");
            msgEvent.getChannel().sendMessage(failEmbed.build()).queue();
        }

    }

    private EmbedBuilder formatQuote(Member selfMember, QuoteJson quote)
    {
        EmbedBuilder quoteEmbed = new EmbedBuilder();
        quoteEmbed.setColor(CmdUtil.getHighlightColour(selfMember));
        quoteEmbed.setTitle("===== "+quote.name+" =====");
        quoteEmbed.setDescription(quote.message+"\n\n~"+quote.user);
        quoteEmbed.setFooter(quote.date, null);
        return quoteEmbed;
    }

    private QuoteJson getQuoteByName(String guildID, String quoteName) throws IOException
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
                    return quote;
                }
            }
        }
        throw new IOException("The quote could not be found.");
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
