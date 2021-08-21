package com.zazsona.jara.commands;

import com.zazsona.jara.configuration.GuildSettings;
import com.zazsona.jara.configuration.SettingsUtil;
import com.zazsona.jara.ModuleAttributes;
import com.zazsona.jara.ModuleManager;
import com.zazsona.jara.SeasonalModuleAttributes;
import com.zazsona.jara.module.ModuleCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * Provides help details for built-in, module, and custom commands.
 */
public class Help extends ModuleCommand
{
    /**
     * The command prefix used for this guild
     */
    private String prefix;
    /**
     * The settings for this guild
     */
    private GuildSettings guildSettings;

    /**
     * Layout for help pages, where params lists the various call parameters, and description provides detailed information about the command.
     */
    public static class HelpPage
    {
        /**
         * The parameters that may be used to call the command<br>
         * [Param] indicates required<br>
         * (Param) indicates optional
         */
        String[] params;
        /**
         * A detailed description about what the command does, and how to use it.
         */
        String description;

        public HelpPage(String description, String... params)
        {
            this.params = params;
            this.description = description;
        }

        public HelpPage()
        {
            this.params = new String[0];
            this.description = "No information has been provided for this command.";
        }

    }

    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
        prefix = SettingsUtil.getGuildCommandPrefix(msgEvent.getGuild().getId()).toString();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("======== Help Menu ========", null, null);
        embed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        embed.setThumbnail("https://i.imgur.com/4TUoYOM.png");
        if (parameters.length == 1)
        {
            embed.setDescription("To get a list of commands, use "+prefix+"help [Category] (all).");
            String topics =
                    "**Games** - SP/MP experiences to keep you busy!\n"
                            + "**Toys** - Quick fun commands.\n"
                            + "**Utility** - Small commands for basic applications.\n"
                            + "**Audio** - Commands for Voice Channels.\n"
                            + "**Seasonal** - Seasonal celebrations.\n"
                            + "**Admin** - Tools to modify the bot.";

            embed.addField("Topics",topics, true);
        }
        else if (parameters.length >= 2)
        {
            embed = getPage(msgEvent, parameters, embed);
        }
        msgEvent.getChannel().sendMessage(embed.build()).queue();
    }

    /**
     * Gets the formatted page the user has requested
     * @param msgEvent the context
     * @param parameters the user's request
     * @param embed the embed to set
     * @return a formatted embed with the requested help details
     */
    private EmbedBuilder getPage(GuildMessageReceivedEvent msgEvent, String[] parameters, EmbedBuilder embed)
    {
        ModuleManager.Category category = getCategory(parameters[1]);
        boolean limitToPerms = !(msgEvent.getMember().isOwner() | (parameters.length >= 3 && parameters[2].equalsIgnoreCase("all")));
        if (category != null)
        {
            LinkedList<String> commandInfo = getCommandStrings(msgEvent, category, limitToPerms);

            if (commandInfo.size() == 0)
            {
                 embed.setDescription("Sorry, looks like I haven't got anything for you here.");
                 return embed;
            }
            else
            {
                StringBuilder descBuilder = new StringBuilder();
                for (String command : commandInfo)
                {
                    descBuilder.append(command).append("\n");
                }
                if (descBuilder.length() > 2047) //Check if pages are required
                {
                    int pageNo = (parameters.length >= 3 && parameters[parameters.length-1].matches("[0-9]*")) ? 1 : Integer.parseInt(parameters[parameters.length-1]); //We check the final parameter as to avoid conflicts with "all"
                    int pageTotal = (int) Math.ceil(descBuilder.length()/2048);
                    embed.setFooter("Page "+pageNo+" of "+pageTotal+".", "");
                    for (int i = 1; i<pageNo; i++)
                    {
                        int lastEntryEnd = descBuilder.substring(0, 2048).lastIndexOf("\n")+1;
                        descBuilder.replace(0, lastEntryEnd, "");
                    }
                }
                embed.setDescription(descBuilder.toString());
                return embed;
            }
        }
        else
        {
            embed.setDescription(getCommandPage(parameters[1].toLowerCase(), msgEvent.getMember().isOwner() || guildSettings.isPermitted(msgEvent.getMember(), "Config")));
            return embed;
        }
    }

    /**
     * Gets the list of commands, and description snippets
     * @param msgEvent context
     * @param category the category of commands to list
     * @param limitToPerms whether to only display commands the user can use
     * @return a list of commands keys and their description in pretty print
     */
    @NotNull
    private LinkedList<String> getCommandStrings(GuildMessageReceivedEvent msgEvent, ModuleManager.Category category, boolean limitToPerms)
    {
        LinkedList<String> commandInfo = new LinkedList<>();
        for (ModuleAttributes ma : ModuleManager.getModulesInCategory(category))
        {
            if (ma.getCommandClass() != null)
            {
                if (SettingsUtil.getGlobalSettings().isModuleEnabled(ma.getKey()) && guildSettings.isCommandEnabled(ma.getKey()))
                {
                    if (!limitToPerms || guildSettings.isPermitted(msgEvent.getMember(), ma.getKey()))
                    {
                        if (ma instanceof SeasonalModuleAttributes)
                        {
                            if (limitToPerms && !((SeasonalModuleAttributes) ma).isActive(ZonedDateTime.now(guildSettings.getTimeZoneId())))
                            {
                                continue;
                            }
                        }
                        commandInfo.add("**"+ma.getKey()+"** - "+ma.getDescription());
                    }

                }
            }
        }
        for (String key : guildSettings.getCustomCommandSettings().getCommandKeys())
        {
            ModuleAttributes ma = guildSettings.getCustomCommandSettings().getCommandAttributes(key);
            if (ma.getCategory() == category)
            {
                if (!limitToPerms || guildSettings.isPermitted(msgEvent.getMember(), key))
                    commandInfo.add("**"+key+"** - "+((ma.getDescription().length() > 30) ? ma.getDescription().replace("\n", " ").substring(0, 27)+"..." : ma.getDescription().replace("\n", " ")));
            }
        }
        commandInfo.sort(Comparator.naturalOrder());
        return commandInfo;
    }

    /**
     * Gets a specific command's details
     * @param alias the alias to get the help page for
     * @param userHasConfigAccess boolean on config access, displays configuration details
     * @return the page as a string
     */
    private String getCommandPage(String alias, boolean userHasConfigAccess)
    {
        /*
            No permissions check here is deliberate. The aim of removing those commands from the list is so that the user gets a list of commands they can use, thus do not have to play the guessing game.
            By not doing the check here they can still find out about the other commands when seeing them be used.
         */

        ModuleAttributes moduleAttributes = ModuleManager.getModule(alias);
        if (moduleAttributes != null)
        {
            HelpPage helpPage = moduleAttributes.getHelpPage();
            StringBuilder stringBuilder = new StringBuilder();
            if (userHasConfigAccess) //This is limited to config users for the sake of tidiness. Standard users have no use for this information.
            {
                stringBuilder.append("__Attributes__\n");
                stringBuilder.append("Name: ").append(moduleAttributes.getKey()).append("\n");
                stringBuilder.append("Command: ").append((moduleAttributes.getCommandClass() != null) ? "*Yes*" : "*No*").append("\n");
                stringBuilder.append("Configurable: ").append((moduleAttributes.getConfigClass() != null) ? "*Yes*" : "*No*").append("\n");
                stringBuilder.append("\n");
            }
            stringBuilder.append("__Aliases__\n");
            for (String otherAlias : moduleAttributes.getAliases())
            {
                stringBuilder.append(otherAlias).append(", ");
            }
            stringBuilder.setLength(stringBuilder.length()-2);
            if (moduleAttributes.getCommandClass() != null)
            {
                stringBuilder.append("\n\n__Parameters__\n");
                if (helpPage.params.length > 0)
                {
                    for (String param : helpPage.params)
                    {
                        stringBuilder.append(prefix).append(param).append("\n");
                    }
                }
                else
                {
                    stringBuilder.append(prefix).append(alias).append("\n");
                }
            }
            else
            {
                stringBuilder.append("\n");
            }
            stringBuilder.append("\n__Description__\n");
            stringBuilder.append(helpPage.description);
            return stringBuilder.toString();
        }
        else if (guildSettings.getCustomCommandSettings().getCommandAttributes(alias) != null)
        {
            moduleAttributes = guildSettings.getCustomCommandSettings().getCommandAttributes(alias);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("__Attributes__\n");
            stringBuilder.append("Name: ").append(moduleAttributes.getKey()).append("\n");
            stringBuilder.append("Command: ").append("*Yes (Custom)*").append("\n");
            stringBuilder.append("Configurable: ").append("*Yes (Custom)*").append("\n");
            stringBuilder.append("\n__Aliases__\n");
            stringBuilder.append(moduleAttributes.getKey()).append(", ");
            for (String otherAlias : guildSettings.getCustomCommandSettings().getCommand(alias).getAliases())
            {
                stringBuilder.append(otherAlias).append(", ");
            }
            stringBuilder.setLength(stringBuilder.length()-2);
            stringBuilder.append("\n\n__Description__\n");
            stringBuilder.append("A custom command.\n\n").append(moduleAttributes.getDescription());
            return stringBuilder.toString();
        }
        else
        {
            return "PICNIC Error: Unknown Parameter \""+alias+"\".";
        }
    }

    /**
     * Gets the category in the user's request
     * @param parameter the user request
     * @return the category, or null if it is an invalid request
     */
    private ModuleManager.Category getCategory(String parameter)
    {
        for (ModuleManager.Category category : ModuleManager.Category.values())
        {
            if (parameter.equalsIgnoreCase(category.toString()))
            {
                return category;
            }
        }
        return null;
    }
}