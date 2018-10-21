package commands.games;

import commands.GameCommand;
import jara.Core;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class CountdownConundrum extends GameCommand
{
    private static Logger logger = LoggerFactory.getLogger(CountdownConundrum.class);
    /**
     * The time it took to guess the conundrum
     */
    private int seconds;
    /**
     * The embed
     */
    private EmbedBuilder embed;
    /**
     * The message containing the embed
     */
    private Message embedMsg;

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        TextChannel channel = super.createGameChannel(msgEvent, msgEvent.getMember().getEffectiveName() + "s-countdown-conundrum");
        try
        {
            String winner = "";
            String[] conundrum = getConundrum();
            Random r = new Random();
            embed = new EmbedBuilder();
            embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            embed.setTitle("Countdown Conundrum");
            embed.setThumbnail("https://i.imgur.com/0uNRZWG.png");
            embed.setDescription("Find the anagram before time runs out!\n\n***"+conundrum[1].toUpperCase()+"***");
            embedMsg = channel.sendMessage(embed.build()).complete();

            Runnable timer = () ->
            {
                seconds = 0;
                while (seconds <= 30)
                {
                    try
                    {
                        switch (seconds)
                        {
                            case 30:
                                embed.setThumbnail("https://i.imgur.com/eMrORg8.png");
                                embedMsg.editMessage(embed.build()).queue();
                                break;
                            case 23:
                                embed.setThumbnail("https://i.imgur.com/F2deeEY.png");
                                embedMsg.editMessage(embed.build()).queue();
                                break;
                            case 15:
                                embed.setThumbnail("https://i.imgur.com/JpwwNrY.png");
                                embedMsg.editMessage(embed.build()).queue();
                                break;
                            case 8:
                                embed.setThumbnail("https://i.imgur.com/llmbGHa.png");
                                embedMsg.editMessage(embed.build()).queue();
                                break;

                        }
                        Thread.sleep(1000);
                        seconds++;
                    }
                    catch (InterruptedException e)
                    {
                        //Game end.
                        return;
                    }

                }
            };
            Thread timerThread = new Thread(timer);
            timerThread.start();
            MessageManager msgManager = new MessageManager();
            while (seconds <= 30)
            {
                Message message = msgManager.getNextMessage(channel, 1000);
                if (message != null)
                {
                    String answer = message.getContentDisplay();

                    if (answer.length() == 5 && answer.toLowerCase().endsWith("quit"))
                    {
                        timerThread.interrupt();
                    }

                    if (answer.equalsIgnoreCase(conundrum[0]))
                    {
                        winner = message.getMember().getEffectiveName();
                        timerThread.interrupt();
                        break;
                    }
                    else if (answer.length() == 9)
                    {
                        channel.sendMessage("Good guess, but that's not it.").queue();
                    }
                }
            }

            if (!winner.equals(""))
            {
                embed.setThumbnail("https://i.imgur.com/scKHMRb.png");
                embed.setDescription("Congratulations! The word was "+conundrum[0]+".\n"+winner+" got it in just "+seconds+" seconds!");
                channel.sendMessage(embed.build()).queue();
            }
            else
            {
                embed.setThumbnail("https://i.imgur.com/KwjqNkH.png");
                embed.setDescription("Oh no! You were beaten by the clock. The word was "+conundrum[0]+".\nBetter luck next time!");
                channel.sendMessage(embed.build()).queue();
            }
        }
        catch (IOException e)
        {
            channel.sendMessage("An unexpected error occured.").queue();
            e.printStackTrace();
        }
        super.deleteGameChannel(msgEvent, channel);
    }

    /**
     * Gets the list of conundrum, where the first array index is the answer, and the second is the anagram.
     * @return String[][] - The conundrums
     */
    private String[] getConundrum() throws IOException
    {
        try
        {
            String[] conundrum = new String[2];
            File conundrumFile = new File(getClass().getResource("/game/conundrums.txt").toURI());
            double lines = Files.lines(conundrumFile.toPath()).count();
            Scanner scanner = new Scanner(conundrumFile);
            long conundrumIndex = Math.round(Math.random()*lines);
            while (conundrumIndex >= 0)
            {
                conundrum[0] = scanner.next();
                conundrumIndex--;
            }
            scanner.close();

            byte[] conundrumChars = conundrum[0].getBytes();
            Random r = new Random();
            for (int i = conundrumChars.length - 1; i > 0; i--)
            {
                int charIndex = r.nextInt(i + 1);       //This will eventually go to between 0 or 1, however those values (most likely) will have already been swapped.
                byte temp = conundrumChars[charIndex];
                conundrumChars[charIndex] = conundrumChars[i];
                conundrumChars[i] = temp;
            }
            conundrum[1] = new String(conundrumChars, StandardCharsets.UTF_8);

            return conundrum;
        }
        catch (URISyntaxException e)
        {
            logger.error("The Conundrums file is missing or corrupt. Countdown Conundrums cannot function.");
            throw new IOException();
        }

    }

}
