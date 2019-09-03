package commands.admin;

import commands.CmdUtil;
import module.Command;
import configuration.GuildSettings;
import configuration.SettingsUtil;
import configuration.guild.CustomCommandBuilder;
import jara.ModuleRegister;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.ArrayList;

public class CustomCommandManager extends Command
{
    private MessageManager mm;
    private GuildSettings guildSettings;

    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        mm = new MessageManager();
        guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());

        StringBuilder commandListBuilder = new StringBuilder();
        for (String key : guildSettings.getCustomCommandMap().keySet())
        {
            commandListBuilder.append(key).append(", ");
        }

        while (true) //This is here so that, if someone without permission tries to use the menu, it will just ignore their response.
        {
            EmbedBuilder embed = getEmbedStyle(msgEvent);
            StringBuilder descBuilder = new StringBuilder();
            descBuilder.append("Please select an option, followed by the command. E.g: `Edit PartyTime`.\n");
            if (commandListBuilder.toString().length() > 0)
                descBuilder.append("Your commands:\n").append(commandListBuilder.toString().substring(0, commandListBuilder.length()-2)).append("\n\n");
            descBuilder.append("**Add** - Add a new command.\n**Edit** - Modify a command.\n**Delete** - Delete a command.\n**Quit** - Close menu");
            embed.setDescription(descBuilder.toString());
            msgEvent.getChannel().sendMessage(embed.build()).queue();
            try
            {
                Message message = mm.getNextMessage(msgEvent.getChannel());

                if (guildSettings.isPermitted(message.getMember(), getClass()))
                {
                    CustomCommandBuilder customCommand = null;
                    String[] selection = message.getContentDisplay().toLowerCase().split(" ");
                    if (!selection[0].equals("quit"))
                        customCommand = guildSettings.getCustomCommand(selection[1]);

                    switch (selection[0].trim())
                    {
                        case "add":
                            if (customCommand != null)
                            {
                                embed.setDescription("That command already exists. Opening command for editing...");
                                editCommand(msgEvent, customCommand);
                            }
                            else
                            {
                                ArrayList<String> everyoneRole = new ArrayList<>();
                                everyoneRole.add(msgEvent.getGuild().getPublicRole().getId());
                                customCommand = guildSettings.addCustomCommand(selection[1], new String[0], "A custom command.", ModuleRegister.Category.UTILITY, new ArrayList<>(), "", "");
                                guildSettings.addPermissions(everyoneRole, selection[1]);
                                editCommand(msgEvent, customCommand);
                            }
                            return;
                        case "edit":
                            editCommand(msgEvent, customCommand);
                            return;
                        case "delete":
                            guildSettings.removeCustomCommand(selection[1]);
                            embed.setDescription("Command "+selection[1]+" successfully removed.");
                            msgEvent.getChannel().sendMessage(embed.build()).queue();
                            return;
                        case "quit":
                            embed.setDescription("Custom Command Manager closed.");
                            msgEvent.getChannel().sendMessage(embed.build()).queue();
                            return;
                        default:
                            embed.setDescription("Invalid Selection.");
                            msgEvent.getChannel().sendMessage(embed.build()).queue();
                            break;
                    }
                }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                embed.setDescription("You have to specify an option and a command. `[Option] [Command Name]`");
                msgEvent.getChannel().sendMessage(embed.build()).queue();
            }
            catch (IOException e)
            {
                embed.setDescription("An error occurred when saving settings.");
                msgEvent.getChannel().sendMessage(embed.build()).queue();
            }

        }

    }

    private void editCommand(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand)
    {
        while (true)
        {
            EmbedBuilder embed = getEmbedStyle(msgEvent);
            embed.setDescription("What would you like to modify?\n**Aliases**\n**Description**\n**Category**\n\n**Message**\n**Audio**\n**Roles**\n\n**Quit**");
            msgEvent.getChannel().sendMessage(embed.build()).queue();

            Message message = mm.getNextMessage(msgEvent.getChannel());
            String selection = message.getContentDisplay().toLowerCase();

            if (guildSettings.isPermitted(message.getMember(), getClass()))
            {
                switch (selection)
                {
                    case "aliases":
                        modifyAliases(msgEvent, customCommand);
                        break;
                    case "description":
                        modifyDescription(msgEvent, customCommand);
                        break;
                    case "category":
                        modifyCategory(msgEvent, customCommand);
                        break;
                    case "message":
                        setUseFeatures(msgEvent, customCommand, 1);
                        break;
                    case "roles":
                        setUseFeatures(msgEvent, customCommand, 2);
                        break;
                    case "audio":
                        setUseFeatures(msgEvent, customCommand, 3);
                        break;
                    case "quit":
                        String quitMessage = save(customCommand);
                        embed.setDescription(quitMessage);
                        msgEvent.getChannel().sendMessage(embed.build()).queue();
                        return;
                    default:
                        embed.setDescription("Invalid selection. Please try again.");
                        msgEvent.getChannel().sendMessage(embed.build()).queue();
                        break;
                }
            }
        }
    }

    private String save(CustomCommandBuilder customCommand)
    {
        try
        {
            guildSettings.editCustomCommand(customCommand.getKey(), customCommand);
            return "Saved & exited.";
        }
        catch (IOException e)
        {
            return "An error occurred when saving the command.";
        }
    }

    private void modifyAliases(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand)
    {
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        StringBuilder descBuilder = new StringBuilder();
        descBuilder.append("Please enter the aliases you would like to add/remove, or \"Quit\" to quit. Separate them with spaces.\n\nExisting Aliases:\n");
        for (String alias : customCommand.getAliases())
        {
            descBuilder.append(alias).append(", ");
        }
        embed.setDescription(descBuilder.toString().substring(0, descBuilder.length()-2));
        msgEvent.getChannel().sendMessage(embed.build()).queue();

        while (true)
        {
            Message message = mm.getNextMessage(msgEvent.getChannel());
            String[] newAliases = message.getContentDisplay().toLowerCase().split(" ");

            if (guildSettings.isPermitted(message.getMember(), getClass()))
            {
                if (newAliases[0].equalsIgnoreCase("quit"))
                {
                    break;
                }
                customCommand.modifyAliases(newAliases);
                break;
            }
        }

    }

    private void modifyDescription(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand)
    {
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        embed.setDescription("Please enter the new description.\n\nExisting description: " + customCommand.getDescription());
        msgEvent.getChannel().sendMessage(embed.build()).queue();

        while (true)
        {
            Message message = mm.getNextMessage(msgEvent.getChannel());
            String newDescription = message.getContentDisplay();

            if (guildSettings.isPermitted(message.getMember(), getClass()))
            {
                customCommand.setDescription(newDescription);
                break;
            }
        }

    }

    private void modifyCategory(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand)
    {
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        embed.setDescription("Please enter the new category out of:\n**Admin**\n**Audio**\n**Games**\n**Toys**\n**Utility**\n\nCurrent Category: "+ ModuleRegister.getCategoryName(customCommand.getCategory()));
        msgEvent.getChannel().sendMessage(embed.build()).queue();
        while (true)
        {
            Message message = mm.getNextMessage(msgEvent.getChannel());
            ModuleRegister.Category category = ModuleRegister.getCategoryID(message.getContentDisplay());

            if (guildSettings.isPermitted(message.getMember(), getClass()))
            {
                if (category == null)
                {
                    embed.setDescription("Invalid category. Please try again.");
                    msgEvent.getChannel().sendMessage(embed.build()).queue();
                }
                else
                {
                    customCommand.setCategory(category);
                    break;
                }
            }
        }
    }

    private void setUseFeatures(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand, int feature)
    {
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        switch (feature)
        {
            case 1:
                embed.setDescription("Would you like the command to send a message? [Y/N]");
                break;
            case 2:
                embed.setDescription("Would you like the command to toggle roles? [Y/N]");
                break;
            case 3:
                embed.setDescription("Would you like the command to queue audio? [Y/N]");
                break;
            default:
                throw new IndexOutOfBoundsException();
        }
        msgEvent.getChannel().sendMessage(embed.build()).queue();

        while (true)
        {
            Message message = mm.getNextMessage(msgEvent.getChannel());
            String selection = message.getContentDisplay().toLowerCase();

            if (guildSettings.isPermitted(message.getMember(), getClass()))
            {
                switch (selection)
                {
                    case "y":
                        switch (feature)
                        {
                            case 1:
                                setNewMessage(msgEvent, customCommand);
                                break;
                            case 2:
                                setNewRolesToggle(msgEvent, customCommand);
                                break;
                            case 3:
                                setNewAudioLink(msgEvent, customCommand);
                                break;
                            default:
                                throw new IndexOutOfBoundsException();
                        }
                        return;
                    case "n":
                        switch (feature)
                        {
                            case 1:
                                customCommand.setMessage("");
                                break;
                            case 2:
                                customCommand.modifyRoles(customCommand.getRoles().toArray(new String[0])); //If this method finds it already has the roles, it removes them. By plugging in all existing roles, it removes all of them.
                                break;
                            case 3:
                                customCommand.setAudioLink("");
                            default:
                                throw new IndexOutOfBoundsException();
                        }
                        return;
                    default:
                        embed.setDescription("Invalid selection");
                        msgEvent.getChannel().sendMessage(embed.build()).queue();
                        break;

                }
            }
        }
    }
    private void setNewMessage(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand)
    {
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        embed.setDescription("Please enter the new message.\n\nExisting message: " + customCommand.getMessage());
        msgEvent.getChannel().sendMessage(embed.build()).queue();

        while (true)
        {
            Message message = mm.getNextMessage(msgEvent.getChannel());
            String newMessage = message.getContentRaw();

            if (guildSettings.isPermitted(message.getMember(), getClass()))
            {
                customCommand.setMessage(newMessage);
                break;
            }
        }

    }

    private void setNewRolesToggle(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand)
    {
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        StringBuilder descBuilder = new StringBuilder();
        descBuilder.append("Please enter the roles (by name) you would like to add/remove, separated by commas, or \"Quit\" to quit.\n\nExisting Roles:\n");
        for (String roleID : customCommand.getRoles())
        {
            Role role = msgEvent.getGuild().getRoleById(roleID);
            if (role != null)
            {
                descBuilder.append(role.getName()).append(", ");
            }

        }
        embed.setDescription(descBuilder.toString().substring(0, descBuilder.length()-2));
        msgEvent.getChannel().sendMessage(embed.build()).queue();

        while (true)
        {
            Message message = mm.getNextMessage(msgEvent.getChannel());
            String[] newRoles = message.getContentDisplay().toLowerCase().split(",");

            if (guildSettings.isPermitted(message.getMember(), getClass()))
            {
                if (newRoles[0].equalsIgnoreCase("quit"))
                {
                    break;
                }
                ArrayList<String> newRoleIDs = new ArrayList<>();
                for (String roleName : newRoles)
                {
                    newRoleIDs.add(msgEvent.getGuild().getRolesByName(roleName, true).get(0).getId());
                }
                customCommand.modifyRoles(newRoleIDs.toArray(newRoles));
                break;
            }
        }

    }

    private void setNewAudioLink(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand)
    {
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        embed.setDescription("Please enter the new audio track link.\n\nExisting link: " + customCommand.getAudioLink());
        msgEvent.getChannel().sendMessage(embed.build()).queue();
        while (true)
        {
            Message message = mm.getNextMessage(msgEvent.getChannel());
            String newAudioLink = message.getContentDisplay();

            if (guildSettings.isPermitted(message.getMember(), getClass()))
            {
                customCommand.setAudioLink(newAudioLink);
                break;
            }
        }

    }


    private EmbedBuilder getEmbedStyle(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        return embed;
    }
}
