package commands.games;

import commands.CmdUtil;
import commands.Command;
import configuration.SettingsUtil;
import jara.Core;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;

public class Hangman extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        TextChannel channel = super.createGameChannel(msgEvent, msgEvent.getMember().getEffectiveName()+"s-hangman");
        String word = CmdUtil.getRandomWord(); //Select a random word from "WordList"

        while (word.length() > 15 || word.length() < 5) //We don't want a giant word, that'd be unfair. But we also don't want a tiny one.
        {
            word = CmdUtil.getRandomWord();
        }
        char[] progress = new char[word.length()];
        Arrays.fill(progress, '#');

        EmbedBuilder embed = getEmbedStyle(msgEvent);
        embed.setDescription(":game_die:  Welcome to Hangman! The word's **" + String.valueOf(progress) + "**. :game_die: ");
        channel.sendMessage(embed.build()).queue();

        boolean success = startGame(msgEvent, word, progress, channel);
        endGame(msgEvent, channel, word, success);
    }

    /**
     * Main game logic method. This runs Hangman until a win or lose condition is found.
     * @param msgEvent context
     * @param word the word to guess
     * @param progress the players' progress towards the word
     * @param channel the channel to interact with
     * @return
     * true - Players' win<br>
     * false - Bot's win
     */
    private boolean startGame(GuildMessageReceivedEvent msgEvent, String word, char[] progress, TextChannel channel)
    {
        StringBuilder guessHistory = new StringBuilder();
        MessageManager msgManager = new MessageManager();
        byte attempts = 8;

        while (attempts>0)
        {
            EmbedBuilder gameEmbed = getEmbedStyle(msgEvent);
            Message msg = msgManager.getNextMessage(channel);
            boolean correctGuess = false;

            if (msg.getContentDisplay().equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msgEvent.getGuild().getId()) + "quit"))
            {
                return false; //The game has been quit, and as such lost.
            }
            else if (msg.getContentDisplay().length() == 1) //If the message is only one char in length...
            {
                if (guessHistory.toString().contains(msg.getContentDisplay().toUpperCase())) //Check if it is a previous guess...
                {
                    gameEmbed.setDescription("You've already used that letter. Try another.");
                    channel.sendMessage(gameEmbed.build()).queue();
                    continue;   //If it is, inform the user & skip it.
                }
                else    //If it isn't, see how they did.
                {
                    /*

                        The main gameplay logic sits here. This determines if they have correctly guessed a letter.

                     */
                    char guess = msg.getContentDisplay().toLowerCase().charAt(0);
                    guessHistory.append(", ").append(Character.toUpperCase(guess));

                    for (int i = 0; i < word.length(); i++) //Check if word contains the guess.
                    {
                        if (word.charAt(i) == guess)        //If it does...
                        {
                            progress[i] = guess;
                            correctGuess = true;            //Register this in progress; mark the guess as correct.
                        }
                    }


                    if (correctGuess)
                    {
                        gameEmbed.setDescription("**You got one!**");
                        gameEmbed.setThumbnail("https://i.imgur.com/mBPBip8.png");
                        if (Arrays.equals(word.toCharArray(), progress))
                        {
                            return true;
                        }
                    }
                    else //If they guessed incorrectly...
                    {
                        attempts--;
                        gameEmbed.setThumbnail(getHangmanImage(attempts));
                        gameEmbed.setDescription("**Uh-oh! That's not it.**");
                    }

                    gameEmbed.addField("Progress", String.valueOf(progress), true);
                    gameEmbed.addField("Guesses", guessHistory.toString().substring(2), true);      //Run this regardless of correct or incorrect guess.
                    channel.sendMessage(gameEmbed.build()).queue();
                }
            }
        }
        return false;
    }

    /**
     * Gets the respective Hangman image for the current failed attempt.
     * @param attempts
     * @return String - The image URL
     */
    private String getHangmanImage(byte attempts)
    {
        switch (attempts)  //Generate Hangman image
        {
            case 7:
                return ("https://i.imgur.com/NynGFvk.png");
            case 6:
                return ("https://i.imgur.com/jF1MxtR.png");
            case 5:
                return ("https://i.imgur.com/a1d7xvA.png");
            case 4:
                return ("https://i.imgur.com/wKm9Uyn.png");
            case 3:
                return ("https://i.imgur.com/ZwxnCKS.png");
            case 2:
                return ("https://i.imgur.com/nv7UCAy.png");
            case 1:
                return ("https://i.imgur.com/zYfry8y.png");
            case 0:
                return ("https://i.imgur.com/5a7HY94.png");
        }
        return null;
    }

    /**
     * Outputs the winner and performs tidy up
     * @param msgEvent context
     * @param channel channel to announce in
     * @param word the final word
     * @param success whether the players succeeded
     */
    private void endGame(GuildMessageReceivedEvent msgEvent, TextChannel channel, String word, boolean success)
    {
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        if (!success)
        {
            embed.setDescription("**Oh no! The word was "+word+"**.");
            embed.setThumbnail("https://i.imgur.com/mhqyyd2.png");
        }
        else
        {
            embed.setDescription("**Congratulations! You win. The word was "+word+"**.");
            embed.setThumbnail("https://i.imgur.com/aRkU3aD.png");
        }
        channel.sendMessage(embed.build()).queue();
        super.deleteGameChannel(msgEvent, channel);
    }

    /**
     * Gets the base embed style for this game
     * @param msgEvent context
     * @return EmbedBuilder - a pre-styled embed. Just add milk.
     */
    private EmbedBuilder getEmbedStyle(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Hangman");
        embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        return embed;
    }
}
