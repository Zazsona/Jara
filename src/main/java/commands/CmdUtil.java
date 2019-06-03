package commands;


import audio.Audio;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import jara.CommandRegister;
import jara.Core;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

/**
 * Don't remove items from this API, as it will break the modules that rely on them.
 */
public class CmdUtil
{
    /**
     * Gets JDA
     * @return
     */
    public static JDA getJDA()
    {
        return Core.getShardManager().getApplicationInfo().getJDA();
    }
    /**
     * A list of almost every english word
     */
    private static ArrayList<String> wordList;
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
                headers.forEach((name, value) -> httpGet.addHeader(name, value));
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
            e.printStackTrace();
            return "";
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Loads the entire word list into memory.
     * @return a list of almost every english word
     * @throws IOException
     */
    public static ArrayList<String> getWordList() throws IOException
    {
        try
        {
            if (wordList == null)
            {
                File wordFile = new File(CmdUtil.class.getResource("/wordList.txt").toURI());
                wordList = new ArrayList<>();
                Scanner scanner = new Scanner(wordFile);
                while (scanner.hasNext())
                {
                    String word = scanner.nextLine();
                    wordList.add(word);
                }
                scanner.close();
            }
            return wordList;
        }
        catch (URISyntaxException e)
        {
            throw new IOException(); //URI is hard coded, something else went wrong.
        }
    }

    /**
     * Gets a random word from the word list. If the word list is not loaded into memory, this will load it.
     * @return a random word of any length
     */
    public static String getRandomWord()
    {
        Random r = new Random();
        try
        {
            return getWordList().get(r.nextInt(wordList.size()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "";
        }

    }

    /**
     * Gets the audio instance for the guild defined by guildID
     * @param guildID the guild to get the audio instance for
     * @return The audio instance
     */
    public static Audio getGuildAudio(String guildID)
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

    /**
     * Gets audio track details in a pretty format
     * @param track the track to detail
     * @return the formatted details
     */
    public static String formatAudioTrackDetails(AudioTrack track)
    {
        AudioTrackInfo requestedTrackInfo = track.getInfo();
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("**"+requestedTrackInfo.title+"**\n");
        infoBuilder.append(requestedTrackInfo.author+"\n");
        infoBuilder.append(formatMillisecondsToHhMmSs(requestedTrackInfo.length));
        return infoBuilder.toString();
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
    public static ArrayList<String> getTopicList() throws IOException
    {
        try
        {
            if (topicList == null)
            {
                topicList = new ArrayList<>();
                File topicsFile = new File(CmdUtil.class.getResource("/game/topics.txt").toURI());
                Scanner scanner = new Scanner(topicsFile);
                while (scanner.hasNextLine())
                {
                    topicList.add(scanner.nextLine());
                }
                scanner.close();
            }
            return topicList;
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
            throw new IOException(); //URI is hardcoded, so something else has gone fucky here.
        }

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
        new Help().run(msgEvent, "/?", CommandRegister.getCommand(clazz).getCommandKey());
        /*
         * So, technically this is fine, as help is *always* enabled and cannot be disabled. But generally calling commands like this is a bad idea, as they may be disabled.
         * This also saves us having to copy command usage info for each command, which could be a problem as commands change.
         */
    }

    /**
     * The yearly seasons.
     */
    public static enum Season
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
