package commands.games;

import commands.CmdUtil;
import commands.Command;
import jara.Core;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

public class LastWord extends Command
{
    private static Logger logger = LoggerFactory.getLogger(LastWord.class);
    private TextChannel channel;
    private String letter;

    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        try
        {
            channel = super.createGameChannel(msgEvent, msgEvent.getMember().getEffectiveName()+"s-lastword");

            String[] letters = {"A", "A", "A", "B","B","B", "C", "C", "C", "C", "D","D","D", "E","E","E","E", "F","F", "G","G","G", "H", "I", "J", "K", "L","L","L", "M","M","M", "N","N", "O", "P","P", "Q", "R","R","R", "S", "S", "S", "S", "S", "S", "S", "S", "T", "U", "V", "W", "X", "Y", "Z"};
            letter = letters[new Random().nextInt(letters.length)];
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Last Word");
            embed.setDescription(":game_die: Welcome to The Last Word! :game_die:\nI'm looking for **"+ CmdUtil.getRandomTopic()+"** starting with **"+letter+"**");
            embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
            embed.setThumbnail("https://i.imgur.com/hvphthX.png");
            channel.sendMessage(embed.build()).queue();

            runGame(msgEvent);
        }
        catch (IOException e)
        {
            logger.error("The topics file is missing or unavailable.");
            msgEvent.getChannel().sendMessage("An error occurred when starting the game.").queue();
        }
    }

    private void runGame(GuildMessageReceivedEvent msgEvent)
    {
        Random r = new Random();
        MessageManager mm = new MessageManager();

        Message[] messages = mm.getNextMessages(channel, (r.nextInt(23)+7)*1000, Integer.MAX_VALUE); //7-30 seconds, as many messages as we can take.

        boolean winnerFound = false;
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Last Word");
        embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setThumbnail("https://i.imgur.com/hvphthX.png");
        try
        {
            for (int i = messages.length-1; i>-1; i--)
            {
                if (messages[i].getContentDisplay().toUpperCase().startsWith(letter))
                {
                    if (CmdUtil.getWordList().contains(messages[i].getContentDisplay().toLowerCase()))
                    {
                        winnerFound = true;
                        embed.setDescription("The victory goes to **"+messages[i].getMember().getEffectiveName()+"** with "+messages[i].getContentDisplay()+"! Congratulations.");
                        break;
                    }
                }
            }
        }
        catch (IOException e)
        {
            for (int i = messages.length-1; i>-1; i--)
            {
                if (messages[i].getContentDisplay().toUpperCase().startsWith(letter))
                {
                    winnerFound = true;
                    embed.setDescription("The victory goes to **"+messages[i].getMember().getEffectiveName()+"** with "+messages[i].getContentDisplay()+"! Congratulations.");
                    break;
                }
            }

        }
        if (!winnerFound)
        {
            embed.setDescription("You guys got nothing? Wow. Nice going.");
        }
        channel.sendMessage(embed.build()).queue();

        endGame(msgEvent, messages);
    }

    private void endGame(GuildMessageReceivedEvent msgEvent, Message[] messages)
    {
        deleteGameChannel(msgEvent, channel);
    }


}
