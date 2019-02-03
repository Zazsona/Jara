package commands.games;

import commands.CmdUtil;
import commands.GameCommand;
import configuration.SettingsUtil;
import jara.Core;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.HashMap;
import java.util.Random;

public class WordSearch extends GameCommand
{
    private TextChannel channel;
    private HashMap<String, String> wordCoords;
    private String[] words;
    private boolean complete;

    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        wordCoords = new HashMap<>();
        channel = super.createGameChannel(msgEvent, msgEvent.getMember().getEffectiveName()+"s-Crossword");
        String wordsearch = buildGrid();
        MessageManager mm = new MessageManager();
        sendWelcomeMessage();
        do
        {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription(wordsearch);
            embedBuilder.addField("Words", words[0]+"\n"+words[1], true);
            embedBuilder.addField("", words[2]+"\n"+words[3], true);
            embedBuilder.addField("", words[4]+"\n"+words[5], true);
            embedBuilder.setColor(Core.getHighlightColour(channel.getGuild().getSelfMember()));
            channel.sendMessage(embedBuilder.build()).queue();

            if (complete)
            {
                break;
            }
        } while (checkInput(mm.getNextMessage(channel)));
        endGame(msgEvent);
    }
    private void sendWelcomeMessage()
    {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription("Welcome to Word Search! Simply enter the co-ordinates of the word when you find it, and try to find all "+words.length+"!\nE.g: B5-F9");
        embedBuilder.setColor(Core.getHighlightColour(channel.getGuild().getSelfMember()));
        channel.sendMessage(embedBuilder.build()).queue();
    }

    private void endGame(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription("That's a wrap! Thanks for playing.");
        embedBuilder.setColor(Core.getHighlightColour(channel.getGuild().getSelfMember()));
        channel.sendMessage(embedBuilder.build()).queue();
        super.deleteGameChannel(msgEvent, channel);
    }

    private String buildGrid()
    {
        String[][] board = new String[9][9];
        words = new String[6];
        for (int i = 0; i<words.length; i++)
        {
            do
            {
                do
                {
                    words[i] = CmdUtil.getRandomWord().toUpperCase();
                } while (words[i].length() > board.length-2);

            } while (!placeWord(words[i], board));
        }

        Random r = new Random();
        String[] letters = {"A", "A", "A", "B","B","B", "C", "C", "C", "C", "D","D","D", "E","E","E","E", "F","F", "G","G","G", "H", "I", "J", "K", "L","L","L", "M","M","M", "N","N", "O", "P","P", "Q", "R","R","R", "S", "S", "S", "S", "S", "S", "S", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        for (int x = 0; x<board.length; x++)
        {
            for (int y = 0; y<board[x].length; y++)
            {
                if (board[x][y] == null)
                {
                    board[x][y] = letters[r.nextInt(letters.length)];
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("```  +-----------------------------------+ \n");
        for (int x = 0; x<board.length; x++)
        {
            sb.append((x+1)).append(" ");
            for (int y = 0; y<board[x].length; y++)
            {
                sb.append("| ").append(board[x][y]).append(" ");
            }
            sb.append("|\n  +-----------------------------------+\n");
        }
        sb.append("    A   B   C   D   E   F   G   H   I```");
        return sb.toString();
    }

    private boolean placeWord(String word, String[][] board)
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
                            if (board[x][y+i] != null)                                       //First check to ensure there is space for the word
                            {
                                if (!board[x][y+i].equals(word.substring(i, i+1)))
                                {
                                    throw new ArrayIndexOutOfBoundsException("Position occupied.");
                                }
                            }
                        }
                        for (int i = 0; i<word.length(); i++)                                   //Place the word
                        {
                            board[x][y+i] = word.substring(i, i+1);
                        }
                        wordCoords.put(arrayIndexToGridIndex(x, y)+arrayIndexToGridIndex(x, y+(word.length()-1)), word);
                        break;
                    case 1:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x+i][y+i] != null)
                            {
                                if (!board[x+i][y+i].equals(word.substring(i, i+1)))
                                {
                                    throw new ArrayIndexOutOfBoundsException("Position occupied.");
                                }
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            board[x+i][y+i] = word.substring(i, i+1);
                        }
                        wordCoords.put(arrayIndexToGridIndex(x, y)+arrayIndexToGridIndex(x+(word.length()-1), y+(word.length()-1)), word);
                        break;
                    case 2:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x+i][y] != null)
                            {
                                if (!board[x+i][y].equals(word.substring(i, i+1)))
                                {
                                    throw new ArrayIndexOutOfBoundsException("Position occupied.");
                                }
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            board[x+i][y] = word.substring(i, i+1);
                        }
                        wordCoords.put(arrayIndexToGridIndex(x, y)+arrayIndexToGridIndex(x+(word.length()-1), y), word);
                        break;
                    case 3:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x+i][y-i] != null)
                            {
                                if (!board[x+i][y-i].equals(word.substring(i, i+1)))
                                {
                                    throw new ArrayIndexOutOfBoundsException("Position occupied.");
                                }
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            board[x+i][y-i] = word.substring(i, i+1);
                        }
                        wordCoords.put(arrayIndexToGridIndex(x, y)+arrayIndexToGridIndex(x+(word.length()-1), y-(word.length()-1)), word);
                        break;
                    case 4:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x][y-i] != null)
                            {
                                if (!board[x][y-i].equals(word.substring(i, i+1)))
                                {
                                    throw new ArrayIndexOutOfBoundsException("Position occupied.");
                                }
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            board[x][y-i] = word.substring(i, i+1);
                        }
                        wordCoords.put(arrayIndexToGridIndex(x, y)+arrayIndexToGridIndex(x, y-(word.length()-1)), word);
                        break;
                    case 5:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x-i][y-i] != null)
                            {
                                if (!board[x-i][y-i].equals(word.substring(i, i+1)))
                                {
                                    throw new ArrayIndexOutOfBoundsException("Position occupied.");
                                }
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            board[x-i][y-i] = word.substring(i, i+1);
                        }
                        wordCoords.put(arrayIndexToGridIndex(x, y)+arrayIndexToGridIndex(x-(word.length()-1), y-(word.length()-1)), word);
                        break;
                    case 6:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x-i][y] != null)
                            {
                                if (!board[x-i][y].equals(word.substring(i, i+1)))
                                {
                                    throw new ArrayIndexOutOfBoundsException("Position occupied.");
                                }
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            board[x-i][y] = word.substring(i, i+1);
                        }
                        wordCoords.put(arrayIndexToGridIndex(x, y)+arrayIndexToGridIndex(x-(word.length()-1), y), word);
                        break;
                    case 7:
                        for (int i = 0; i<word.length(); i++)
                        {
                            if (board[x-i][y+i] != null)
                            {
                                if (!board[x-i][y+i].equals(word.substring(i, i+1)))
                                {
                                    throw new ArrayIndexOutOfBoundsException("Position occupied.");
                                }
                            }
                        }
                        for (int i = 0; i<word.length(); i++)
                        {
                            board[x-i][y+i] = word.substring(i, i+1);
                        }
                        wordCoords.put(arrayIndexToGridIndex(x, y)+arrayIndexToGridIndex(x-(word.length()-1), y+(word.length()-1)), word);
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

    private String arrayIndexToGridIndex(int x, int y)
    {
        if (x < 9 && y < 9)
        {
            x += 1;
            y += 65;
            String result = String.valueOf((char) y).toUpperCase();
            result += x;
            return result;
        }
        return "";
    }

    private boolean checkInput(Message msg)
    {
        String input = msg.getContentDisplay();
        input = input.toUpperCase().trim();
        input = input.replace("-", "").replace(" ", "");

        if (input.length() == 4)
        {
            System.out.println(wordCoords);
            System.out.println(input);
            String backwardsInput = (input.substring(input.length()-2)+input.substring(0, 2));
            System.out.println(backwardsInput);
            if (wordCoords.containsKey(input) || wordCoords.containsKey(backwardsInput))
            {
                int completeWords = 0;
                for (int i = 0; i<words.length; i++)
                {
                    if (words[i].equals(wordCoords.get(input)) || words[i].equals(wordCoords.get(backwardsInput)))
                    {
                        words[i] = "~~*"+words[i]+"*~~";
                        completeWords++;
                    }
                    else if (words[i].startsWith("~~"))
                    {
                        completeWords++;
                    }
                }
                if (completeWords == words.length)
                {
                    complete = true;
                }
            }
            else if (input.equals(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId()) + "quit"))
            {
                complete = true;
            }
            else
            {
                channel.sendMessage("Sorry, that's not it.").queue();
            }
            return true;
        }
        return false;
    }
}
