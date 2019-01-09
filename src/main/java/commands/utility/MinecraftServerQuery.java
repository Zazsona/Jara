package commands.utility;

import com.google.gson.Gson;
import commands.Command;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Makes a direct query to a Minecraft server, acting as a 1.13.2 client.
 */
public class MinecraftServerQuery extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        try
        {
            Gson gson = new Gson();
            String[] connectionDetails = validateParameters(msgEvent.getChannel(), parameters);
            if (connectionDetails != null)
            {
                QueryResult queryResult = gson.fromJson(getDetails(connectionDetails[0], Integer.parseInt(connectionDetails[1])), QueryResult.class);
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
                embedBuilder.setTitle("==== MC Server Status ====");
                embedBuilder.setDescription(buildOutput(connectionDetails[0], queryResult));
                msgEvent.getChannel().sendMessage(embedBuilder.build()).queue();
            }
        }
        catch (UnknownHostException e)
        {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            embedBuilder.setTitle("==== MC Server Status ====");
            embedBuilder.setDescription("**Server**: "+parameters[1]+"\n**Status:** Unavailable");
            msgEvent.getChannel().sendMessage(embedBuilder.build()).queue();
        }


    }

    /**
     * Ensure the passed connection details are in the correct format
     * @param channel the channel to send error messages to
     * @param parameters the parameters
     * @return an array with IP at index 0, and port at index 1
     */
    private String[] validateParameters(TextChannel channel, String... parameters)
    {
        if (parameters.length > 1)
        {
            String[] connectionDetails;
            if (parameters.length == 2 && parameters[1].contains(":"))
            {
                connectionDetails = parameters[1].split(":");
            }
            else if (parameters.length == 3)
            {
                connectionDetails = new String[] {parameters[1], parameters[2]};
            }
            else
            {
                connectionDetails = new String[] {parameters[1], "25565"};
            }

            if (!connectionDetails[1].matches("[0-9]*") || Integer.parseInt(connectionDetails[1]) >= 65535)
            {
                channel.sendMessage("Invalid server port.").queue();
                return null;
            }
            return connectionDetails;
        }
        channel.sendMessage("Please specify an IP (& port).").queue();
        return null;

    }

    /**
     * Gets the server's response to a query
     * @param ip the server's ip
     * @param port the server's port
     * @return The query response as JSON
     * @throws UnknownHostException server unavailable
     */
    private String getDetails(String ip, int port) throws UnknownHostException
    {
        try
        {
            Socket clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(ip, port), 1000);
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            new Thread(() -> closeConnection(clientSocket)).start();
            byte[] payload = buildRequestPacket(ip, port);
            dos.write(payload, 0, payload.length);
            dos.write(new byte[] {(byte) 0x01, (byte) 0x00});
            dos.flush();
            Scanner scanner = new Scanner(clientSocket.getInputStream());
            String line = scanner.nextLine().trim();
            scanner.close();
            clientSocket.close();
            return line.substring(line.indexOf("{"), line.lastIndexOf("}")+1);
        }
        catch (IOException e)
        {
            throw new UnknownHostException();
        }
    }

    /**
     * Builds a query for a Minecraft server
     * @param ip the server IP
     * @param port the server port
     * @return the bytes for the query
     */
    private byte[] buildRequestPacket(String ip, int port)
    {
        byte[] payload2 = ip.getBytes();
        byte[] payload1 = {(byte) 0x00, (byte) 0x00, (byte) 0x94, (byte) 0x03, (byte) ip.getBytes().length};
        byte[] payload3 = {BigInteger.valueOf(port).toByteArray()[0], BigInteger.valueOf(port).toByteArray()[1], (byte) 0x01};

        byte[] payload = new byte[payload1.length+payload2.length+payload3.length];
        for (int i = 0; i<payload1.length; i++)
        {
            payload[i] = payload1[i];
        }
        for (int i = payload1.length; i<payload1.length+payload2.length; i++)
        {
            payload[i] = payload2[i-payload1.length];
        }
        for (int i = payload1.length+payload2.length; i<payload.length; i++)
        {
            payload[i] = payload3[i-(payload1.length+payload2.length)];
        }
        payload[0] = (byte) (payload.length-1);
        return payload;
    }

    /**
     * Compiles the {@link QueryResult} into a more presentable format
     * @param ip the server's ip
     * @param qr the QueryResult
     * @return pretty formatting of the query result
     */
    private String buildOutput(String ip, QueryResult qr)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("**Server:** ").append(ip).append("\n");
        stringBuilder.append("**Status:** Online\n");
        stringBuilder.append("**Version:** ").append(qr.version.name).append("\n");
        stringBuilder.append("**Players: ** ").append(qr.players.online).append("/").append(qr.players.max).append("\n");
        if (qr.players.sample != null && qr.players.sample.length > 0)
        {
            int playerLimit = (qr.players.sample.length < 3) ? qr.players.sample.length : 3;
            for (int i = 0; i<playerLimit; i++)
            {
                stringBuilder.append("*").append(qr.players.sample[i].name).append(", ");
            }
            stringBuilder.append("...*\n");
        }
        stringBuilder.append("**MOTD:**\n");
        stringBuilder.append(buildMOTD(qr.description));
        return stringBuilder.toString();
    }

    /**
     * Returns the MOTD as a String, converting formatting options to Discord equivalents
     * @param motd the MOTD
     * @return the formatted MOTD
     */
    private String buildMOTD(Description motd)
    {
        boolean boldOn = false;
        boolean italicOn = false;
        boolean strikethroughOn = false;
        boolean underlineOn = false;
        StringBuilder motdBuilder = new StringBuilder();
        if (motd.extra != null)
        {
            for (Extra extra : motd.extra)
            {
                String text = extra.text;
                if (extra.bold && !boldOn)
                {
                    text = "**"+text;
                    boldOn = true;
                }
                else if (!extra.bold && boldOn)
                {
                    motdBuilder.append("**");
                    boldOn = false;
                }
                if (extra.italic && !italicOn)
                {
                    text = "*"+text;
                    italicOn = true;
                }
                else if (!extra.italic && italicOn)
                {
                    motdBuilder.append("*");
                    italicOn = false;
                }
                if (extra.strikethrough && !strikethroughOn)
                {
                    text = "~~"+text;
                    strikethroughOn = true;
                }
                else if (!extra.strikethrough && strikethroughOn)
                {
                    motdBuilder.append("~~");
                    strikethroughOn = false;
                }
                if (extra.underline && !underlineOn)
                {
                    text = "__"+text;
                    underlineOn = true;
                }
                else if (!extra.underline && underlineOn)
                {
                    motdBuilder.append("__");
                    underlineOn = false;
                }
                motdBuilder.append(text);
            }
        }
        motdBuilder.append(motd.text.replaceAll("§[0-9]|§[A-Z]|§[a-z]", "")); //TODO: Properly acknowledge these formatting options.
        return motdBuilder.toString();
    }

    /**
     * Closes the connection, forcing a EOF
     * @param connection the connection to close
     */
    private void closeConnection(Socket connection)
    {
        try
        {
            Thread.sleep(1500);
            connection.close();
        }
        catch (InterruptedException | IOException e)
        {
            e.printStackTrace();
        }
    }

    private class QueryResult
    {
        public Version version;
        public Players players;
        public Description description;
        public String favicon;
        public ModInfo modInfo;
    }
    public class Players
    {
        public int max;
        public int online;
        public PlayersSample[] sample;
    }
    public class PlayersSample
    {
        String id;
        String name;
    }
    private class Version
    {
        public String name;
        public int protocol;
    }
    private class Description
    {
        public Extra[] extra;
        public String text;
    }
    private class Extra
    {
        public String color;
        public boolean strikethrough;
        public boolean bold;
        public boolean underline;
        public boolean italic;
        public String text;
    }
    private class ModInfo
    {
        public String type;
        public Mod[] modList;
    }
    private class Mod
    {
        String id;
        String version;
    }
}
