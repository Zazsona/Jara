package commands;


import audio.Audio;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import jara.ModuleRegister;
import jara.Core;
import module.Command;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Don't remove items from this API, as it will break the modules that rely on them.
 */
public class CmdUtil
{
    /**
     * Gets JDA.<br>
     *     Don't rely on this being a fast operation. It will not work until the bot is launched,
     *     and as such, will hold the thread until that happens.
     * @return
     */
    public synchronized static JDA getJDA()
    {
        try
        {
            return Core.getShardManager().getApplicationInfo().getJDA();
        }
        catch (NullPointerException e)
        {
            try
            {
                while (Core.getShardManager() == null)
                {
                    Thread.sleep(10);
                }
                while (!Core.getShardManager().getApplicationInfo().getJDA().getStatus().equals(JDA.Status.CONNECTED))
                {
                    Thread.sleep(10);
                }
                return Core.getShardManager().getApplicationInfo().getJDA();
            }
            catch (InterruptedException e1)
            {
                //Do nothing.
            }
        }
        return null;
    }
    /**
     * A list of almost every english word
     */
    private static ArrayList<String> wordList;

    /**
     * A list of top 10k english words.
     */
    private static ArrayList<String> topWordList;

    /**
     * A list of random topics
     */
    private static ArrayList<String> topicList;
    /**
     * A map of guildID to that guild's audio instance.
     */
    private static HashMap<String, Audio> guildAudios = new HashMap<>();
    /**
     * This method sends a HTTP request to the specified URL.
     * @param URL the URL to send a request to
     * @return The data returned from the request<br>
     * null - Error occurred.
     */
    public static String sendHTTPRequest(String URL)
    {
        return sendHTTPRequestWithHeader(URL, null);
    }

    /**
     * This method sends a HTTP request to the specified URL, including the specified headers.<br>
     * The HashMap's key will be used as the header name, with the data as the header data.
     * @param URL the URL to send a request to
     * @param headers the header data to include
     * @return The data returned from the request<br>
     * null - Error occurred.
     */
    public static String sendHTTPRequestWithHeader(String URL, HashMap<String, String> headers)
    {
        try
        {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(URL);
            if (headers != null)
            {
                headers.forEach(httpGet::addHeader);
            }
            ResponseHandler<String> responseHandler = response ->
            {
                int status = response.getStatusLine().getStatusCode();
                if (status == 200)
                {
                    HttpEntity httpEntity = response.getEntity();
                    if (httpEntity != null)
                    {
                        return EntityUtils.toString(httpEntity);
                    }
                    else
                    {
                        return null;
                    }
                }
                else
                {
                    throw new ClientProtocolException("HTML returned code: " + status + ".");
                }
            };
            String pageData = httpClient.execute(httpGet, responseHandler);
            httpClient.close();
            return pageData; //We could escape HTML encoding here, but that'd cause issues with Json and such. So don't.
        }
        catch (ClientProtocolException e)
        {
            return "";
        }
        catch (IOException e)
        {
            LoggerFactory.getLogger(CmdUtil.class).error(e.toString());
            return "";
        }
    }

    /**
     * Loads the entire word list into memory. Ideal for checking words.
     * @return a list of almost every english word
     * @throws IOException
     */
    public synchronized static ArrayList<String> getWordList() throws IOException
    {
        if (wordList == null)
        {
            wordList = new ArrayList<>();
            Scanner scanner = new Scanner(CmdUtil.class.getResourceAsStream("/wordList.txt"));
            while (scanner.hasNext())
            {
                String word = scanner.nextLine();
                wordList.add(word);
            }
            scanner.close();
        }
        return wordList;
    }

    /**
     * Loads the entire top word list into memory. Ideal for giving words.
     * @return a list of roughly the top 10k most used English words. (Excluding curses)
     * @throws IOException
     */
    public synchronized static ArrayList<String> getTopWordList() throws IOException
    {
        if (topWordList == null)
        {
            topWordList = new ArrayList<>();
            Scanner scanner = new Scanner(CmdUtil.class.getResourceAsStream("/wordList - Common.txt"));
            while (scanner.hasNext())
            {
                String word = scanner.nextLine();
                topWordList.add(word);
            }
            scanner.close();
        }
        return topWordList;
    }

    /**
     * Gets a random word from the word list. If the word list is not loaded into memory, this will load it.
     * @param topWords whether to only get words from the top 10k
     * @throws IOException
     * @return a random word of any length
     */
    public static String getRandomWord(boolean topWords) throws IOException
    {
        Random r = new Random();
        if (topWords)
        {
            return getTopWordList().get(r.nextInt(topWordList.size()));
        }
        else
        {
            return getWordList().get(r.nextInt(wordList.size()));
        }
    }

    /**
     * Gets the audio instance for the guild defined by guildID
     * @param guildID the guild to get the audio instance for
     * @return The audio instance
     */
    public synchronized static Audio getGuildAudio(String guildID)
    {
        try
        {
            if (guildAudios.containsKey(guildID))
            {
                return guildAudios.get(guildID);
            }
            else
            {
                guildAudios.put(guildID, new Audio(Core.getShardManager().getGuildById(guildID)));
                return guildAudios.get(guildID);
            }
        }
        catch (ConcurrentModificationException e)
        {
            try
            {
                Thread.sleep(200);
                getGuildAudio(guildID);
            }
            catch (InterruptedException e1)
            {
            }
        }
        return null;
    }

    /**
     * Removes the current save for a guild's audio, including history.
     * @param guildID
     * @return
     */
    public synchronized static Audio clearGuildAudio(String guildID)
    {
        try
        {
            return guildAudios.remove(guildID);
        }
        catch (ConcurrentModificationException e)
        {
            try
            {
                Thread.sleep(200);
                clearGuildAudio(guildID);
            }
            catch (InterruptedException e1)
            {
            }
        }
        return null;
    }

    /**
     * Gets audio track details in a pretty format
     * @param track the track to detail
     * @return the formatted details
     */
    public static String formatAudioTrackDetails(AudioTrack track)
    {
        AudioTrackInfo requestedTrackInfo = track.getInfo();
        String infoBuilder = "**" + requestedTrackInfo.title + "**\n" +
                requestedTrackInfo.author + "\n" +
                formatMillisecondsToHhMmSs(requestedTrackInfo.length);
        return infoBuilder;
    }

    /**
     * Converts milliseconds to HH:mm:ss. If there are no hours, it becomes mm:ss.
     * @param ms time to pretty in milliseconds
     * @return the time format.
     */
    public static String formatMillisecondsToHhMmSs(long ms)
    {
        long remainingTime = ms;
        long hours = remainingTime / (1000*60*60);
        remainingTime -= hours*(1000*60*60);
        long minutes = remainingTime / (1000*60);
        remainingTime -= minutes*(1000*60);
        long seconds = remainingTime / (1000);

        if (hours > 0)
        {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        else if (minutes > 0)
        {
            return String.format("%02d:%02d", minutes, seconds);
        }
        else
        {
            return String.format("00:%02d", seconds); //It'd look weird if it was just seconds.
        }
    }

    /**
     * Loads the entire topic list into memory
     * @return the topic list
     * @throws IOException
     */
    public synchronized static ArrayList<String> getTopicList() throws IOException
    {
        if (topicList == null)
        {
            topicList = new ArrayList<>();
            Scanner scanner = new Scanner(CmdUtil.class.getResourceAsStream("/game/topics.txt"));
            while (scanner.hasNextLine())
            {
                topicList.add(scanner.nextLine());
            }
            scanner.close();
        }
        return topicList;

    }

    /**
     * Gets a random topic based from the topic list.
     * @return a topic
     * @throws IOException
     */
    public static String getRandomTopic() throws IOException
    {
        return getTopicList().get(new Random().nextInt(getTopicList().size()));
    }

    /**
     * Gets the bot's themed colour. That is, Jara's default colour or, if available, the colour of its role.
     * @param selfMember Jara's member object
     * @return the colour to use
     */
    public static Color getHighlightColour(Member selfMember)
    {
        try
        {
            return selfMember.getRoles().get(0).getColor(); //Try to set it to the bot's primary role colour
        }
        catch (IndexOutOfBoundsException | NullPointerException e)	//If the bot has no role
        {
            return Color.decode("#5967cf"); //Use a default theme.
        }
    }

    /**
     * Opens the help page to for the specified command.
     *
     * @param msgEvent
     * @param clazz
     */
    public static void sendHelpInfo(GuildMessageReceivedEvent msgEvent, Class<? extends Command> clazz)
    {
        new Help().run(msgEvent, "/?", ModuleRegister.getModule(clazz).getKey());
        /*
         * So, technically this is fine, as help is *always* enabled and cannot be disabled. But generally calling commands like this is a bad idea, as they may be disabled.
         * This also saves us having to copy command usage info for each command, which could be a problem as commands change.
         */
    }

    /**
     * The yearly seasons.
     */
    public enum Season
    {
        SPRING,
        SUMMER,
        AUTUMN,
        WINTER
    }

    /**
     * Gets the current season.
     * @return the {@link Season}
     */
    public static Season getSeason()
    {
        OffsetDateTime utc = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime spring = OffsetDateTime.of(LocalDateTime.of(LocalDate.now().getYear(), 3, 20, 0, 0), ZoneOffset.UTC);
        OffsetDateTime summer = OffsetDateTime.of(LocalDateTime.of(LocalDate.now().getYear(), 6, 20, 0, 0), ZoneOffset.UTC);
        OffsetDateTime autumn = OffsetDateTime.of(LocalDateTime.of(LocalDate.now().getYear(), 9, 22, 0, 0), ZoneOffset.UTC);
        OffsetDateTime winter = OffsetDateTime.of(LocalDateTime.of(LocalDate.now().getYear(), 12, 21, 0, 0), ZoneOffset.UTC);
        if (utc.isAfter(spring) && utc.isBefore(summer))
        {
            return Season.SPRING;
        }
        else if (utc.isAfter(summer) && utc.isBefore(autumn))
        {
            return Season.SUMMER;
        }
        else if (utc.isAfter(autumn) && utc.isBefore(winter))
        {
            return Season.AUTUMN;
        }
        else
        {
            return Season.WINTER;
        }
    }


}
