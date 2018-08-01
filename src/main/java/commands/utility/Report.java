package commands.utility;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import commands.Command;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Report extends Command
{
	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
	{
		StringBuilder reportSB = new StringBuilder();
		reportSB.append("Bot User: "+msgEvent.getJDA().getSelfUser().getName()+"#"+msgEvent.getJDA().getSelfUser().getDiscriminator()+"\n");
		reportSB.append("Status: Online\n");
		reportSB.append("DateTime: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyy HH:mm:ss")) + "\n");
		reportSB.append("Shard: "+msgEvent.getJDA().getShardInfo().getShardId()+"\n");
		reportSB.append("Shard Total: "+msgEvent.getJDA().getShardInfo().getShardTotal()+"\n");
		reportSB.append("Server: "+msgEvent.getGuild().getName()+"\n");
		reportSB.append("Channel: #"+msgEvent.getChannel().getName()+"\n");
		reportSB.append("Ping: "+msgEvent.getJDA().getPing()+"ms\n");
		reportSB.append("Command Author: "  + msgEvent.getAuthor().getName()+"#"+msgEvent.getAuthor().getDiscriminator());
		if (msgEvent.getMember().getNickname() != null)
		{
			reportSB.append(" (" + msgEvent.getMember().getNickname() + ")");   //Display guild specific nickname, too.
		}
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(msgEvent.getJDA().getSelfUser().getName()+" Report:");
		embed.setDescription(reportSB.toString());
		embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
		
        msgEvent.getChannel().sendMessage(embed.build()).queue();
	}

}
