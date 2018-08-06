package commands.config;

import java.util.List;

import commands.Command;
import configuration.GlobalSettingsManager;
import configuration.GuildSettingsManager;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Config extends Command {
	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
	{
		if (parameters.length > 1)
		{
			if (parameters[1].equalsIgnoreCase("setgamecategory"))
			{
				if (parameters.length >= 3)
				{
					setGameCategory(msgEvent, parameters);
				}
			}
			else if (parameters[1].equalsIgnoreCase("remgamecategory"))
			{
				setGameCategory(msgEvent);
			}
			else if (parameters[1].equalsIgnoreCase("enable") || parameters[1].equalsIgnoreCase("disable"))
			{
				if (parameters.length == 3)
				{
					setStatus(msgEvent, parameters);
				}
				else
				{
					msgEvent.getChannel().sendMessage("Insufficient parameters.").queue();
				}

			}
			else if (parameters[1].equalsIgnoreCase("addrole") || parameters[1].equalsIgnoreCase("remrole"))
			{
				if (parameters.length == 4)
				{
					setPermissions(msgEvent, parameters);
				}
				else
				{
					msgEvent.getChannel().sendMessage("Insufficient parameters.").queue();
				}

			}
			else
			{
				msgEvent.getChannel().sendMessage("Config option not recognised.").queue();
			}
		}
		else
		{
			displayConfig(msgEvent);
		}
	}
	private void setPermissions(GuildMessageReceivedEvent msgEvent, String... parameters) 
	{
		List<Role> roles = msgEvent.getGuild().getRolesByName(parameters[3], true);
		if (roles.isEmpty())
		{
			if (parameters[3].equalsIgnoreCase("everyone") || parameters[3].equals("*"))
			{
				roles.add(msgEvent.getGuild().getPublicRole());
			}
			else
			{
				msgEvent.getChannel().sendMessage("Could not find a role by that name.").queue();
				return;
			}
		}
		String roleID = roles.get(0).getId();
		if (parameters[2].equals("*"))
		{
			GuildSettingsManager guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId());
			for (String key : GlobalSettingsManager.getGlobalEnabledCommandKeys())
			{
				if (parameters[1].equalsIgnoreCase("addrole"))
				{
					guildSettings.addPermittedRole(key, roleID);
				}
				else
				{
					guildSettings.removePermittedRole(key, roleID);
				}
			}
			msgEvent.getChannel().sendMessage(roles.get(0).getName().replace("@", "")+" can now use all enabled commands!").queue();	
		}
		else
		{
			for (String key : GlobalSettingsManager.getGlobalEnabledCommandKeys())
			{
				if (parameters[2].equalsIgnoreCase(key))
				{
					GuildSettingsManager guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId());
					if (parameters[1].equalsIgnoreCase("addrole"))
					{
						guildSettings.addPermittedRole(key, roleID);
					}
					else
					{
						guildSettings.removePermittedRole(key, roleID);
					}
					msgEvent.getChannel().sendMessage(key+" permissions updated!").queue();	
				}
			}
		}

	}
	private void setStatus(GuildMessageReceivedEvent msgEvent, String... parameters) 
	{
		if (parameters[2].equals("*"))
		{
			GuildSettingsManager guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId());
			for (String key : GlobalSettingsManager.getGlobalEnabledCommandKeys())
			{

				if (parameters[1].equalsIgnoreCase("enable"))
				{
					guildSettings.setGuildCommandEnabledStatus(key, true);
				}
				else
				{
					if (!(key.equalsIgnoreCase("config") || key.equalsIgnoreCase("about") || key.equalsIgnoreCase("help"))) //If it is NOT any of these...
					{
						guildSettings.setGuildCommandEnabledStatus(key, false);
					}
				}
			}
			msgEvent.getChannel().sendMessage("All commands have been "+parameters[1].toLowerCase()+"d").queue(); //Felt like being lazy here. para[1] is always either "enable"/"disable", this makes it "enabled" or "disabled" in text.
		}
		else
		{
			for (String key : GlobalSettingsManager.getGlobalEnabledCommandKeys())
			{
				if (parameters[2].equalsIgnoreCase(key))
				{
					GuildSettingsManager guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId());
					if (parameters[1].equalsIgnoreCase("enable"))
					{
						guildSettings.setGuildCommandEnabledStatus(key, true);
					}
					else
					{
						if (key.equalsIgnoreCase("config") || key.equalsIgnoreCase("about") || key.equalsIgnoreCase("help"))
						{
							msgEvent.getChannel().sendMessage(key + " cannot be disabled.").queue();
							return;
						}
						guildSettings.setGuildCommandEnabledStatus(key, false);
					}
					msgEvent.getChannel().sendMessage(key+" status updated!").queue();	
					return;
				}
			}
			msgEvent.getChannel().sendMessage("Command not found.").queue();
		}

	}
	private void setGameCategory(GuildMessageReceivedEvent msgEvent, String...parameters)
	{

		String categoryID;
		if (parameters.length > 2)
		{
			StringBuilder categoryNameBuilder = new StringBuilder();
			for (int i = 2; i<parameters.length; i++)
			{
				categoryNameBuilder.append(parameters[i]+" ");
			}
			categoryNameBuilder.deleteCharAt(categoryNameBuilder.length()-1);
			String categoryName = categoryNameBuilder.toString();

			categoryID = msgEvent.getGuild().getCategoriesByName(categoryName, true).get(0).getId();
			if (categoryID.equals(""))
			{
				msgEvent.getChannel().sendMessage("Category not found.").queue();
				return;
			}
		}
		else
		{
			categoryID = "";
		}

		GuildSettingsManager guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId());
		guildSettings.setGuildGameCategoryID(categoryID);
		if (categoryID.equals(""))
		{
			msgEvent.getChannel().sendMessage("Game category removed.").queue();
		}
		else
		{
			msgEvent.getChannel().sendMessage("Game category set to "+msgEvent.getGuild().getCategoryById(categoryID).getName()).queue();
		}

	}
	private void displayConfig(GuildMessageReceivedEvent msgEvent)
	{
		GuildSettingsManager guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId());
		String gameCategory;
		try
		{
			gameCategory = msgEvent.getGuild().getCategoryById(guildSettings.getGuildGameCategoryID()).getName();
		}
		catch (IllegalArgumentException e)
		{
			gameCategory = "Not set";
		}
		EmbedBuilder embed = new EmbedBuilder();
		embed.setDescription("**Commands**\n"
				+ "/config SetGameCategory (Category Name)\n"
				+ "/config RemGameCategory\n"
				+ "/config enable [Command]\n"
				+ "/config disable [Command]\n"
				+ "/config AddRole [Command] [Role Name]\n"
				+ "/config RemRole [Command] [Role Name]\n"
				+ "\n"
				+ "**Game Category**: "+gameCategory);
				
		StringBuilder keyList = new StringBuilder();
		StringBuilder enabledList = new StringBuilder();
		StringBuilder roleList = new StringBuilder();
		for (String key : GlobalSettingsManager.getGlobalEnabledCommandKeys()) 
		{
			keyList.append(key+"\n");
			if (guildSettings.getGuildCommandEnabledStatus(key))
			{
				enabledList.append("✓\n");
			}
			else
			{
				enabledList.append("X\n");
			}
			for (String roleID : guildSettings.getPermittedRoles(key))
			{
				roleList.append(msgEvent.getGuild().getRoleById(roleID).getName().replace("@", "")+", "); //Removing @ here as getName appends it for the everyone role, causing a ping.
			}
			if (roleList.lastIndexOf(", ") == roleList.length()-2)
			{
				roleList.setLength(roleList.length()-2); //Remove last ', '
			}
			roleList.append("\n");
		}
		embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
		embed.setTitle("===================== Config ======================");
		embed.addField("Command", keyList.toString(), true);
		embed.addField("Enabled", enabledList.toString(), true);
		embed.addField("Permissions", roleList.toString(), true);
		msgEvent.getChannel().sendMessage(embed.build()).queue();
	}

}
