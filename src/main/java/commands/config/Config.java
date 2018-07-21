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
		GuildSettingsManager guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId());
		EmbedBuilder embed = new EmbedBuilder();
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
			roleList.setLength(roleList.length()-1); //Remove last ', '
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
		//TODO: Limit which commands can be disabled (Help, Config)
	}

}
