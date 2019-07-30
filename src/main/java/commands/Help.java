package commands;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import jara.CommandAttributes;
import jara.CommandRegister;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.*;

/**
 * Provides help details for built-in, module, and custom commands.
 */
public class Help extends Command
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
     * A static map holding all of the {@link HelpPage}s recorded. Indexed by command key
     */
    private static HashMap<String, HelpPage> pageMap = new HashMap<>();

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

    /**
     * This method adds a page to the HelpPage map
     * @param alias the alias to provide help for
     * @param hp the {@link HelpPage} to associated with it
     * @throws IllegalArgumentException alias already registered
     */
    public static void addPage(String alias, HelpPage hp)
    {
        if (pageMap.put(alias.toLowerCase(), hp) != null)
        {
            throw new IllegalArgumentException("Key "+alias+" has already been set.");
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
        CommandRegister.Category category = getCategory(parameters[1]);
        boolean limitToPerms = !(msgEvent.getMember().isOwner() | (parameters.length >= 3 && parameters[2].equalsIgnoreCase("all")));
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
                        commandInfo.add("**"+ca.getCommandKey()+"** - "+ca.getDescription());
                }
            }
            for (String key : guildSettings.getCustomCommandMap().keySet())
            {
                CommandAttributes ca = guildSettings.getCustomCommandAttributes(key);
                if (guildSettings.getCustomCommandAttributes(key).getCategory() == category)
                {
                    if (!limitToPerms || guildSettings.isPermitted(msgEvent.getMember(), key))
                        commandInfo.add("**"+key+"** - "+ca.getDescription());
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

    /**
     * Gets a specific command's details
     * @param alias the alias to get the help page for
     * @return the page as a string
     */
    private String getCommandPage(String alias)
    {
        /*
            No permissions check here is deliberate. The aim of removing those commands from the list is so that the user gets a list of commands they can use, thus do not have to play the guessing game.
            By not doing the check here they can still find out about the other commands when seeing them be used.
         */

        HelpPage helpPage = pageMap.get(alias);
        if (helpPage != null)
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("__Aliases__\n");
            for (String otherAlias : CommandRegister.getCommand(alias).getAliases())
            {
                stringBuilder.append(otherAlias).append(", ");
            }
            stringBuilder.setLength(stringBuilder.length()-2);
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

            stringBuilder.append("\n__Description__\n");
            stringBuilder.append(helpPage.description);
            return stringBuilder.toString();
        }
        else if (guildSettings.getCustomCommandAttributes(alias) != null)
        {
            CommandAttributes ca = guildSettings.getCustomCommandAttributes(alias);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("__Aliases__\n");
            stringBuilder.append(ca.getCommandKey()).append(", ");
            for (String otherAlias : guildSettings.getCustomCommand(alias).getAliases())
            {
                stringBuilder.append(otherAlias).append(", ");
            }
            stringBuilder.setLength(stringBuilder.length()-2);
            stringBuilder.append("\n\n__Description__\n");
            stringBuilder.append("A custom command.\n\n").append(ca.getDescription());
            return stringBuilder.toString();
        }
        else
        {
            return "PICNIC Error: Unknown Parameter \""+alias+"\". Usage: /Help [Category]/[Command] (all) (Page #)";
        }
    }

    /**
     * Gets the category in the user's request
     * @param parameter the user request
     * @return the category, or null if it is an invalid request
     */
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