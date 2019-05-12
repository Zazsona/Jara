package commands.toys.Quote;

import com.google.gson.Gson;
import commands.CmdUtil;
import commands.Command;
import commands.toys.Quote.Json.QuoteJson;
import commands.toys.Quote.Json.QuoteListJson;
import jara.CommandRegister;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SaveQuote extends Command
{
    private String invocationKey;
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        invocationKey = parameters[0];
        if (parameters.length >= 3)
        {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            try
            {
                if (parameters.length > 3)
                {
                    for (int i = 3; i<parameters.length; i++)
                    {
                        parameters[2] += " "+parameters[i];
                    }
                }
                if (parameters[2].length() > 25)
                {
                    msgEvent.getChannel().sendMessage("That name is too long. Limit: 25 characters.").queue();
                    return;
                }
                Message message = null;

                if (parameters[1].length() == 18 && Pattern.matches("[0-9]*", parameters[1]))
                {
                    message = getMessageToQuoteByID(msgEvent.getChannel(), parameters[1]);
                }

                if (message == null) //By doing a null rather than if the parameter is a name, we can also check for users who have names like 123456789123456789
                {
                    message = getMessageToQuoteByUsername(msgEvent.getChannel(), parameters[1]);
                }

                if (message == null) //If we still couldn't find it as a username...
                {
                    embed.setDescription("Unable to find message. Please enter a valid user or message ID in the same channel as the message.");
                }
                else
                {
                    QuoteJson quote = saveQuote(msgEvent, message, parameters[2]);
                    embed.setDescription("Successfully saved quote "+quote.name+" by "+message.getMember().getEffectiveName()+"!");
                }
                msgEvent.getChannel().sendMessage(embed.build()).queue();
            }
            catch (IOException e)
            {
                embed.setDescription("Error: Unable to save quote.");
                msgEvent.getChannel().sendMessage(embed.build()).queue();
            }
        }
        else
        {
            CommandRegister.sendHelpInfo(msgEvent, getClass());
        }
    }

    private QuoteJson saveQuote(GuildMessageReceivedEvent msgEvent, Message message, String quoteName) throws IOException
    {
        Gson gson = new Gson();
        File quoteFile = GetQuoteFile(msgEvent.getGuild().getId());
        String json = new String(Files.readAllBytes(quoteFile.toPath()));
        QuoteListJson quoteListJson = gson.fromJson(json, QuoteListJson.class);

        if (quoteListJson == null)
        {
            quoteListJson = new QuoteListJson();
            quoteListJson.QuoteList = new ArrayList<QuoteJson>();
        }

        if (!doesQuoteNameExist(msgEvent, quoteListJson.QuoteList, quoteName))
        {
            QuoteJson quote = null;
            if (message.getAttachments().size() > 0)
            {
                quote = new QuoteJson(quoteName, message.getMember().getEffectiveName(), message.getContentDisplay(), message.getAttachments().get(0).getUrl(), message.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE));

            }
            else
            {
                quote = new QuoteJson(quoteName, message.getMember().getEffectiveName(), message.getContentDisplay(), message.getCreationTime().format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            quoteListJson.QuoteList.add(quote);

            PrintWriter printWriter = new PrintWriter(new FileOutputStream(quoteFile, false));
            printWriter.print(gson.toJson(quoteListJson));
            printWriter.close();
            return quote;
        }
        else
        {
            throw new IOException("Quote already exists.");
        }
    }

    private boolean doesQuoteNameExist(GuildMessageReceivedEvent msgEvent, ArrayList<QuoteJson> quotes, String name)
    {
        for (QuoteJson quote : quotes)
        {
            if (quote.name.equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }

    private Message getMessageToQuoteByID(TextChannel channel, String messageID)
    {
        return channel.getMessageById(messageID).complete();
    }

    private Message getMessageToQuoteByUsername(TextChannel channel, String name)
    {
        List<Member> members = channel.getGuild().getMembersByEffectiveName(name, true);
        List<Message> messages = channel.getHistory().retrievePast(20).complete();
        for (Message message : messages)
        {
            if (message.getContentDisplay().equalsIgnoreCase(invocationKey))
            {
                continue;
            }
            if (members.contains(message.getMember()))
            {
                return message;
            }
        }
        return null;
    }

    private File GetQuoteFile(String guildID) throws IOException
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
