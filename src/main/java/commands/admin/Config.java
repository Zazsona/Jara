package commands.admin;

import java.util.List;

import commands.Command;
import configuration.GlobalSettingsManager;
import configuration.GuildSettingsManager;
import jara.CommandAttributes;
import jara.CommandRegister;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Config extends Command { //TODO: Add category based enable/disable/permissions

	GuildSettingsManager guildSettings;
	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
	{
		guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId());
		if (parameters.length > 1)
		{
			if (parameters[1].equalsIgnoreCase("check"))
			{
				if (parameters.length > 2)
				{
					checkSetting(msgEvent, parameters[2]);
				}
			}
			else if (parameters[1].equalsIgnoreCase("setgamecategory"))
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
					CommandRegister.sendHelpInfo(msgEvent, getClass());
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
					CommandRegister.sendHelpInfo(msgEvent, getClass());
				}

			}
			else
			{
				CommandRegister.sendHelpInfo(msgEvent, getClass());
			}
		}
		else
		{
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("============== Config ===============");
			embed.setThumbnail("https://i.imgur.com/Hb8ET7G.png");
			embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
			StringBuilder descBuilder = new StringBuilder();
			descBuilder.append("**=========== Instructions ============**\n");
			descBuilder.append("/config\n");
			descBuilder.append("/config check [Command]/[Command Category]/GameCategory\n");
			descBuilder.append("/config SetGameCategory (Channel Category Name)\n");
			descBuilder.append("/config RemGameCategory\n");
			descBuilder.append("/config enable [Command]\n");
			descBuilder.append("/config disable [Command]\n");
			descBuilder.append("/config AddRole [Command] [Role Name]\n");
			descBuilder.append("/config RemRole [Command] [Role Name]\n\n");
			descBuilder.append(getGameCategory(msgEvent));
			descBuilder.append("**============= Commands ==============**\n");
			for (String key : GlobalSettingsManager.getGloballyEnabledCommandKeys())
			{
				renderCommandData(msgEvent, key, embed);
			}
			embed.setDescription(descBuilder.toString());
			msgEvent.getChannel().sendMessage(embed.build()).queue();
		}
	}
	private void setPermissions(GuildMessageReceivedEvent msgEvent, String... parameters) 
	{
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("============== Config ===============");
		embed.setThumbnail("https://i.imgur.com/Hb8ET7G.png");
		embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
		List<Role> roles = msgEvent.getGuild().getRolesByName(parameters[3], true);
		if (roles.isEmpty())
		{
			if (parameters[3].equalsIgnoreCase("everyone") || parameters[3].equals("*"))
			{
				roles.add(msgEvent.getGuild().getPublicRole());
			}
			else
			{
				embed.setDescription("Could not find a role by that name.");
				msgEvent.getChannel().sendMessage(embed.build()).queue();
				return;
			}
		}
		String roleID = roles.get(0).getId();
		if (parameters[2].equals("*"))
		{
			for (String key : GlobalSettingsManager.getGloballyEnabledCommandKeys())
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
			embed.setDescription(roles.get(0).getName().replace("@", "")+" can now use all enabled commands!");
			msgEvent.getChannel().sendMessage(embed.build()).queue();
		}
		else
		{
			for (String key : GlobalSettingsManager.getGloballyEnabledCommandKeys())
			{
				if (parameters[2].equalsIgnoreCase(key))
				{
					if (parameters[1].equalsIgnoreCase("addrole"))
					{
						guildSettings.addPermittedRole(key, roleID);
					}
					else
					{
						guildSettings.removePermittedRole(key, roleID);
					}
					embed.setDescription(key+" permissions updated!");
					msgEvent.getChannel().sendMessage(embed.build()).queue();

				}
			}
		}

	}
	private void setStatus(GuildMessageReceivedEvent msgEvent, String... parameters) 
	{
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("============== Config ===============");
		embed.setThumbnail("https://i.imgur.com/Hb8ET7G.png");
		embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
		if (parameters[2].equals("*"))
		{
			for (String key : GlobalSettingsManager.getGloballyEnabledCommandKeys())
			{

				if (parameters[1].equalsIgnoreCase("enable"))
				{
					guildSettings.setCommandEnabled(key, true);
				}
				else
				{

					guildSettings.setCommandEnabled(key, false);
				}
			}
			embed.setDescription("All basic commands have been "+parameters[1].toLowerCase()+"d"); //Felt like being lazy here. para[1] is always either "enable"/"disable", this makes it "enabled" or "disabled" in text.
			msgEvent.getChannel().sendMessage(embed.build()).queue();
		}
		else if (parameters[2].equalsIgnoreCase("games") || parameters[2].equalsIgnoreCase("toys") || parameters[2].equalsIgnoreCase("utility") || parameters[2].equalsIgnoreCase("audio") || parameters[2].equalsIgnoreCase("admin"))
		{
			for (CommandAttributes cmdAttributes : CommandRegister.getCommandsInCategory(CommandRegister.getCategoryID(parameters[2])))
			{
				if (GlobalSettingsManager.isCommandEnabledGlobally(cmdAttributes.getCommandKey()))
				{
					if (parameters[1].equalsIgnoreCase("enable"))
					{
						guildSettings.setCommandEnabled(cmdAttributes.getCommandKey(), true);
					}
					else
					{

						guildSettings.setCommandEnabled(cmdAttributes.getCommandKey(), false);
					}
				}
			}
			embed.setDescription("Commands in "+parameters[2]+" have been "+parameters[1]+"d");
			msgEvent.getChannel().sendMessage(embed.build()).queue();
		}
		else
		{
			for (String key : GlobalSettingsManager.getGloballyEnabledCommandKeys())
			{
				if (parameters[2].equalsIgnoreCase(key))
				{
					if (parameters[1].equalsIgnoreCase("enable"))
					{
						guildSettings.setCommandEnabled(key, true);
					}
					else
					{
						if (!CommandRegister.getCommand(key).isDisableable())
						{
							embed.setDescription(key + " cannot be disabled.");
							msgEvent.getChannel().sendMessage(embed.build()).queue();
							return;
						}
						guildSettings.setCommandEnabled(key, false);
					}
					embed.setDescription(key+" status updated!");
					msgEvent.getChannel().sendMessage(embed.build()).queue();
					return;
				}
			}
			embed.setDescription("Command not found.");
			msgEvent.getChannel().sendMessage(embed.build()).queue();
		}

	}
	private void setGameCategory(GuildMessageReceivedEvent msgEvent, String...parameters)
	{
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("============== Config ===============");
		embed.setThumbnail("https://i.imgur.com/Hb8ET7G.png");
		embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
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
				embed.setDescription("Category not found.");
				msgEvent.getChannel().sendMessage(embed.build()).queue();
				return;
			}
		}
		else
		{
			categoryID = "";
		}

		guildSettings.setGuildGameCategoryID(categoryID);
		if (categoryID.equals(""))
		{
			embed.setDescription("Category removed.");
			msgEvent.getChannel().sendMessage(embed.build()).queue();
		}
		else
		{
			embed.setDescription("Game category set to "+msgEvent.getGuild().getCategoryById(categoryID).getName());
			msgEvent.getChannel().sendMessage(embed.build()).queue();
		}

	}
	private void checkSetting(GuildMessageReceivedEvent msgEvent, String query)
	{
		EmbedBuilder embed = new EmbedBuilder();
		StringBuilder descBuilder = new StringBuilder();
		if (query.equalsIgnoreCase("games") || query.equalsIgnoreCase("toys") || query.equalsIgnoreCase("utility") || query.equalsIgnoreCase("audio") || query.equalsIgnoreCase("admin"))
		{
			for (CommandAttributes cmdAttributes : CommandRegister.getCommandsInCategory(CommandRegister.getCategoryID(query)))
			{
				if (GlobalSettingsManager.isCommandEnabledGlobally(cmdAttributes.getCommandKey()))
				{
					renderCommandData(msgEvent, cmdAttributes.getCommandKey(), embed);
				}
			}
		}
		else if (query.equalsIgnoreCase("gamecategory"))
		{
			descBuilder.append(getGameCategory(msgEvent));
		}
		else if (query.equals("*"))
		{
			descBuilder.append(getGameCategory(msgEvent));
			descBuilder.append("**============= Commands ==============**\n");
			for (String key : GlobalSettingsManager.getGloballyEnabledCommandKeys())
			{
				renderCommandData(msgEvent, key, embed);
			}
		}
		else
		{
			boolean commandFound = false;
			for (String key : GlobalSettingsManager.getGloballyEnabledCommandKeys())
			{
				if (key.equalsIgnoreCase(query))
				{
					renderCommandData(msgEvent, key, embed); //TODO
					commandFound = true;
					break;
				}
			}
			if (!commandFound)
			{
				CommandRegister.sendHelpInfo(msgEvent, getClass());
				return;
			}
		}
		embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
		embed.setTitle("============== Config ===============");
		embed.setDescription(descBuilder.toString());
		embed.setThumbnail("https://i.imgur.com/Hb8ET7G.png");
		msgEvent.getChannel().sendMessage(embed.build()).queue();
	}

	private String getGameCategory(GuildMessageReceivedEvent msgEvent)
	{
		StringBuilder descBuilder = new StringBuilder();
		descBuilder.append("**============ Game Category ============**\n");
		if (guildSettings.getGuildGameCategoryID().equals(""))
		{
			descBuilder.append("None Set.");
		}
		else
		{
			descBuilder.append(msgEvent.getGuild().getCategoryById(guildSettings.getGuildGameCategoryID()).getName()).append("\n");
			descBuilder.append(guildSettings.getGuildGameCategoryID());
		}
		descBuilder.append("\n\n");
		return descBuilder.toString();
	}

	private String renderCommandData(GuildMessageReceivedEvent msgEvent, String key, EmbedBuilder embed)
	{
		StringBuilder headerBuilder = new StringBuilder();
		StringBuilder dataBuilder = new StringBuilder();
		if (guildSettings.isCommandEnabled(key))
		{
			dataBuilder.append("*Enabled*\n");
		}
		else
		{
			dataBuilder.append("*Disabled*\n");
		}
		dataBuilder.append("Roles: ");
		for (String roleID : guildSettings.getPermittedRoles(key))
		{
			dataBuilder.append(msgEvent.getGuild().getRoleById(roleID).getName().replace("@", "")+", "); //Removing @ here as getName appends it for the everyone role, causing a ping.
		}
		if (dataBuilder.lastIndexOf(", ") == dataBuilder.length()-2)
		{
			dataBuilder.setLength(dataBuilder.length()-2); //Remove last ', '
		}
		dataBuilder.append("\n\n");
		embed.addField("== "+key+" ==", dataBuilder.toString(), true);
		return dataBuilder.toString();
	}

}
