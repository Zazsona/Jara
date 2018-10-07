package commands.games;

import commands.Command;
import jara.Core;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Random;

public class CountdownConundrum extends Command
{
    private TextChannel channel;
    private int seconds;
    private EmbedBuilder embed;
    private Message embedMsg;


    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        channel = super.createGameChannel(msgEvent, msgEvent.getMember().getEffectiveName() + "s-countdown-conundrum");
        String winner = "";
        String[][] conundrums = getConundrums();
        Random r = new Random();
        int index = r.nextInt(conundrums.length);
        embed = new EmbedBuilder();
        embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setTitle("Countdown Conundrum");
        embed.setThumbnail("https://i.imgur.com/0uNRZWG.png");
        embed.setDescription("Find the anagram before time runs out!\n\n***"+conundrums[index][0].toUpperCase()+"***");
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

                if (answer.equalsIgnoreCase(conundrums[index][1]))
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
            embed.setDescription("Congratulations! The word was "+conundrums[index][1]+".\n"+winner+" got it in just "+seconds+" seconds!");
            channel.sendMessage(embed.build()).queue();
        }
        else
        {
            embed.setThumbnail("https://i.imgur.com/KwjqNkH.png");
            embed.setDescription("Oh no! Looks like you were beaten by the clock.\nBetter luck next time!");
            channel.sendMessage(embed.build()).queue();
        }

        super.deleteGameChannel(msgEvent, channel);
        return;

    }
    public String[][] getConundrums()
    {
        String[][] conundrums = new String[40][2];
        conundrums[0][0] = "Soartrip";
        conundrums[0][1] = "Airports";
        conundrums[1][0] = "Toooften";
        conundrums[1][1] = "Footnote";
        conundrums[2][0] = "Froglegs";
        conundrums[2][1] = "Floggers";
        conundrums[3][0] = "Girltina";
        conundrums[3][1] = "Tailing";
        conundrums[4][0] = "Peterdad";
        conundrums[4][1] = "Departed";
        conundrums[5][0] = "Scotchyip";
        conundrums[5][1] = "Psychotic";
        conundrums[6][0] = "Rifledick";
        conundrums[6][1] = "Flickered";
        conundrums[7][0] = "Atellyhit";
        conundrums[7][1] = "Lethality";
        conundrums[8][0] = "Wontcnoud";
        conundrums[8][1] = "Countdown";
        conundrums[9][0] = "Drumuncon";
        conundrums[9][1] = "Conundrum";
        conundrums[10][0] = "Crewonset";
        conundrums[10][1] = "Sweetcorn";
        conundrums[11][0] = "Minigramt";
        conundrums[11][1] = "Immigrant";
        conundrums[12][0] = "Hashpipes";
        conundrums[12][1] = "Shipshape";
        conundrums[13][0] = "Itmbignre";
        conundrums[13][1] = "Timbering";
        conundrums[14][0] = "Amusingmy";
        conundrums[14][1] = "Gymnasium";
        conundrums[15][0] = "Largebaps";
        conundrums[15][1] = "Graspable";
        conundrums[16][0] = "Sentminor";
        conundrums[16][1] = "Innermost";
        conundrums[17][0] = "Drapospel";
        conundrums[17][1] = "Prolapsed";
        conundrums[18][0] = "Dickhewer";
        conundrums[18][1] = "Prolapsed";
        conundrums[19][0] = "Flungfish";
        conundrums[19][1] = "Shuffling";
        conundrums[20][0] = "Cuzzsball";
        conundrums[20][1] = "Scuzzball";
        conundrums[21][0] = "Juboxkees";
        conundrums[21][1] = "Jukeboxes";
        conundrums[22][0] = "Pizzaarpa";
        conundrums[22][1] = "Paparazzi";
        conundrums[23][0] = "Repsybarr";
        conundrums[23][1] = "Raspberry";
        conundrums[24][0] = "Coppypock";
        conundrums[24][1] = "Poppycock";
        conundrums[25][0] = "Jifyectob";
        conundrums[25][1] = "Objectify";
        conundrums[26][0] = "Offockkns";
        conundrums[26][1] = "Knockoffs";
        conundrums[27][0] = "Fillyjesh";
        conundrums[27][1] = "Jellyfish";
        conundrums[28][0] = "Muglebubb";
        conundrums[28][1] = "Bubblegum";
        conundrums[29][0] = "Mooschzer";
        conundrums[29][1] = "Schmoozer";
        conundrums[30][0] = "Dizeminim";
        conundrums[30][1] = "Minimized";
        conundrums[31][0] = "Druowbszz";
        conundrums[31][1] = "Buzzwords";
        conundrums[32][0] = "Bornmaity";
        conundrums[32][1] = "Abnormity";
        conundrums[33][0] = "Badtedack";
        conundrums[33][1] = "Backdated";
        conundrums[34][0] = "Bitialies";
        conundrums[34][1] = "Abilities";
        conundrums[35][0] = "Repledmas";
        conundrums[35][1] = "Resampled";
        conundrums[36][0] = "Rarhingbor";
        conundrums[36][1] = "Harboring";
        conundrums[37][0] = "Finlentus";
        conundrums[37][1] = "Influents";
        conundrums[38][0] = "Furyllest";
        conundrums[38][1] = "Restfully";
        conundrums[39][0] = "Vocelkits";
        conundrums[39][1] = "Livestock";
        return conundrums;
    }

}
