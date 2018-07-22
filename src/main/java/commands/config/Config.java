package commands.config;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import commands.Command;
import configuration.GlobalSettingsManager;
import configuration.GuildSettingsManager;
import jara.CommandRegister;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Config extends Command {
	//TODO: Perhaps incorporate a unique superclass for each category. This could also be used with generics for easy mass selecton (All games, all config, etc.)
	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
	{
		if (parameters[1].equalsIgnoreCase("setgamecategory"))
		{
			if (parameters.length == 3)
			{
				setGameCategory(parameters[2], msgEvent);
			}
		}
		else if (parameters[i].equalsIgnoreCase("remgamecategory"))
		{
			setGameCategory("", msgEvent);
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
			displayConfig(msgEvent);
		}
		//TODO: Limit which commands can be disabled (Help, Config)
	}
	private void setPermissions(GuildMessageReceivedEvent msgEvent, String... parameters) 
	{
		List<Role> roles = msgEvent.getGuild().getRolesByName(parameters[3], true);
		if (roles.isEmpty())
		{
			msgEvent.getChannel().sendMessage("Could not find a role by that name.").queue();
			return;
		}
		String roleID = roles.get(0).getId();
		for (String key : GlobalSettingsManager.getGlobalEnabledCommandKeys())
		{
			if (parameters[2].equalsIgnoreCase(key))
			{
				
				GuildSettingsManager guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId());
				if (parameters[1].equalsIgnoreCase("addrole"))
				{
					guildSettings.addRoleCommandPermission(key, roleID);
				}
				else
				{
					guildSettings.removeRoleCommandPermission(key, roleID);
				}
				msgEvent.getChannel().sendMessage(key+" permissions updated!").queue();	
			}
		}
	}
	private void setStatus(GuildMessageReceivedEvent msgEvent, String... parameters) 
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
					}
					guildSettings.setGuildCommandEnabledStatus(key, false);
				}

				msgEvent.getChannel().sendMessage(key+" status updated!").queue();	
			}
		}
	}
	private void setGameCategory(String categoryName, GuildMessageReceivedEvent msgEvent)
	{
		String categoryID;
		if (categoryName.equals(""))
		{
			categoryID = "";
		}
		else
		{
			categoryID = msgEvent.getGuild().getCategoriesByName(categoryName, true).get(0).getId();
			if (categoryID.equals(""))
			{
				msgEvent.getChannel().sendMessage("Category not found.").queue();
				return;
			}
			GuildSettingsManager guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId());
			guildSettings.setGuildGameCategoryID(categoryID);
			msgEvent.getChannel().sendMessage("Game category set to "+msgEvent.getGuild().getCategoryById(categoryID).getName());
		}
	}
	private void displayConfig(GuildMessageReceivedEvent msgEvent)
	{
		GuildSettingsManager guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId());
		EmbedBuilder embed = new EmbedBuilder();
		embed.setDescription("**Commands**\n"
				+ "/config SetGameCategory [Category Name]\n"
				+ "/config RemGameCategory"
				+ "/config enable [Command]\n"
				+ "/config disable [Command]\n"
				+ "/config AddRole [Command] [Role Name]\n"
				+ "/config RemRole [Command] [Role Name]\n");
				
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
			for (String roleID : guildSettings.getCommandRolePermissions(key))
			{
				roleList.append(msgEvent.getGuild().getRoleById(roleID).getName().replace("@", "")+", "); //Replacing @ here as getName appends it for the everyone role, causing a ping.
			}
			if (roleList.lastIndexOf(", ") == roleList.length()-2)
			{
				roleList.setLength(roleList.length()-2); //Remove last ', '
			}
			roleList.append("\n");
		}
		try
		{
			embed.setColor(msgEvent.getGuild().getSelfMember().getRoles().get(0).getColor()); //Try to set it to the bot's primary role color
		}
		catch (IndexOutOfBoundsException e)	//If the bot has no role
		{
			embed.setColor(Color.decode("#5967cf"));	//Use a default theme. //TODO: Make this global in Core (get)
		}
		embed.setTitle("============ Config =============");
		embed.addField("Command", keyList.toString(), true);
		embed.addField("Enabled", enabledList.toString(), true);
		embed.addField("Permissions", roleList.toString(), true);
		msgEvent.getChannel().sendMessage(embed.build()).queue();
	}

}
