package commands.games;

import commands.Command;
import jara.Core;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.Random;

public class Hangman extends Command
{
    private static String[] wordList; //Despite being static, we won't set this yet to save on memory.
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        TextChannel channel = super.createGameChannel(msgEvent, msgEvent.getMember().getEffectiveName()+"s-hangman");
        if (wordList == null)
        {
            wordList = new String[] { "mind", "joint", "count", "bird", "roll", "strikebreaker", "explosion", "negotiation", "halt", "know", "sodium", "ample", "fever", "manufacture", "obligation", "literacy", "carve", "player", "spectrum", "dump", "crook", "java", "pamphlet", "playstation", "switch", "brush", "header", "footer", "grill", "burning", "destruction", "string", "face", "crook", "jazz", "portal", "gang", "machine", "deadly", "shameful", "bloodstain", "beacon", "civilisation", "number", "rocket", "flaming", "ceremony", "comet", "gushing", "debit", "credit", "mythical", "rock", "guitar", "final", "fantasy", "kingdom", "heart", "wrist", "band", "engine", "blade", "key", "coaster", "wing", "nocturnal", "titan", "leviathan", "man", "keyboard", "poo", "bear", "dog", "cat", "dragon", "den", "denizen", "technology", "mouse", "laptop", "kid", "wolf", "cradle", "camera", "cable", "urine", "expansionism", "ruse", "dishwasher", "walnut", "idiot", "plonker", "fool", "human", "libra", "element", "scissors", "word", "game", "bike", "pelt", "producer", "rainbow", "ripple", "twine", "variety", "procrastination", "bed", "headphones", "phone", "hyena", "patience", "general", "discord", "cloud", "middle", "street", "speaker", "array", "button", "england", "afghanistan", "typo", "window", "sprite", "mobile", "broker", "tipster", "bar", "pub", "cactus", "dime", "pound", "coin", "penny", "cartridge", "disc", "solid", "state", "drive", "hard", "nexus", "sonic", "sky", "volume", "attitude", "beanstalk", "frazzle", "court", "crush", "willy", "orchestra", "studio", "stage", "toast", "bread", "biscuit", "ring", "microphone", "boom", "central", "unit", "motherboard", "sword", "weapon", "bow", "video", "space", "jupiter", "earth", "mars", "star", "vacuum", "hamster", "fuel", "car", "muffin", "scone", "ghost", "learn", "college", "school", "stack", "slippers", "pen", "pencil", "rubber", "cup", "case", "tiara", "tractor", "gauntlet", "bikini", "beast", "cheek", "shirt", "fedora", "sock", "lego", "brick", "wednesday", "tuxedo", "suit", "mail", "space", "square", "design", "device", "website", "commerce", "apple", "news", "logo", "week", "free", "trial", "offer", "code", "figure", "funny", "hilarious", "year", "meme", "dank", "comic", "nerd", "geek", "outcast", "intelligence", "bum" };
        }
        Random r = new Random();
        String word = wordList[r.nextInt(wordList.length)]; //Select a random word from "WordList"
        String progress = word.replaceAll("[a-zA-Z]", "#");
        byte attempts = 9;
        StringBuilder guessHistory = new StringBuilder();

        MessageManager msgManager = new MessageManager();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setTitle("Hangman");
        embed.setDescription(":game_die:  Welcome to Hangman! The word's **" + progress + "**. :game_die: ");
        channel.sendMessage(embed.build()).queue();

        while (attempts>1 && !word.equals(progress))
        {
            EmbedBuilder gameEmbed = new EmbedBuilder();
            Message msg = msgManager.getNextMessage(channel);
            boolean correctGuess = false;

            if (msg.getContentDisplay().length() == 1)
            {
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
                        case 8:
                            gameEmbed.setThumbnail("https://i.imgur.com/NynGFvk.png");
                            break;
                        case 7:
                            gameEmbed.setThumbnail("https://i.imgur.com/jF1MxtR.png");
                            break;
                        case 6:
                            gameEmbed.setThumbnail("https://i.imgur.com/a1d7xvA.png");
                            break;
                        case 5:
                            gameEmbed.setThumbnail("https://i.imgur.com/wKm9Uyn.png");
                            break;
                        case 4:
                            gameEmbed.setThumbnail("https://i.imgur.com/ZwxnCKS.png");
                            break;
                        case 3:
                            gameEmbed.setThumbnail("https://i.imgur.com/nv7UCAy.png");
                            break;
                        case 2:
                            gameEmbed.setThumbnail("https://i.imgur.com/zYfry8y.png");
                            break;
                        case 1:
                            gameEmbed.setThumbnail("https://i.imgur.com/8ragw82.png");
                            break;
                    }  //Generate Hangman image
                }

                gameEmbed.setTitle("Hangman");
                gameEmbed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
                gameEmbed.addField("Progress", progress, true);
                gameEmbed.addField("Guesses", guessHistory.toString().substring(2), true);
                channel.sendMessage(gameEmbed.build()).queue();
            }
        }

        if (attempts<=0)
        {
            embed.setDescription("Oh no! I'm afraid you didn't quite get it. The word was "+word);
            channel.sendMessage(embed.build()).queue();
        }
        else
        {
            embed.setDescription("Congratulations! You win. The word was "+word);
            channel.sendMessage(embed.build()).queue();
        }
        super.deleteGameChannel(msgEvent, channel);
    }
}
