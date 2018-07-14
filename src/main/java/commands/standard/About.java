package commands.standard;
import java.awt.Color;

import commands.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class About extends Command
{
	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
	{
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("======About======");
		embed.setDescription("Jara - A general purpose Discord bot built with smaller guilds in mind.\n"
				+ "\n**Contributors**\n"							//Feel free to add your details here
				+ "Zazsona\n"										//If forking, I ask that you retain the contributor list here in some capacity.
				+ "\nSource code available here:\n"				
				+ "https://github.com/Zazsona/Jara");
		
		try
		{
			embed.setColor(msgEvent.getGuild().getSelfMember().getRoles().get(0).getColor()); //Try to set it to the bot's primary role color
		}
		catch (IndexOutOfBoundsException e)	//If the bot has no role
		{
			embed.setColor(Color.decode("#5967cf"));	//Use a default theme.
		}
		msgEvent.getChannel().sendMessage(embed.build()).queue();	
	}
}
