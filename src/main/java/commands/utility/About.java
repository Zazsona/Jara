package commands.utility;
import java.awt.Color;

import commands.Command;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class About extends Command
{
	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
	{
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("======About======");
		embed.setDescription("Jara - A Discord bot for you.\n"
				+ "\n**Contributors**\n"							//Feel free to add your details here
				+ "Zazsona\n"										//If forking, I ask that you retain the contributor list here in some capacity.
				+ "\nSource code available here:\n"				
				+ "https://github.com/Zazsona/Jara");
		embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
		msgEvent.getChannel().sendMessage(embed.build()).queue();	
	}
}
