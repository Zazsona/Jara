package commands;

import java.util.ArrayList;
import java.util.Collections;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import jara.CommandAttributes;
import jara.CommandRegister;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import static jara.CommandRegister.Category.*;

@SuppressWarnings("SpellCheckingInspection")
public class Help extends Command {

	/**
	 * The command prefix for this guild
	 */
	private String prefix;

	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters) 
	{
		prefix = SettingsUtil.getGuildCommandPrefix(msgEvent.getGuild().getId()).toString();
		EmbedBuilder embed = new EmbedBuilder();
		embed.setAuthor("Help Menu", null, null);
		embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
		if (parameters.length == 1)
		{
			embed.setDescription("To get a list of commands, use "+prefix+"help [Topic]. For more information, use "+prefix+"about\n" +
										 "By default, only commands you have access to are shown. use \""+prefix+"help [Topic] all\" to see every command.");
			String topics = 
					"**Games** - SP/MP experiences to keep you busy!\n"
					+ "**Toys** - Quick fun commands.\n"
					+ "**Utility** - Small commands for basic applications.\n"
					+ "**Audio** - Commands for Voice Channels.\n"
					+ "**Admin** - Tools to modify the bot.";
			
			embed.addField("Topics",topics, true);
		}
		else if (parameters.length >= 2)
		{
			boolean limitToPerms = !msgEvent.getMember().isOwner();
			ArrayList<String> roleIDs = new ArrayList<>();
			if (parameters.length == 3)
			{
				if (parameters[2].equalsIgnoreCase("all"))
				{
					limitToPerms = false;									//If the user sets "all" display every command, regardless of permissions
				}
			}
			if (limitToPerms)
			{
				for (Role role : msgEvent.getMember().getRoles())			//Otherwise, collect their roles and base it off that.
				{
					roleIDs.add(role.getId());
				}
				roleIDs.add(msgEvent.getGuild().getId()); //This adds the everyone role
			}

			if (parameters[1].equalsIgnoreCase("games"))
			{
				embed.setDescription(buildCommandList(GAMES, msgEvent.getGuild().getId(), roleIDs, limitToPerms));
			}
			else if (parameters[1].equalsIgnoreCase("toys"))
			{
				embed.setDescription(buildCommandList(TOYS, msgEvent.getGuild().getId(), roleIDs, limitToPerms));
			}
			else if (parameters[1].equalsIgnoreCase("utility"))
			{
				embed.setDescription(buildCommandList(UTILITY, msgEvent.getGuild().getId(), roleIDs, limitToPerms));
			}
			else if (parameters[1].equalsIgnoreCase("audio"))
			{
				embed.setDescription(buildCommandList(AUDIO, msgEvent.getGuild().getId(), roleIDs, limitToPerms));
			}
			else if (parameters[1].equalsIgnoreCase("admin"))
			{
				embed.setDescription(buildCommandList(ADMIN, msgEvent.getGuild().getId(), roleIDs, limitToPerms));
			}
			else
			{
				embed.setDescription(buildEmbedDesc(parameters[1], msgEvent.getGuild().getId()));
			}

		}
		msgEvent.getChannel().sendMessage(embed.build()).queue();
	}

	/**
	 * Returns the parameters and instructions for a command.
	 * @param key The command key
	 * @return Command parameters and info. Index 0 contains the instructions
	 */
	private ArrayList<String> getCommandExplanation(String key, String guildID)
	{
		/*
		 * Yay! Help menus. exciting stuff, but someone's gotta do 'em.
		 * 
		 * Anyway, here's my recommended format for consistency:
		 *
		 * Displaying parameters:
		 * Put these inside the paramSetups array list, and lay them out so that it looks like...
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
		ArrayList<String> paramSetups = new ArrayList<>();
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
			paramSetups.add(prefix+"EightBall (Question about the future)");
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
			paramSetups.add(prefix+"config");
			paramSetups.add(prefix+"config check [Command]/[Command Category]/GameCategory");
			paramSetups.add(prefix+"config SetGameCategory (Channel Category Name)");
			paramSetups.add(prefix+"config RemGameCategory");
			paramSetups.add(prefix+"config enable [Command]");
			paramSetups.add(prefix+"config disable [Command]");
			paramSetups.add(prefix+"config AddRole [Command] [Role Name]");	//TODO: Woefully outdated
			paramSetups.add(prefix+"config RemRole [Command] [Role Name]");
			infoBuilder.append("This is the primary command for customising the bot for your guild. Use ")
					.append(prefix).append("config check * to view the full current configuration.\n")
					.append("\n").append("Options:\n")
					.append("Check - Displays settings the specified item\n")
					.append("Enable/Disable - This sets whether a command can be used at all. Using a disabled command will simply return a message.\n")
					.append("AddRole/RemRole - Defines which Discord roles can use the command.\n")
					.append("Set/RemGameCategory - Setting a game category means the bot will create a new channel in the category for each individual game. This helps reduce clutter in the channel the command was used in and allows users to easily see when and what games are currently on-going.\n"
					).append("\n")
					.append("NOTE: To select all commands or roles, use a *. (For roles this is the same as using the everyone role)");
		}
		else if (key.equalsIgnoreCase("Countdown"))
		{
			paramSetups.add(prefix+"Countdown");
			paramSetups.add(prefix+"Countdown (List of selections)");
			paramSetups.add(prefix+"Countdown (selection1) (selection2) ... (selection9)");
			infoBuilder.append("Try your skills at the classic word game: Countdown!\n"
					+ "\n"
					+ "To win, you must create the longest word you can come up with using just the letters generated.\n"
					+ "\n"
					+ "To generate the letters, you can enter 'c' for a consonant, or 'v' for a vowel 9 times.");
		}
		else if (key.equalsIgnoreCase("CountdownConundrum"))
		{
			paramSetups.add(prefix+"CountdownConundrum");
			infoBuilder.append("Fancy yourself a wordsmith? Try to figure out the nine letter word from the anagram.");
		}
		else if (key.equalsIgnoreCase("Hangman"))
		{
			paramSetups.add(prefix+"Hangman");
			infoBuilder.append("Guess the word by entering each letter at a time to save Mr.Unfortunate.\nBe careful, though. Guess wrong eight times and it's game over.");
		}
		else if (key.equalsIgnoreCase("Help"))
		{
			paramSetups.add(prefix+"Help");
			paramSetups.add(prefix+"Help (Command)");
			paramSetups.add(prefix+"Help (Category)");
			infoBuilder.append(":thinking:");
		}
		else if (key.equalsIgnoreCase("Randomizer"))
        {
            paramSetups.add(prefix+"Randomizer [Option1] [Option2] ... [OptionN]");
            infoBuilder.append("Randomly selects an item from the list.");
        }
        else if (key.equalsIgnoreCase("Say"))
		{
			paramSetups.add(prefix+"Say [Sentence]");
			infoBuilder.append("Have the bot repeat a sentence.");
		}
		else if (key.equalsIgnoreCase("Timecard"))
		{
			infoBuilder.append("Displays a Timecard from Spongebob Squarepants. In some cases may result in comedic scenarios.");
		}
		else if (key.equalsIgnoreCase("WouldYouRather"))
		{
			infoBuilder.append("Simple \"Would you rather?\" question.\nNote: NSFW questions will only appear in NSFW channels.");
		}
		else if (key.equalsIgnoreCase("IsItWednesdayMyDudes")) //C'mon. This one should be pretty bloody obvious.
		{
			infoBuilder.append("This either tells you if it is Wednesday, or deploys a team of super spies. I'll leave it up to you to figure out which.");
		}
		else if (key.equalsIgnoreCase("Play"))
		{
			paramSetups.add(prefix+"Play [Link]");
			infoBuilder.append("Plays the track at the link. Supports:\nYouTube\nSoundCloud\nBandcamp\nVimeo\nTwitch\nHTTP URLs");
		}
		else if (key.equalsIgnoreCase("Skip"))
		{
			infoBuilder.append("Votes to skip the current playing track.");
		}
		else if (key.equalsIgnoreCase("NowPlaying"))
		{
			infoBuilder.append("Gives some information about the currently playing track.");
		}
		else if (key.equalsIgnoreCase("ForceSkip"))
		{
			infoBuilder.append("Skips the current track immediately without a vote.");
		}
		else if (key.equalsIgnoreCase("Replay"))
		{
			infoBuilder.append("Adds the current playing track onto the queue again.");
		}
		else if (key.equalsIgnoreCase("Pause"))
		{
			infoBuilder.append("Pauses/Resumes the current track.");
		}
		else
		{
			if (CommandRegister.getCommand(key) == null)
			{
				infoBuilder.append("**Custom Server Command**\n");
				infoBuilder.append(SettingsUtil.getGuildSettings(guildID).getCustomCommandAttributes(key).getDescription());
			}
			else
			{
				infoBuilder.append("No information is available for this command.");
			}

		}
		paramSetups.add(0, infoBuilder.toString()); //To save creating a new array list with duplicate data, we'll just 'borrow' this one. The first index is always the info.
		return paramSetups;
	}

	/**
	 * Builds the page for a specific command's details
	 * @param alias A command alias
	 * @return The command help page
	 */
	private String buildEmbedDesc(String alias, String guildID)
	{
		StringBuilder cmdDescBuilder = new StringBuilder();
		CommandAttributes cmdAttributes = CommandRegister.getCommand(alias);
		if (cmdAttributes == null) //Maybe it's a custom command?
		{
			cmdAttributes = SettingsUtil.getGuildSettings(guildID).getCustomCommandAttributes(alias);
			if (cmdAttributes == null) //Nope, it's definitely someone being a smart arse.
			{
				cmdDescBuilder.append("That command doesn't exist.");
				return null;
			}

		}
		cmdDescBuilder.append("~~------------------------------------------------------------~~\n");
		cmdDescBuilder.append("**Command:** ").append(cmdAttributes.getCommandKey()).append("\n");
		StringBuilder aliasBuilder = new StringBuilder();
		for (String regAlias : cmdAttributes.getAliases())
		{
			aliasBuilder.append(regAlias).append(", ");
		}
		aliasBuilder.setLength(aliasBuilder.length()-2);
		cmdDescBuilder.append("**Aliases:** ").append(aliasBuilder.toString()).append("\n");

		ArrayList<String> explanationList = getCommandExplanation(cmdAttributes.getCommandKey(), guildID);
		if (explanationList.size() > 1)
		{
			cmdDescBuilder.append("**Parameters: **\n");
			for (int i = 1; i<explanationList.size(); i++) //Start from 1, so we skip info.
			{
				cmdDescBuilder.append(explanationList.get(i)).append("\n");
			}
		}
		cmdDescBuilder.append("~~------------------------------------------------------------~~\n");
		cmdDescBuilder.append("**Info:**\n").append(explanationList.get(0)).append("\n");
		cmdDescBuilder.append("~~------------------------------------------------------------~~\n");
		return cmdDescBuilder.toString();
	}

	/**
	 * Generates Help list of all commands and custom commands in the specified category.
	 *
	 * limitToPerms is used here to bypass the permissions check.<br>
	 * The reason this check exists is not to hide the existence of the commands from the user, but rather so that<br>
	 * they do not have to trawl through a list of commands where they can only use some of them, which could be an issue as the command count grows.<br>
	 * <br>
	 * TL;DR, the permission check is for an easier experience, not secrecy. Consider this when setting limitToPerms as true.
	 *
	 * @param categoryID The category to list
	 * @param guildID The guild to check permissions for
	 * @param roleIDs The roles to limit commands to
	 * @param limitToPerms Whether to limit shown commands to roles or not
	 * @return The command list
	 */
	private String buildCommandList(CommandRegister.Category categoryID, String guildID, ArrayList<String> roleIDs, boolean limitToPerms)
	{
		GuildSettings guildSettings = SettingsUtil.getGuildSettings(guildID);
		StringBuilder commands = new StringBuilder();
		boolean commandsListed = false;

		ArrayList<CommandAttributes> cas = new ArrayList<>();
		Collections.addAll(cas, CommandRegister.getCommandsInCategory(categoryID));
		for (String key : guildSettings.getCustomCommandMap().keySet())
		{
			if (guildSettings.getCustomCommandAttributes(key).getCategoryID() == categoryID)
			{
				cas.add(guildSettings.getCustomCommandAttributes(key));
			}
		}


		commands.append("~~------------------------------------------------------------~~\n");
		for (CommandAttributes cmdAttributes : cas)
		{
			try //Spaghet TODO: Not spaghet
			{
				if (guildSettings.isCommandEnabled(cmdAttributes.getCommandKey()) && SettingsUtil.getGlobalSettings().isCommandEnabled(cmdAttributes.getCommandKey()))
				{
					if (!(Collections.disjoint(guildSettings.getPermissions(cmdAttributes.getCommandKey()), roleIDs)) || !limitToPerms)
					{
						commands.append("**").append(cmdAttributes.getCommandKey()).append("** - ").append(cmdAttributes.getDescription()).append("\n");
						commandsListed = true;
					}
				}
			}
			catch (NullPointerException e) //Custom command
			{
				if (guildSettings.isCommandEnabled(cmdAttributes.getCommandKey()))
				{
					if (!(Collections.disjoint(guildSettings.getPermissions(cmdAttributes.getCommandKey()), roleIDs)) || !limitToPerms)
					{
						commands.append("**").append(cmdAttributes.getCommandKey()).append("** - ").append(cmdAttributes.getDescription()).append("\n");
						commandsListed = true;
					}
				}
			}
		}
		if (!commandsListed)
		{
			commands.append("You don't have permission to use any of these commands. Use `").append(prefix).append("help [Category] all` to see all enabled commands. \n");
		}
		commands.append("~~------------------------------------------------------------~~\n");

		return commands.toString();
	}

}
