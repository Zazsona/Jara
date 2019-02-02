package commands.games;

import commands.CmdUtil;
import commands.GameCommand;
import commands.utility.Randomizer;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Random;

public class WordSearch extends GameCommand
{
    TextChannel channel;
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        channel = super.createGameChannel(msgEvent, msgEvent.getMember().getEffectiveName()+"s-Crossword");
        channel.sendMessage(buildGrid()).queue();
    }

    private MessageEmbed buildGrid()
    {
        String[][] board = new String[9][9];
        String[] words = new String[6];
        for (int i = 0; i<words.length; i++)
        {
            do
            {
                do
                {
                    words[i] = CmdUtil.getRandomWord();
                } while (words[i].length() > board.length);
            } while (!placeWord(words[i], board));
        }

        for (int x = 0; x<board.length; x++)
        {
            for (int y = 0; y<board[x].length; y++)
            {
                if (board[x][y] == null)
                {
                    board[x][y] = CmdUtil.getRandomWord().substring(0, 1); //This gets the general pattern of word starting letters, avoiding cases where, for example, Z comes up as often as A, despite Zs infrequent use.
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("```+-----------------------------------+ \n");
        for (int x = 0; x<board.length; x++)
        {
            for (int y = 0; y<board[x].length; y++)
            {
                sb.append("| ").append(board[x][y]+" ");
            }
            sb.append("|\n+-----------------------------------+\n");
        }
        sb.append("```");
        
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(sb.toString());
        embedBuilder.addField("Words", words[0]+"\n"+words[1], true);
        embedBuilder.addField("", words[2]+"\n"+words[3], true);
        embedBuilder.addField("", words[4]+"\n"+words[5], true);
        embedBuilder.setColor(Core.getHighlightColour(channel.getGuild().getSelfMember()));
        return embedBuilder.build();
    }

    public boolean placeWord(String word, String[][] board)
    {
        Random r = new Random();
        for (int a = 2; a>-1; a--)  //We make 3 attempts to place the word. It may be impossible to fit a word, hence the limit.
        {
            try
            {
                int x = r.nextInt(board.length);
                int y = r.nextInt(board[x].length); //Random location on the board

                switch (r.nextInt(8))           //Each value represents a different direction. 360 degrees at 45 degree intervals.
                {
                    case 0:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x][y+i] != null)                                          //First check to ensure there is space for the word
                            {
                                throw new ArrayIndexOutOfBoundsException("Position occupied.");
                            }
                        }
                        for (int i = 0; i<word.length(); i++)                                   //Place the word
                        {
                            if (board[x][y+i] == null)
                            {
                                board[x][y+i] = word.substring(i, i+1);
                            }
                        }
                        break;
                    case 1:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x+i][y+i] != null)
                            {
                                throw new ArrayIndexOutOfBoundsException("Position occupied.");
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x+i][y+i] == null)
                            {
                                board[x+i][y+i] = word.substring(i, i+1);
                            }
                        }
                        break;
                    case 2:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x+i][y] != null)
                            {
                                throw new ArrayIndexOutOfBoundsException("Position occupied.");
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x+i][y] == null)
                            {
                                board[x+i][y] = word.substring(i, i+1);
                            }
                        }
                        break;
                    case 3:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x+i][y-i] != null)
                            {
                                throw new ArrayIndexOutOfBoundsException("Position occupied.");
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x+i][y-i] == null)
                            {
                                board[x+i][y-i] = word.substring(i, i+1);
                            }
                        }
                        break;
                    case 4:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x][y-i] != null)
                            {
                                throw new ArrayIndexOutOfBoundsException("Position occupied.");
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x][y-i] == null)
                            {
                                board[x][y-i] = word.substring(i, i+1);
                            }
                        }
                        break;
                    case 5:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x-i][y-i] != null)
                            {
                                throw new ArrayIndexOutOfBoundsException("Position occupied.");
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x-i][y-i] == null)
                            {
                                board[x-i][y-i] = word.substring(i, i+1);
                            }
                        }
                        break;
                    case 6:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x-i][y] != null)
                            {
                                throw new ArrayIndexOutOfBoundsException("Position occupied.");
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x-i][y] == null)
                            {
                                board[x-i][y] = word.substring(i, i+1);
                            }
                        }
                        break;
                    case 7:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x-i][y+i] != null)
                            {
                                throw new ArrayIndexOutOfBoundsException("Position occupied.");
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x-i][y+i] == null)
                            {
                                board[x-i][y+i] = word.substring(i, i+1);
                            }
                        }
                        break;
                }
                return true;
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                if (a == 0)
                {
                    return false;
                }
            }
        }
        return false;
    }
}
