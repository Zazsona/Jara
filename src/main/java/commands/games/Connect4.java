package commands.games;

import commands.CmdUtil;
import commands.GameCommand;
import configuration.SettingsUtil;
import jara.Core;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Connect4 extends GameCommand
{
    private final Counter[][] counters = new Counter[7][6];
    private EmbedBuilder embed;
    private TextChannel channel;
    private final MessageManager mm = new MessageManager();
    private Member player1;
    private Member player2;
    private boolean isPlayer1Turn = true;
    private enum Counter
    {
        NONE,
        RED, //player2
        BLUE //player1
    }

    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        /*
            Initialisation
         */
        channel = super.createGameChannel(msgEvent, msgEvent.getMember().getEffectiveName()+"s-connect4");
        embed = new EmbedBuilder();
        embed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setTitle("Connect 4");
        Counter winner = null;
        for (int i = 0; i<counters.length; i++)
        {
            for (int j = 0; j < counters[i].length; j++)
            {
                counters[i][j] = Counter.NONE;
            }
        }
        /*
            Game driver
         */
        while (winner == null)
        {
            sendBoard("");
            getInput(msgEvent);
            winner = checkForEndGame();
            isPlayer1Turn = !isPlayer1Turn;
        }
        try
        {
            embed.setThumbnail("https://i.imgur.com/rIBssX0.png");
            if (winner.equals(Counter.NONE))
            {
                sendBoard("**The board is full. Game over!**");
            }
            else if (winner.equals(Counter.RED))
            {
                sendBoard("**"+player2.getEffectiveName()+" is the winner! Congratulations.**");
            }
            else if (winner.equals(Counter.BLUE))
            {
                sendBoard("**"+player1.getEffectiveName()+" is the winner! Congratulations.**");
            }
        }
        catch (NullPointerException e) //This happens if the game is exited before players are even set up
        {
            embed.setDescription("Game cancelled.");
            channel.sendMessage(embed.build()).queue();
        }

        super.deleteGameChannel(msgEvent, channel);
    }

    /**
     * Converts the board into a graphical interface for Discord, and sends it using embed.
     */
    private void sendBoard(String customMessage)
    {
        StringBuilder descBuilder = new StringBuilder();
        descBuilder.append(customMessage).append("\n\n");
        descBuilder.append(":regional_indicator_a: :regional_indicator_b: :regional_indicator_c: :regional_indicator_d: :regional_indicator_e: :regional_indicator_f: :regional_indicator_g:\n\n");
        for (int j = counters[0].length-1; j>-1; j--)
        {
            for (int i = 0; i<counters.length; i++)
            {
                switch (counters[i][j])
                {
                    case NONE:
                        descBuilder.append(":white_circle: ");
                        break;
                    case RED:
                        descBuilder.append(":red_circle: ");
                        break;
                    case BLUE:
                        descBuilder.append(":large_blue_circle: ");
                        break;
                }
            }
            descBuilder.append("\n");
        }
        embed.setDescription(descBuilder.toString());
        channel.sendMessage(embed.build()).queue();
    }

    /**
     * Takes input from the current player.
     * @param msgEvent the context to take input from
     */
    private void getInput(GuildMessageReceivedEvent msgEvent)
    {
        while (true) //Wait for return statement
        {
            Message msg = mm.getNextMessage(channel);
            int column;

            if (msg.getContentDisplay().matches("[A-Ga-g]") || msg.getContentDisplay().equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msgEvent.getGuild().getId()) + "quit")) //If it is a valid input
            {
                if (player1 == null && isPlayer1Turn)
                {
                    player1 = msg.getMember();
                    if (!player1.equals(msgEvent.getMember()))
                    {
                        player2 = msgEvent.getMember();
                    }
                }                                                                                                                   //Set the players if they're not already
                else if (player2 == null && !isPlayer1Turn && !msg.getMember().equals(player1))
                {
                    player2 = msg.getMember();
                }

                if ((msg.getMember().equals(player1) && isPlayer1Turn) || (msg.getMember().equals(player2) && !isPlayer1Turn) || msg.getMember().hasPermission(Permission.ADMINISTRATOR))                  //Run player-only code (+ admin for the sake of ending the game if need be)
                {
                    if (msg.getContentDisplay().equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msgEvent.getGuild().getId()) + "quit"))
                    {
                        Counter winCounter = (msg.getMember().equals(player1)) ? Counter.RED : Counter.BLUE;
                        for (int i = 0; i < counters.length; i++)
                        {
                            for (int j = 0; j < counters[i].length; j++)
                            {
                                counters[i][j] = winCounter;
                            }
                        }
                        return;
                    }
                    switch (msg.getContentDisplay().toUpperCase())
                    {
                        case "A":
                            column = 0;
                            break;
                        case "B":
                            column = 1;
                            break;
                        case "C":
                            column = 2;
                            break;
                        case "D":
                            column = 3;
                            break;
                        case "E":
                            column = 4;
                            break;
                        case "F":
                            column = 5;
                            break;
                        case "G":
                            column = 6;
                            break;
                        default:
                            continue;
                    }

                    for (int i = 0; i < counters[column].length; i++)
                    {
                        if (counters[column][i].equals(Counter.NONE))
                        {
                            counters[column][i] = (isPlayer1Turn) ? Counter.BLUE : Counter.RED;
                            return;
                        }
                    }
                    embed.setDescription("That column is full. Please select another.");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }

    /**
     * Checks to see if the game is over
     * @return counter type of the winner, or null if the game is not over.
     */
    private Counter checkForEndGame()
    {
        int comboSize = 1;
        Counter comboCounter = Counter.NONE;
        /*
            Vertically
         */
        for (int i = 0; i<counters.length; i++)
        {
            for (int j = 0; j<counters[i].length; j++)
            {
                if (counters[i][j].equals(comboCounter) && !counters[i][j].equals(Counter.NONE))
                {
                    comboSize++;
                    if (comboSize == 4)
                    {
                        return comboCounter;
                    }
                }
                else
                {
                    comboCounter = counters[i][j];
                    comboSize = 1;
                }
            }
        }
        comboSize = 1;
        comboCounter = Counter.NONE;
        /*
            Horizontally
         */
        for (int j = 0; j<counters[0].length; j++)
        {
            for (int i = 0; i < counters.length; i++)
            {
                if (counters[i][j].equals(comboCounter) && !counters[i][j].equals(Counter.NONE))
                {
                    comboSize++;
                    if (comboSize == 4)
                    {
                        return comboCounter;
                    }
                }
                else
                {
                    comboCounter = counters[i][j];
                    comboSize = 1;
                }
            }
        }
        /*
            Diagonally
         */
        for (int j = 0; j<counters[0].length; j++)
        {
            for (int i = 0; i < counters.length; i++)
            {
                if (counters[i][j].equals(Counter.NONE))
                {
                    continue;
                }
                /*
                    Up-Left
                 */
                comboCounter = counters[i][j];
                comboSize = 1;
                for (int offset = 1; offset<5; offset++)
                {
                    if (i-offset >= 0 && j+offset < counters[0].length && counters[i-offset][j+offset].equals(comboCounter))
                    {
                        comboSize++;
                        if (comboSize == 4)
                        {
                            return comboCounter;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
                /*
                    Up-Right
                 */
                comboCounter = counters[i][j];
                comboSize = 1;
                for (int offset = 1; offset<5; offset++)
                {
                    if (i+offset < counters.length && j+offset < counters[0].length && counters[i+offset][j+offset].equals(comboCounter))
                    {
                        comboSize++;
                        if (comboSize == 4)
                        {
                            return comboCounter;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }
        /*
            Full check
         */
        for (int i = 0; i<counters.length; i++)
        {
            if (counters[i][counters[i].length-1].equals(Counter.NONE))
            {
                break;
            }
            if (i == counters.length-1)
            {
                return Counter.NONE;
            }
        }
        return null; //No end game
    }
}
