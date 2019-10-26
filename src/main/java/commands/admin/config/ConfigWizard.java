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

    /**
     * Constructor
     * @param msgEvent context
     * @param guildSettings the guild settings to run through
     * @param channel the channel to run in
     */
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

    /**
     * Prompts the user to set the command prefix
     * @throws IOException unable to save
     */
    private void configurePrefix() throws IOException
    {
        cms.modifyPrefix(msgEvent);
    }

    /**
     * Prompts the user to set the timezone
     * @throws IOException unable to save
     */
    private void configureTimezone() throws IOException
    {
        cms.modifyTimeZone(msgEvent);
    }

    /**
     * Runs the user through setting the audio settings
     * @throws IOException unable to save
     */
    private void configureAudio() throws IOException
    {
        cas.modifySkipVotes(msgEvent);
        cas.modifyVoiceLeaving(msgEvent);
        cas.modifyQueueLimits(msgEvent);
    }

    /**
     * Runs the user through setting the game settings
     * @throws IOException unable to save
     */
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

    /**
     * Runs the user through setting the command settings
     * @throws IOException unable to save
     */
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

    /**
     * Runs the user through setting the module specific settings
     * @throws IOException unable to save
     */
    private void configureModuleSettings() throws IOException
    {
        for (ModuleAttributes ma : ModuleRegister.getModules())
        {
            if (ma.getConfigClass() != null)
            {
                cmos.loadConfig(msgEvent, ma, null, true);
            }
        }
    }


}
