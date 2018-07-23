package commands.games;

import java.util.Random;

import commands.Command;
import jara.MessageManager;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Countdown extends Command 
{
	TextChannel channel;
	String letters;
	
	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters) 
	{
		System.out.println("CD start");
		channel = super.createGameChannel(msgEvent, msgEvent.getMember().getEffectiveName()+"s-countdown");
		generateLetters(parameters);
	}
	private String generateLetters(String...parameters)
	{
		if (parameters.length > 1)		//If there are several parameters...
		{
			StringBuilder rebuiltParams = new StringBuilder();
			for (int i = 0; i<parameters.length; i++)
			{
				if (parameters[i].matches("[cv]+"))				//Take only the "C" or "V" ones (i.e, ignore /countdown and any other params that may be added)
				{
					rebuiltParams.append(parameters[i]);
				}
				if (rebuiltParams.length() == 9-letters.length())
				{
					break;										//Do not allow any more than 9 selections
				}
			}
			return generateLetters(rebuiltParams.toString());	//Recall the method, now with only one, valid, parameter.
		}
		
		StringBuilder lettersBuilder = new StringBuilder();
		parameters[0] = parameters[0].toLowerCase();
		if (parameters[0].matches("[cv]+"))
		{
            char[] consonants = {'B', 'B', 'C', 'C', 'C', 'D', 'D', 'D', 'D', 'D', 'D', 'F', 'F', 'G', 'G', 'G', 'H', 'H', 'J', 'K', 'L', 'L', 'L', 'L', 'L', 'M', 'M', 'M', 'M', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'P', 'P', 'P', 'P', 'Q', 'R', 'R', 'R', 'R', 'R', 'R', 'R', 'R', 'R', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T', 'V', 'W', 'X', 'Y', 'Z'};
            char[] vowels = {'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'U', 'U', 'U', 'U', 'U', };
			Random r = new Random();
            char[] selections = parameters[0].toCharArray();
            lettersBuilder.append(letters); //Get previous progress.
			for (char selection : selections)
			{
				if (lettersBuilder.length() < 9)
				{
					if (selection == 'v')
					{
						lettersBuilder.append(vowels[r.nextInt(vowels.length)]);
					}
					else if (selection == 'c')
					{
						lettersBuilder.append(consonants[r.nextInt(consonants.length)]);
					}
				}
				else
				{
					break;
				}

			}
			letters = lettersBuilder.toString();
			if (letters.length() == 9)
			{
				return letters;		//All done here!
			}
			channel.sendMessage(letters);
		}
		return generateLetters(new MessageManager().getNextMessage(channel).getContentDisplay()); //If there are still selections missing, get 'em.
	}

}
