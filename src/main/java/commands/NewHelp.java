package commands;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import jara.CommandAttributes;
import jara.CommandRegister;
import jara.Core;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.*;

public class NewHelp extends Command
{
    private String prefix;
    private GuildSettings guildSettings;
    private static HashMap<String, HelpPage> pageMap = new HashMap<>();
    public static class HelpPage
    {
        final String[] params = new String[0];
        final String description = "No information has been provided for this command.";
    }


    public static void addPage(String key, HelpPage hp)
    {
        if (pageMap.put(key, hp) != null)
        {
            throw new IllegalArgumentException("That key has already been set.");
        }
    }

    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
        prefix = SettingsUtil.getGuildCommandPrefix(msgEvent.getGuild().getId()).toString();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Help Menu", null, null);
        embed.setColor(Core.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        if (parameters.length == 1)
        {
            embed.setDescription("To get a list of commands, use "+prefix+"help [Category].\n" +
                                         "By default, only commands you have access to are shown. use \""+prefix+"help [Category] all\" to see every command.");
            String topics =
                    "**Games** - SP/MP experiences to keep you busy!\n"
                            + "**Toys** - Quick fun commands.\n"
                            + "**Utility** - Small commands for basic applications.\n"
                            + "**Audio** - Commands for Voice Channels.\n"
                            + "**Admin** - Tools to modify the bot.";

            embed.addField("Topics",topics, true);
        }
        else if (parameters.length >= 2)
        {
            embed = getPage(msgEvent, parameters, embed);
        }
        msgEvent.getChannel().sendMessage(embed.build()).queue();
    }

    private EmbedBuilder getPage(GuildMessageReceivedEvent msgEvent, String[] parameters, EmbedBuilder embed)
    {
        CommandRegister.Category category = getCategory(parameters[1]);
        boolean limitToPerms = msgEvent.getMember().isOwner() | (parameters.length == 3 && parameters[2].equalsIgnoreCase("all"));
        if (category != null)
        {
            LinkedList<String> commandInfo = new LinkedList<>();
            /*
                Get commands from all sources
             */
            for (CommandAttributes ca : CommandRegister.getCommandsInCategory(category))
            {
                if (SettingsUtil.getGlobalSettings().isCommandEnabled(ca.getCommandKey()) && guildSettings.isCommandEnabled(ca.getCommandKey()))
                {
                    if (!limitToPerms || guildSettings.isPermitted(msgEvent.getMember(), ca.getCommandKey()))
                        commandInfo.add(ca.getCommandKey()+" - "+ca.getDescription());
                }
            }
            for (String key : guildSettings.getCustomCommandMap().keySet())
            {
                CommandAttributes ca = guildSettings.getCustomCommandAttributes(key);
                if (guildSettings.getCustomCommandAttributes(key).getCategory() == category)
                {
                    if (!limitToPerms || guildSettings.isPermitted(msgEvent.getMember(), key))
                        commandInfo.add(key+" - "+ca.getDescription());
                }
            }
            commandInfo.sort(Comparator.naturalOrder());
            /*
                All commands added & sorted.
             */

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
            embed.setDescription(getCommandPage(parameters[1].toLowerCase()));
            return embed;
        }
    }

    private String getCommandPage(String key)
    {
        /*
            No permissions check here is deliberate. The aim of removing those commands from the list is so that the user gets a list of commands they can use, thus do not have to play the guessing game.
            By not doing the check here they can still find out about the other commands when seeing them be used.
         */

        HelpPage helpPage = pageMap.get(key);
        if (helpPage != null)
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("**Aliases**\n");
            for (String alias : CommandRegister.getCommand(key).getAliases())
            {
                stringBuilder.append(alias).append(", ");
            }
            stringBuilder.setLength(stringBuilder.length()-2);
            stringBuilder.append("\n**Parameters**\n");
            if (helpPage.params.length > 0)
            {
                for (String param : helpPage.params)
                {
                    stringBuilder.append(prefix).append(param).append("\n");
                }
            }
            else
            {
                stringBuilder.append(prefix).append(key).append("\n");
            }

            stringBuilder.append("**Description\n**");
            stringBuilder.append(helpPage.description);
            return stringBuilder.toString();
        }
        else if (guildSettings.getCustomCommandAttributes(key) != null)
        {
            CommandAttributes ca = guildSettings.getCustomCommandAttributes(key);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("**Aliases**\n");
            for (String alias : guildSettings.getCustomCommand(key).getAliases())
            {
                stringBuilder.append(alias).append(", ");
            }
            stringBuilder.setLength(stringBuilder.length()-2);
            stringBuilder.append("\n**Description\n**");
            stringBuilder.append("A custom command.\n\n").append(ca.getDescription());
            return stringBuilder.toString();
        }
        else
        {
            return "PICNIC Error: Unknown Parameter \""+key+"\". Usage: /Help [Category]/[Command] (all) (Page #)";
        }
    }

    private CommandRegister.Category getCategory(String parameter)
    {
        for (CommandRegister.Category category : CommandRegister.Category.values())
        {
            if (parameter.equalsIgnoreCase(category.toString()))
            {
                return category;
            }
        }
        return null;
    }
}