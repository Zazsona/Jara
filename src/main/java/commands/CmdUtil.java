package commands;


import audio.Audio;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import configuration.SettingsUtil;
import jara.Core;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class CmdUtil
{
    /**
     * A list of almost every english word
     */
    private static ArrayList<String> wordList;
    /**
     * A list of radom topics
     */
    private static ArrayList<String> topicList;
    /**
     * A map of guildID to that guild's audio instance.
     */
    private static HashMap<String, Audio> guildAudios = new HashMap<>();
    /**
     * This method sends a HTTP request to the specified URL.
     * @param URL
     * @return
     * String - The data returned from the request
     * null - Error occured.
     */
    public static String sendHTTPRequest(String URL)
    {
        return sendHTTPRequestWithHeader(URL, null);
    }

    /**
     * This method sends a HTTP request to the specified URL, including the specified headers.<br>
     * The HashMap's key will be used as the header name, with the data as the header data.
     * @param URL
     * @param headers
     * @return
     * String - The data returned from the request
     * null - Error occured.
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
            return StringEscapeUtils.unescapeHtml4(pageData);
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
     * @return
     * @throws IOException
     */
    public static ArrayList<String> getWordList() throws IOException
    {
        File localWordsFile = new File(SettingsUtil.getDirectory()+"/wordList.txt");
        if (wordList == null)
        {
            wordList = new ArrayList<>();
            if (!localWordsFile.exists())
            {
                URLConnection cloudWordsFile = new URL("https://raw.githubusercontent.com/Zazsona/Jara/master/assets/wordsList.txt").openConnection();
                cloudWordsFile.connect();
                Scanner scanner = new Scanner(cloudWordsFile.getInputStream());

                PrintWriter printWriter = new PrintWriter(localWordsFile);
                while (scanner.hasNext())
                {
                    String word = scanner.nextLine();
                    wordList.add(word);
                    printWriter.println(word);
                }
                scanner.close();
                printWriter.close();
            }
            else
            {
                Scanner scanner = new Scanner(localWordsFile);
                while (scanner.hasNext())
                {
                    String word = scanner.nextLine();
                    wordList.add(word);
                }
                scanner.close();
            }
        }
        return wordList;
    }

    /**
     * Gets a random word from the word list. If the word list is not loaded into memory, this will load it.
     * @return String - The random word of any length
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
     * @return Audio - The audio instance
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
     * @return String - the formatted details
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
     * @param ms
     * @return
     * String - the time format.
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
     * @return
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

    public static String getRandomTopic() throws IOException
    {
        return getTopicList().get(new Random().nextInt(getTopicList().size()));
    }


}
