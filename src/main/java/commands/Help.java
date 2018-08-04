package commands;

import java.awt.Color;
import java.util.ArrayList;

import jara.CommandAttributes;
import jara.CommandRegister;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Help extends Command {

	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters) 
	{
		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor("Help Menu", null, null);
		embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
		if (parameters.length == 1)
		{

			embed.setDescription("To get a list of commands, use /help [Topic]. For more information, use /about\n");
			String topics = 
					"**Games** - SP/MP experiences to keep you busy!\n"
					+ "**Toys** - Quick fun commands.\n"
					+ "**Utility** - Small commands for basic applications.\n"
					+ "**Audio** - Commands for Voice Channels.\n"
					+ "**Admin** - Tools to modify the bot.";
			
			embed.addField("Topics",topics, true);
		}
		else if (parameters.length == 2)
		{
			if (parameters[1].equalsIgnoreCase("games"))
			{
				embed.setDescription(buildCommandList(CommandRegister.GAMES));
			}
			else if (parameters[1].equalsIgnoreCase("toys"))
			{
				embed.setDescription(buildCommandList(CommandRegister.TOYS));
			}
			else if (parameters[1].equalsIgnoreCase("utility"))
			{
				embed.setDescription(buildCommandList(CommandRegister.UTILITY));
			}
			else if (parameters[1].equalsIgnoreCase("audio"))
			{
				embed.setDescription(buildCommandList(CommandRegister.AUDIO));
			}
			else if (parameters[1].equalsIgnoreCase("admin"))
			{
				embed.setDescription(buildCommandList(CommandRegister.ADMIN)); //TODO: Limit this to those who have access?
			}
			else
			{
				embed.setDescription(buildEmbedDesc(parameters[1]));
			}

		}
		msgEvent.getChannel().sendMessage(embed.build()).queue();
	}
	private ArrayList<String> getCommandExplanation(String key)
	{
		/**
		 * Yay! Help menus. exciting stuff, but someone's gotta do 'em.
		 * 
		 * Anyway, here's my recommended format for consistency:
		 *
		 * Displaying parameters:
		 * Put these inside the paramSetups arraylist, and lay them out so that it looks like...
		 * 	
		 * /CommandTrigger [Parameter1] (Parameter2)
		 * 
		 * CommandTrigger = The string used to activate the command, this should be the key.
		 * [Parameter1] - Square brackets indicate a required parameter
		 * (Parameter2) - Circle brackets indicate an optional parameter
		 * If there are no parameters, you can leave this empty.
		 * 
		 * Displaying explanation:
		 * Put this in the second dimension, and write whatever, really. Just a basic paragraph saying what do & how do.
		 * 
		 */
		ArrayList<String> paramSetups = new ArrayList<String>();
		StringBuilder infoBuilder = new StringBuilder();
		if (key.equalsIgnoreCase("Ping"))
		{
			infoBuilder.append("Simple ping command. The bot will reply with a quick 'Pong!'");
		}
		else if (key.equalsIgnoreCase("Report"))
		{
			infoBuilder.append("Lists all current working stats for the bot, handy for fixing issues or the curious.");
		}
		else if (key.equalsIgnoreCase("About"))
		{
			infoBuilder.append("Displays details about the Jara project, the base for this bot.");
		}
		else if (key.equalsIgnoreCase("EightBall"))
		{
			paramSetups.add("/EightBall (Question about the future)");
			infoBuilder.append("Unsure about what the future holds? Consult the eight ball to get the answers you seek.");
		}
		else if (key.equalsIgnoreCase("Jokes"))
		{
			infoBuilder.append("Fancy a laugh? This command will tell a joke.");
		}
		else if (key.equalsIgnoreCase("CoinFlip"))
		{
			infoBuilder.append("Flip a coin for a quick decision. It'll come up heads or tails.");
		}
		else if (key.equalsIgnoreCase("Config"))
		{
			paramSetups.add("/config");
			paramSetups.add("/config SetGameCategory (Category Name)");
			paramSetups.add("/config RemGameCategory");
			paramSetups.add("/config enable [Command]");
			paramSetups.add("/config disable [Command]");
			paramSetups.add("/config AddRole [Command] [Role Name]");
			paramSetups.add("/config RemRole [Command] [Role Name]");
			infoBuilder.append("This is the primary command for customising the bot for your guild. Use /config to view the current configuration.\n"
					+ "\n"
					+ "Options:\n"
					+ "Enable/Disable - This sets whether a command can be used at all. Using a disabled command will simply return a message.\n"
					+ "AddRole/RemRole - Defines which Discord roles can use the command.\n"
					+ "Set/RemGameCategory - Setting a game category means the bot will create a new channel in the category for each individual game. This helps reduce clutter in the channel the command was used in and allows users to easily see when and what games are currently on-going.\n"
					+ "\n"
					+ "NOTE: To select all commands or roles, use a *. (For roles this is the same as using the everyone role)"
					);
		}
		else if (key.equalsIgnoreCase("Countdown"))
		{
			paramSetups.add("/Countdown");
			paramSetups.add("/Countdown (List of selections)");
			paramSetups.add("/Countdown (selection1) (selection2) ... (selection9)");
			infoBuilder.append("Try your skills at the classic word game: Countdown!\n"
					+ "\n"
					+ "To win, you must create the longest word you can come up with using just the letters generated.\n"
					+ "\n"
					+ "To generate the letters, you can enter 'c' for a consonant, or 'v' for a vowel 9 times.");
		}
		else if (key.equalsIgnoreCase("Help"))
		{
			paramSetups.add("/Help");
			paramSetups.add("/Help (Command)");
			paramSetups.add("/Help (Category)");
			infoBuilder.append(":thinking:");
		}
		else if (key.equalsIgnoreCase("Randomiser"))
        {
            paramSetups.add("/Randomiser [Option1] [Option2] ... [OptionN]");
            infoBuilder.append("Randomly selects an item from the list.");
        }
        else if (key.equalsIgnoreCase("Say"))
		{
			paramSetups.add("/Say [Sentence]");
			infoBuilder.append("Have the bot repeat a sentence.");
		}
		else
		{
			infoBuilder.append("No information is available for this command.");
		}
		paramSetups.add(0, infoBuilder.toString()); //To save creating a new arraylist with duplicate data, we'll just 'borrow' this one. The first index is always the info.
		return paramSetups;
	}
	private String buildEmbedDesc(String alias)
	{
		
		StringBuilder cmdDescBuilder = new StringBuilder();
		CommandAttributes cmdAttributes = CommandRegister.getCommand(alias);
		if (cmdAttributes == null)
		{
			cmdDescBuilder.append("That command doesn't exist.");
		}
		else
		{
			cmdDescBuilder.append("~~------------------------------------------------------------~~\n");
			cmdDescBuilder.append("**Command:** "+cmdAttributes.getCommandKey()+"\n");
			StringBuilder aliasBuilder = new StringBuilder();
			for (String regAlias : cmdAttributes.getAliases())
			{
				aliasBuilder.append(regAlias+", ");
			}
			aliasBuilder.setLength(aliasBuilder.length()-2);
			cmdDescBuilder.append("**Aliases:** "+aliasBuilder.toString()+"\n");	
			ArrayList<String> explanationList = getCommandExplanation(cmdAttributes.getCommandKey());
			if (explanationList.size() > 1)
			{
				cmdDescBuilder.append("**Parameters: **\n");
				for (int i = 1; i<explanationList.size(); i++) //Start from 1, so we skip info.
				{
					cmdDescBuilder.append(explanationList.get(i)+"\n");
				}
			}
			cmdDescBuilder.append("~~------------------------------------------------------------~~\n");
			cmdDescBuilder.append("**Info:**\n"+explanationList.get(0)+"\n");
			cmdDescBuilder.append("~~------------------------------------------------------------~~\n");
		}
		return cmdDescBuilder.toString();
	}
	private String buildCommandList(int categoryID)
	{
		StringBuilder commands = new StringBuilder();
		commands.append("~~------------------------------------------------------------~~\n");
		for (CommandAttributes cmdAttributes : CommandRegister.getCommandsInCategory(categoryID))
		{
			commands.append(cmdAttributes.getCommandKey()+"\n");
		}
		commands.append("~~------------------------------------------------------------~~\n");
		return commands.toString();
	}

}