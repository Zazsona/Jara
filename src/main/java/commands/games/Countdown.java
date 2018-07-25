package commands.games;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.xml.stream.events.Characters;

import commands.Command;
import jara.Core;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Countdown extends Command 
{
	TextChannel channel;
	String letters = "";
	
	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters) 
	{
		channel = super.createGameChannel(msgEvent, msgEvent.getMember().getEffectiveName()+"s-countdown");
		EmbedBuilder embed = new EmbedBuilder();
		embed.setDescription(("**Welcome to Countdown!**\nTo get started, type 'c' or 'v' into chat to select either a consonant(s) or vowel(s)."));
		embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
		embed.setThumbnail("https://i.imgur.com/KwjqNkH.png");
		channel.sendMessage(embed.build()).queue();
		generateLetters(parameters);
		Message[] answers = getAnswers(msgEvent); 
		generateResults(answers);
		if (!channel.equals(msgEvent.getChannel())) //Basically, if this is a game channel...
		{
			channel.sendMessage("Well played! This channel will be deleted in 30 seconds.").queue();
			try 
			{
				Thread.sleep(30*1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace(); 
			}
			channel.delete().queue();
		}
		return;
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
            ArrayList<Character> consonants = new ArrayList<Character>();
            consonants.addAll(Arrays.asList('B', 'B', 'C', 'C', 'C', 'D', 'D', 'D', 'D', 'D', 'D', 'F', 'F', 'G', 'G', 'G', 'H', 'H', 'J', 'K', 'L', 'L', 'L', 'L', 'L', 'M', 'M', 'M', 'M', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'P', 'P', 'P', 'P', 'Q', 'R', 'R', 'R', 'R', 'R', 'R', 'R', 'R', 'R', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'S', 'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T', 'T', 'V', 'W', 'X', 'Y', 'Z'));
            ArrayList<Character> vowels = new ArrayList<Character>();
            vowels.addAll(Arrays.asList('A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O', 'U', 'U', 'U', 'U', 'U')); //By having duplicate letters here we can influence the odds of one coming up. 
			Random r = new Random();
            char[] selections = parameters[0].toCharArray();
            lettersBuilder.append(letters); //Get previous progress.
			for (char selection : selections)
			{
				if (lettersBuilder.length() < 9)
				{
					if (selection == 'v')
					{
						int index = r.nextInt(vowels.size());
						lettersBuilder.append(vowels.get(index));		//We remove the chars as this changes the odds, as with the cards on the show.
						vowels.remove(index);
					}
					else if (selection == 'c')
					{
						int index = r.nextInt(consonants.size());
						lettersBuilder.append(consonants.get(index));
						consonants.remove(index);
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
			EmbedBuilder embed = new EmbedBuilder();
			embed.setDescription(createBoard());
			embed.setColor(Core.getHighlightColour(channel.getGuild().getSelfMember()));
			channel.sendMessage(embed.build()).queue();
		}
		return generateLetters(new MessageManager().getNextMessage(channel).getContentDisplay()); //If there are still selections missing, get 'em.
	}
	private Message[] getAnswers(GuildMessageReceivedEvent msgEvent)
	{
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
		embed.setDescription("You've got 30 seconds - On your marks, get set, go!\n\n**"+createBoard()+"**");
		embed.setThumbnail("https://i.imgur.com/0uNRZWG.png");
		Message embedMsg = channel.sendMessage(embed.build()).complete();
		MessageManager mm = new MessageManager();
		Thread clockThread = new Thread(new clock(embed, embedMsg));
		clockThread.start();
		Message[] answers = mm.getNextMessages(channel, 35*1000, Integer.MAX_VALUE); 
		return answers;
	}
	private void generateResults(Message[] answers)
	{
 		if (answers.length == 0)
		{
			channel.sendMessage("Looks like nobody got anything! Better luck next time.").queue();
		}
		else
		{
			String[] winnerData = {"", ""}; //Name, Word - These are blank so that length checks do not throw a null exception for the first message.
			for (Message answer : answers)	
			{
				String content = answer.getContentDisplay();
				if (content.length() > 9)
				{
					continue; //Most likely just a bot message, anyway.
				}
				List<Character> answerLetters = content.toUpperCase().chars().mapToObj((letter) -> Character.valueOf((char) letter)).collect(Collectors.toList()); //Converting each Int returns from chars() into a Character, as char[] cannot be converted into Character[]
				for (char letter : letters.toCharArray())
				{
					answerLetters.remove(Character.valueOf(letter));		//TODO: Make this more efficient, if possible.
				}
				
				if (answerLetters.size() == 0)
				{
					if (content.length() > winnerData[1].length()) //The result of all this, is that the first person with the longest word wins.
					{
						//TODO: Check if it's a word.
						winnerData[0] = answer.getMember().getEffectiveName();
						winnerData[1] = content;
					}
				}
			}
			if (!winnerData[0].equals(""))
			{
				EmbedBuilder embed = new EmbedBuilder();
				embed.setColor(Core.getHighlightColour(answers[0].getGuild().getSelfMember()));
				winnerData[1] = winnerData[1].toLowerCase();
				if (isRude(winnerData[1]))
				{
					embed.setDescription("The scores are in, and this game's *dirty minded* Countdown winner is...\n\n**"+winnerData[0]+"** with their **"+winnerData[1].length()+"** letter word from the deepest of gutters, **"+winnerData[1]+"!**");
					embed.setThumbnail("https://i.imgur.com/A9QBiiR.png");
				}
				else
				{
					embed.setDescription("The scores are in, and this game's Countdown winner is...\n\n**"+winnerData[0]+"** with their **"+winnerData[1].length()+"** letter word, **"+winnerData[1]+"!**");
					embed.setThumbnail("https://i.imgur.com/scKHMRb.png");
				}
				channel.sendMessage(embed.build()).queue();
			}
			else
			{
				channel.sendMessage("Looks like nobody got it quite right. Better luck next time!").queue(); //The muppets didn't give any proper answers.
			}

		}

	}
	private class clock implements Runnable
	{
		EmbedBuilder embed;
		Message msg;
		public clock(EmbedBuilder embed, Message msg)
		{
			this.embed = embed;
			this.msg = msg;
		}
		@Override
		public void run() 
		{
			try
			{
				Thread.sleep(7500);
				embed.setThumbnail("https://i.imgur.com/llmbGHa.png");
				msg.editMessage(embed.build()).queue();
				Thread.sleep(7500);
				embed.setThumbnail("https://i.imgur.com/JpwwNrY.png");
				msg.editMessage(embed.build()).queue();
				Thread.sleep(7500);
				embed.setThumbnail("https://i.imgur.com/F2deeEY.png");
				msg.editMessage(embed.build()).queue();
				Thread.sleep(7500);
				embed.setThumbnail("https://i.imgur.com/eMrORg8.png");
				msg.editMessage(embed.build()).queue();
				embed = new EmbedBuilder();
				embed.setColor(Core.getHighlightColour(channel.getGuild().getSelfMember()));
				embed.setThumbnail("https://i.imgur.com/3SUuzD1.png");
				embed.setDescription("Time's up! You've got 5 seconds to state your word!");
				channel.sendMessage(embed.build()).queue();
			}
			catch (InterruptedException e)
			{
				channel.sendMessage("Uh-oh! Looks like something went wrong. You won't get a warning when time's almost up.").queue();
				e.printStackTrace();
			}

		}
		
	}
	private String createBoard()
	{
		StringBuilder boardBuilder = new StringBuilder();
		for (char letter : letters.toLowerCase().toCharArray())
		{
			boardBuilder.append(":regional_indicator_"+letter+":");
		}
		return boardBuilder.toString();
	}
	private boolean isRude(String answer)
	{
		if (answer.contains("piss") || answer.equals("poo") || answer.contains("poop") ||  answer.equals("pee") || answer.equals("butt") || answer.contains("butts") || answer.contains("fuck") || answer.contains("shit") || answer.contains("arse") || answer.contains("bugger") || answer.contains("bollocks") || answer.contains("bugger") || answer.contains("ass") || answer.contains("crap") || answer.contains("bitch") || answer.contains("bastard") || answer.contains("cunt") || answer.contains("twat") || answer.contains("boobs") || answer.equals("tits") || answer.equals("tit") || answer.contains("bellend") || answer.contains("cock") || answer.contains("clunge") || answer.contains("knob") || answer.contains("minge") || answer.contains("prick") || answer.contains("dildo") || answer.contains("jizz") || answer.contains("slag") || answer.contains("slut") || answer.contains("whore") || answer.contains("shag") || answer.equals("sex") || answer.contains("knob") || answer.contains("wank"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
