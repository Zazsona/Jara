package commands.games;

import commands.CmdUtil;
import commands.Command;
import jara.Core;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

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
        String progress = word.replaceAll("[a-zA-Z]", "#");
        byte attempts = 8;
        StringBuilder guessHistory = new StringBuilder();

        MessageManager msgManager = new MessageManager();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setTitle("Hangman");
        embed.setDescription(":game_die:  Welcome to Hangman! The word's **" + progress + "**. :game_die: ");
        channel.sendMessage(embed.build()).queue();

        while (attempts>0 && !word.equals(progress))
        {
            EmbedBuilder gameEmbed = new EmbedBuilder();
            Message msg = msgManager.getNextMessage(channel);
            boolean correctGuess = false;

            if (msg.getContentDisplay().length() == 1)
            {
                if (guessHistory.toString().contains(msg.getContentDisplay().toUpperCase())) //TODO: This should be tidied.
                {
                    gameEmbed.setTitle("Hangman");
                    gameEmbed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
                    gameEmbed.setDescription("You've already used that letter. Try another.");
                    channel.sendMessage(gameEmbed.build()).queue();
                    continue;
                }
                char guess = msg.getContentDisplay().toLowerCase().charAt(0);
                guessHistory.append(", ").append(Character.toUpperCase(guess));

                char[] wordArray = word.toCharArray();
                char[] progressArray = progress.toCharArray();

                for (int i =0; i<wordArray.length; i++)
                {
                    if (wordArray[i] == guess)
                    {
                        progressArray[i] = guess;
                        gameEmbed.setDescription("**You got one!**");
                        gameEmbed.setThumbnail("https://i.imgur.com/mBPBip8.png");
                        correctGuess = true;
                    }
                }

                progress = String.valueOf(progressArray);

                if (!correctGuess)
                {
                    gameEmbed.setDescription("**Uh-oh! That's not it.**");
                    attempts--;
                    switch (attempts)
                    {
                        case 7:
                            gameEmbed.setThumbnail("https://i.imgur.com/NynGFvk.png");
                            break;
                        case 6:
                            gameEmbed.setThumbnail("https://i.imgur.com/jF1MxtR.png");
                            break;
                        case 5:
                            gameEmbed.setThumbnail("https://i.imgur.com/a1d7xvA.png");
                            break;
                        case 4:
                            gameEmbed.setThumbnail("https://i.imgur.com/wKm9Uyn.png");
                            break;
                        case 3:
                            gameEmbed.setThumbnail("https://i.imgur.com/ZwxnCKS.png");
                            break;
                        case 2:
                            gameEmbed.setThumbnail("https://i.imgur.com/nv7UCAy.png");
                            break;
                        case 1:
                            gameEmbed.setThumbnail("https://i.imgur.com/zYfry8y.png");
                            break;
                        //If 0, that's game over.
                    }  //Generate Hangman image
                }

                gameEmbed.setTitle("Hangman");
                gameEmbed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
                gameEmbed.addField("Progress", progress, true);
                gameEmbed.addField("Guesses", guessHistory.toString().substring(2), true);
                if (attempts<=0)                    //Basically, we don't want to send a standard 'progress' embed, and then follow it up with a win/lose one. With this, we're replacing the progress one.
                {
                    gameEmbed.setDescription("**Oh no! I'm afraid you didn't quite get it. The word was "+word+"**");
                    gameEmbed.setThumbnail("https://i.imgur.com/8ragw82.png");
                }
                else if (progress.equals(word))
                {
                    gameEmbed.setDescription("**Congratulations! You win. The word was "+word+"**");
                    gameEmbed.setThumbnail("https://i.imgur.com/aRkU3aD.png");
                }
                channel.sendMessage(gameEmbed.build()).queue();
            }
        }
        super.deleteGameChannel(msgEvent, channel);
    }
}
