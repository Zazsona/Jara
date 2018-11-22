package commands.utility;

import commands.Command;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Poll extends Command
{
	@Override
	public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
	{
		if (parameters.length > 21)
		{
			msgEvent.getChannel().sendMessage("ERROR: Limit of 20 options.").queue();
		}
		else
		{
			byte asciiCode = 65; //The ascii code for "A"
			StringBuilder descBuilder = new StringBuilder();
			for (String option : parameters)
			{
				if (!option.equals(parameters[0]))
					descBuilder.append(((char) asciiCode++)).append(". ").append(option).append("\n");
			}
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("====================");
			embed.setDescription("**Poll**\n"+descBuilder.toString()+"====================");
			embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
			Message msg = msgEvent.getChannel().sendMessage(embed.build()).complete();
			String[] reactions = {"🇦",  "🇧",  "🇨",  "🇩",  "🇪",   "🇫",   "🇬",   "🇭",   "🇮",  "🇯",   "🇰",   "🇱",   "🇲",   "🇳",   "🇴",  "🇵",  "🇶",  "🇷",  "🇸",  "🇹",  "🇺", "🇻", "🇼", "🇽", "🇾",  "🇿"};
			for (int i = 1; i<parameters.length; i++)
			{
				msg.addReaction(reactions[i-1]).queue();
			}
		}
	}
}
