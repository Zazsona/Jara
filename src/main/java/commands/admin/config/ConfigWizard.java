package commands.admin.config;

import configuration.GuildSettings;
import jara.CommandAttributes;
import jara.CommandRegister;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;

public class ConfigWizard
{
    private GuildSettings guildSettings;
    private ConfigMainSettings cms;
    private ConfigAudioSettings cas;
    private ConfigGameSettings cgs;
    private ConfigCommandSettings ccs;
    private GuildMessageReceivedEvent msgEvent;

    public ConfigWizard(GuildMessageReceivedEvent msgEvent, GuildSettings guildSettings, TextChannel channel)
    {
        try
        {
            this.guildSettings = guildSettings;
            this.msgEvent = msgEvent;
            cms = new ConfigMainSettings(guildSettings, channel);
            cas = new ConfigAudioSettings(guildSettings, channel);
            cgs = new ConfigGameSettings(guildSettings, channel);
            ccs = new ConfigCommandSettings(guildSettings, channel);

            EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
            embed.setDescription("**Welcome to the Setup Wizard.**\n\nThis will guide you through each of the settings available for the guild, and then direct you through every command. Let's begin.");
            msgEvent.getChannel().sendMessage(embed.build()).queue();

            configurePrefix();
            embed.setColor(Color.RED);
            embed.setDescription("**AUDIO**");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
            configureAudio();
            embed.setDescription("**GAMES**");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
            configureGames();
            embed.setDescription("**COMMANDS**");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
            configureCommands();
            embed = ConfigMain.getEmbedStyle(msgEvent);
            embed.setDescription("**Setup Complete**\n\nAnd that's that, you're good to go!\nSome commands may have their own individual settings, so look out for those. You can also create your own custom commands using the CustomCommandManager.");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
        catch (IOException e)
        {
            msgEvent.getChannel().sendMessage("An error occurred during setup.").queue();
            LoggerFactory.getLogger(getClass()).error(e.getMessage());
        }

    }

    private void configurePrefix() throws IOException
    {
        cms.modifyPrefix(msgEvent);
    }

    private void configureAudio() throws IOException
    {
        cas.modifySkipVotes(msgEvent);
        cas.modifyVoiceLeaving(msgEvent);
    }

    private void configureGames() throws IOException
    {
        cgs.modifyGameChannels(msgEvent);
        if (guildSettings.isGameChannelsEnabled())
        {
            cgs.modifyChannelTimeout(msgEvent);
        }
        else
        {
            cgs.modifyConcurrentGameInChannel(msgEvent);
        }
    }

    private void configureCommands() throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        for (CommandAttributes ca : CommandRegister.getRegister())
        {
            if (ca.isDisableable())
            {
                ccs.showMenu(ca, embed);
            }
        }
    }


}
