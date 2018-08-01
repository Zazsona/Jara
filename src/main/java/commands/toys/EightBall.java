package commands.toys;
import java.util.Random;

import commands.Command;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class EightBall extends Command
{
	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
	{
        String[] Statements = { "It is certain.", "It is decidedly so.", "Without a doubt.", "Yes, definitely.", "You may rely on it.", "As I see it, yes.", "Most likely.", "Outlook good.", "Yes.", "Signs point to yes.", "Reply hazy. Try again.", "Ask again later.", "Better not tell you now.", "Cannot predict now.", "Concentrate and ask again.", "Don't count on it.", "My reply is no.", "My sources say no.", "Outlook not so good.", "Very doubtful." };
        Random r = new Random();
        msgEvent.getChannel().sendMessage(Statements[r.nextInt(Statements.length)]).queue();
	}
	
}
