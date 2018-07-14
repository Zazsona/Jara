package commands.config;

import java.util.ArrayList;
import java.util.List;

import commands.Command;
import configuration.GuildSettingsManager;
import jara.CommandRegister;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Config extends Command {

	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
	{
		GuildSettingsManager guildSettings = new GuildSettingsManager(msgEvent.getGuild().getId()); //TODO: Move this to command handler, and remove disabled command message
		ArrayList<String> roleIDs = guildSettings.getCommandRolePermissions(new CommandRegister().getCommandKey(getClass()));
		List<Role> userRoles = msgEvent.getMember().getRoles();
		boolean permissionGranted = false;
		for (Role userRole : userRoles)
		{
			if (roleIDs.contains(userRole.getId()))
			{
				permissionGranted = true;
			}
		}
		

	}

}
