package commands.admin.config;

import configuration.GuildSettings;
import jara.ModuleAttributes;
import jara.ModuleRegister;
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
    private ConfigModuleSettings cmos;
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
            cmos = new ConfigModuleSettings(guildSettings, channel);

            EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
            embed.setDescription("**Welcome to the Setup Wizard.**\n\nThis will guide you through each of the settings available for the guild, and then direct you through every command and module.\nYou can use \"quit\" at any point to keep default settings.\n\nLet's begin.");
            msgEvent.getChannel().sendMessage(embed.build()).queue();

            configurePrefix();
            configureTimezone();
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
            embed.setDescription("**MODULE SPECIFIC SETTINGS**");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
            configureModuleSettings();
            embed = ConfigMain.getEmbedStyle(msgEvent);
            embed.setDescription("**Setup Complete**\n\nAnd that's that, you're good to go!\nFor any additional assistance, use "+guildSettings.getCommandPrefix()+"help.");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
        catch (IOException e)
        {
            msgEvent.getChannel().sendMessage("An error occurred during setup.").queue();
            LoggerFactory.getLogger(getClass()).error(e.toString());
        }

    }

    private void configurePrefix() throws IOException
    {
        cms.modifyPrefix(msgEvent);
    }

    private void configureTimezone() throws IOException
    {
        cms.modifyTimeZone(msgEvent);
    }

    private void configureAudio() throws IOException
    {
        cas.modifySkipVotes(msgEvent);
        cas.modifyVoiceLeaving(msgEvent);
        cas.modifyQueueLimits(msgEvent);
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
        for (ModuleAttributes ma : ModuleRegister.getCommandModules())
        {
            if (ma.isDisableable())
            {
                ccs.showMenu(ma, embed);
            }
        }
    }

    private void configureModuleSettings() throws IOException
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        for (ModuleAttributes ma : ModuleRegister.getModules())
        {
            if (ma.getConfigClass() != null)
            {
                cmos.loadConfig(msgEvent, ma, null);
            }
        }
    }


}
