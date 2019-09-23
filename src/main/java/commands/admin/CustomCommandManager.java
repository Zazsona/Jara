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
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class CustomCommandManager extends Command
{
    private MessageManager mm;
    private GuildSettings guildSettings;

    /**
     * The validity of a response
     */
    private enum CCResponseType
    {
        VALID,
        QUIT,
        RESET,
        INVALID_PERMISSIONS,
        INVALID
    }

    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        mm = new MessageManager();
        guildSettings = SettingsUtil.getGuildSettings(msgEvent.getGuild().getId());
        if (parameters.length > 1)
        {
            parseAsParameters(msgEvent, parameters);
        }
        else
        {
            StringBuilder commandListBuilder = new StringBuilder();
            for (String key : guildSettings.getCustomCommandSettings().getCommandKeys())
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
                        String[] selection = message.getContentDisplay().split(" ");
                        if (!selection[0].equals("quit"))
                        {
                            customCommand = guildSettings.getCustomCommandSettings().getCommand(selection[1]);
                            CCResponseType responseType = selectRootSubmenu(msgEvent, customCommand, selection[0].toLowerCase(), selection[1]);
                            if (responseType == CCResponseType.VALID)
                            {
                                customCommand = (customCommand == null) ? guildSettings.getCustomCommandSettings().getCommand(selection[1]) : customCommand; //Just in case it was an add operation, in which case, we need to set the reference to the now existing command.
                                editCommand(msgEvent, customCommand);
                                return; //Assumedly, they won't want to go back to the root menu.
                            }
                            else if (responseType == CCResponseType.QUIT)
                            {
                                return;
                            }
                        }
                        else
                        {
                            selectRootSubmenu(msgEvent, customCommand, selection[0].toLowerCase(), null);
                            return;
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

    }

    /**
     * Runs the command through parameters
     * @param msgEvent
     * @param parameters
     */
    private void parseAsParameters(GuildMessageReceivedEvent msgEvent, String[] parameters)
    {
        try
        {
            Stack<String> paramStack = new Stack<>();
            for (int i = parameters.length-1; i>0; i--) //Down to i>0 to exclude the CCM key
            {
                paramStack.push(parameters[i]);
            }
            if (paramStack.size() >= 2)
            {
                String menuSelection = paramStack.pop();
                String commandName = paramStack.pop();
                CustomCommandBuilder customCommand = guildSettings.getCustomCommandSettings().getCommand(commandName);

                CCResponseType responseType = selectRootSubmenu(msgEvent, customCommand, menuSelection, commandName);
                if (responseType == CCResponseType.VALID)
                {
                    customCommand = (customCommand == null) ? guildSettings.getCustomCommandSettings().getCommand(commandName) : customCommand; //Just in case it was an add operation, in which case, we need to set the reference to the now existing command.
                    if (paramStack.size() == 0)
                    {
                        editCommand(msgEvent, customCommand);
                    }
                    else
                    {
                        String submenuSelection = paramStack.pop();
                        StringBuilder valueBuilder = new StringBuilder();
                        while (!paramStack.empty())
                        {
                            valueBuilder.append(paramStack.pop()).append(" ");
                        }
                        String value = (valueBuilder.length() > 0) ? valueBuilder.toString().trim() : null;
                        selectCommandSubmenu(msgEvent, customCommand, submenuSelection, value);
                    }
                    save(customCommand);
                }
            }
            else
            {
                EmbedBuilder embed = getEmbedStyle(msgEvent);
                embed.setDescription("You have to specify an option and a command. `[Option] [Command Name]`");
                msgEvent.getChannel().sendMessage(embed.build()).queue();
            }
        }
        catch (IOException e)
        {
            EmbedBuilder embed = getEmbedStyle(msgEvent);
            embed.setDescription("An error occurred when saving settings.");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
    }

    /**
     * Selects a submenu from the root, given the parameters provided.
     * @param msgEvent the context
     * @param customCommand the custom command, can be null
     * @param menuSelection the submenu to get
     * @param commandName the name of the custom command
     * @return
     * Valid - Edit Menu<br>
     * Quit - Other menu, operation completed here<br>
     * Invalid - Invalid selection<br>
     * @throws IOException
     */
    private CCResponseType selectRootSubmenu(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand, String menuSelection, String commandName) throws IOException
    {
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        switch (menuSelection.toLowerCase().trim())
        {
            case "add":
                if (customCommand != null)
                {
                    embed.setDescription("That command already exists. Opening command for editing...");
                    msgEvent.getChannel().sendMessage(embed.build()).queue();
                }
                else
                {
                    ArrayList<String> everyoneRole = new ArrayList<>();
                    everyoneRole.add(msgEvent.getGuild().getPublicRole().getId());
                    guildSettings.getCustomCommandSettings().addCommand(commandName, new String[0], "No description.", ModuleRegister.Category.UTILITY, new ArrayList<>(), "", "");
                    guildSettings.addPermissions(everyoneRole, commandName);
                }
                return CCResponseType.VALID;
            case "edit":
                return CCResponseType.VALID;
            case "delete":
                guildSettings.getCustomCommandSettings().removeCommand(commandName);
                embed.setDescription("Command "+commandName+" successfully removed.");
                msgEvent.getChannel().sendMessage(embed.build()).queue();
                return CCResponseType.QUIT;
            case "quit":
                embed.setDescription("Custom Command Manager closed.");
                msgEvent.getChannel().sendMessage(embed.build()).queue();
                return CCResponseType.QUIT;
            default:
                embed.setDescription("Invalid Selection.");
                msgEvent.getChannel().sendMessage(embed.build()).queue();
                return CCResponseType.INVALID;
        }
    }

    /**
     * Selects a submenu within the command editing menu
     * @param msgEvent the context
     * @param customCommand the custom command
     * @param selection the submenu to get
     * @param value the value to pass to the submenu
     * @return
     * Valid - Edit Menu<br>
     * Quit - Other menu, operation completed here<br>
     * Invalid - Invalid selection<br>
     */
    private CCResponseType selectCommandSubmenu(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand, String selection, String value)
    {
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        switch (selection)
        {
            case "aliases":
                modifyAliases(msgEvent, customCommand, value);
                return CCResponseType.VALID;
            case "description":
                modifyDescription(msgEvent, customCommand, value);
                return CCResponseType.VALID;
            case "category":
                modifyCategory(msgEvent, customCommand, value);
                return CCResponseType.VALID;
            case "message":
                setNewMessage(msgEvent, customCommand, value);
                return CCResponseType.VALID;
            case "roles":
                setNewRolesToggle(msgEvent, customCommand, value);
                return CCResponseType.VALID;
            case "audio":
                setNewAudioLink(msgEvent, customCommand, value);
                return CCResponseType.VALID;
            case "quit":
                boolean saveSuccess = save(customCommand);
                String quitMessage = (saveSuccess) ? "Command saved successfully." : "An error occurred when saving the command.";
                embed.setDescription(quitMessage);
                msgEvent.getChannel().sendMessage(embed.build()).queue();
                return CCResponseType.QUIT;
            default:
                embed.setDescription("Invalid menu selection. Please try again.");
                msgEvent.getChannel().sendMessage(embed.build()).queue();
                return CCResponseType.INVALID;
        }
    }

    /**
     * Opens the command menu
     * @param msgEvent the context
     * @param customCommand the command
     */
    private void editCommand(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand)
    {
        while (true)
        {
            EmbedBuilder embed = getEmbedStyle(msgEvent);
            if (customCommand == null)
            {
                embed.setDescription("That command does not exist.");
                msgEvent.getChannel().sendMessage(embed.build()).queue();
                return;
            }
            else
            {
                embed.setDescription("What would you like to modify?\n**Aliases**\n**Description**\n**Category**\n\n**Message**\n**Audio**\n**Roles**\n\n**Quit**");
                msgEvent.getChannel().sendMessage(embed.build()).queue();

                Message message = mm.getNextMessage(msgEvent.getChannel());
                String selection = message.getContentDisplay().toLowerCase();

                if (guildSettings.isPermitted(message.getMember(), getClass()))
                {
                    CCResponseType responseType = selectCommandSubmenu(msgEvent, customCommand, selection, null);
                    if (responseType == CCResponseType.QUIT)
                    {
                        return;
                    }
                }
            }

        }
    }

    /**
     * Saves the custom command
     * @param customCommand the command
     * @return
     */
    private boolean save(CustomCommandBuilder customCommand)
    {
        try
        {
            guildSettings.getCustomCommandSettings().editCommand(customCommand.getKey(), customCommand);
            return true;
        }
        catch (IOException e)
        {
            LoggerFactory.getLogger(getClass()).error(e.toString());
            return false;
        }
    }

    /**
     * Modifies the aliases for the custom command.
     * @param msgEvent the context
     * @param customCommand the command
     * @param value the pre-defined data to enter
     */
    private void modifyAliases(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand, String value)
    {
        CCResponseType responseType = getResponseType(value);
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        if (value == null)
        {
            StringBuilder descBuilder = new StringBuilder();
            descBuilder.append("Please enter the aliases you would like to add/remove. Separate them with spaces.\n\nExisting Aliases:\n");
            for (String alias : customCommand.getAliases())
            {
                descBuilder.append(alias).append(", ");
            }
            embed.setDescription(descBuilder.toString().substring(0, descBuilder.length()-2));
            msgEvent.getChannel().sendMessage(embed.build()).queue();

            Message message = null;
            responseType = CCResponseType.INVALID_PERMISSIONS;
            while (responseType == CCResponseType.INVALID_PERMISSIONS)
            {
                message = mm.getNextMessage(msgEvent.getChannel());
                responseType = getResponseType(message);
            }
            value = message.getContentDisplay();
        }

        if (responseType == CCResponseType.VALID)
        {
            customCommand.modifyAliases(value.split(" "));
            embed.setDescription("Aliases updated!"); //TODO: List current aliases
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
        else if (responseType == CCResponseType.RESET)
        {
            customCommand.modifyAliases(customCommand.getAliases());
            embed.setDescription("Aliases removed!");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
    }

    /**
     * Modifies the description for the custom command.
     * @param msgEvent the context
     * @param customCommand the command
     * @param value the pre-defined data to enter
     */
    private void modifyDescription(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand, String value)
    {
        CCResponseType responseType = getResponseType(value);
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        if (value == null)
        {
            embed.setDescription("Please enter the new description.\n\nExisting description: " + customCommand.getDescription());
            msgEvent.getChannel().sendMessage(embed.build()).queue();

            Message message = null;
            responseType = CCResponseType.INVALID_PERMISSIONS;
            while (responseType == CCResponseType.INVALID_PERMISSIONS)
            {
                message = mm.getNextMessage(msgEvent.getChannel());
                responseType = getResponseType(message);
            }
            value = message.getContentRaw();
        }

        if (responseType == CCResponseType.VALID)
        {
            customCommand.setDescription(value);
            embed.setDescription("Description updated!");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
        else if (responseType == CCResponseType.RESET)
        {
            customCommand.setDescription("");
            embed.setDescription("Description removed!");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
    }

    /**
     * Modifies the {@link jara.ModuleRegister.Category} for the custom command.
     * @param msgEvent the context
     * @param customCommand the command
     * @param value the pre-defined data to enter
     */
    private void modifyCategory(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand, String value)
    {
        CCResponseType responseType = getResponseType(value);
        ModuleRegister.Category category = customCommand.getCategory();
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        if (value == null)
        {
            embed.setDescription("Please enter the new category out of:\n**Admin**\n**Audio**\n**Games**\n**Toys**\n**Utility**\n\nCurrent Category: "+ ModuleRegister.getCategoryName(customCommand.getCategory()));
            msgEvent.getChannel().sendMessage(embed.build()).queue();

            Message message = null;
            responseType = CCResponseType.INVALID_PERMISSIONS;
            while (responseType == CCResponseType.INVALID_PERMISSIONS || responseType == CCResponseType.RESET || category == null)
            {
                message = mm.getNextMessage(msgEvent.getChannel());
                responseType = getResponseType(message);
                category = ModuleRegister.getCategoryID(message.getContentDisplay());
                if (category == null)
                {
                    embed.setDescription("Invalid category. Please try again, or use the quit command.");
                    msgEvent.getChannel().sendMessage(embed.build()).queue();
                }
            }
        }
        else
        {
            category = ModuleRegister.getCategoryID(value);
            if (category == null)
            {
                embed.setDescription("Invalid category. Please try again.");
                msgEvent.getChannel().sendMessage(embed.build()).queue();
                return;
            }
        }
        if (responseType == CCResponseType.VALID)
        {
            customCommand.setCategory(category);
            embed.setDescription("Category set to: "+category.name());
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }

    }

    /**
     * Modifies the message for the custom command.
     * @param msgEvent the context
     * @param customCommand the command
     * @param value the pre-defined data to enter
     */
    private void setNewMessage(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand, String value)
    {
        CCResponseType responseType = getResponseType(value);
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        if (value == null)
        {
            embed.setDescription("Please enter the new message, or `disable`.");
            if (customCommand.getMessage() != null && !customCommand.getMessage().equals(""))
            {
                embed.getDescriptionBuilder().append("\n\nExisting message: ").append(customCommand.getMessage());
            }
            msgEvent.getChannel().sendMessage(embed.build()).queue();

            Message message = null;
            responseType = CCResponseType.INVALID_PERMISSIONS;
            while (responseType == CCResponseType.INVALID_PERMISSIONS)
            {
                message = mm.getNextMessage(msgEvent.getChannel());
                responseType = getResponseType(message);
            }
            value = message.getContentRaw();
        }
        if (responseType == CCResponseType.VALID)
        {
            customCommand.setMessage(value);
            embed.setDescription("Message set!");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }

        else if (responseType == CCResponseType.RESET)
        {
            customCommand.setMessage("");
            embed.setDescription("Message removed!");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }


    }

    /**
     * Modifies the toggled roles for the custom command.
     * @param msgEvent the context
     * @param customCommand the command
     * @param value the pre-defined data to enter
     */
    private void setNewRolesToggle(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand, String value)
    {
        CCResponseType responseType = getResponseType(value);
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        if (value == null)
        {
            StringBuilder descBuilder = new StringBuilder();
            descBuilder.append("Please enter the roles (by name) you would like to add/remove, separated by commas, or `disable`.");
            if (customCommand.getRoles() != null && customCommand.getRoles().size() > 0)
            {
                descBuilder.append("\n\nExisting Roles:\n");
                for (String roleID : customCommand.getRoles())
                {
                    Role role = msgEvent.getGuild().getRoleById(roleID);
                    if (role != null)
                    {
                        descBuilder.append(role.getName()).append(", ");
                    }

                }
                embed.setDescription(descBuilder.toString().substring(0, descBuilder.length()-2));
            }
            embed.setDescription(descBuilder.toString());
            msgEvent.getChannel().sendMessage(embed.build()).queue();

            Message message = null;
            responseType = CCResponseType.INVALID_PERMISSIONS;
            while (responseType == CCResponseType.INVALID_PERMISSIONS)
            {
                message = mm.getNextMessage(msgEvent.getChannel());
                responseType = getResponseType(message);
            }
            value = message.getContentDisplay();
        }
        if (responseType == CCResponseType.VALID)
        {
            String[] newRoles = value.toLowerCase().split(",");
            ArrayList<String> newRoleIDs = new ArrayList<>();
            for (String roleName : newRoles)
            {
                newRoleIDs.add(msgEvent.getGuild().getRolesByName(roleName, true).get(0).getId());
            }
            customCommand.modifyRoles(newRoleIDs.toArray(newRoles));
            embed.setDescription("Roles set!"); //TODO: Show roles
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
        else if (responseType == CCResponseType.RESET)
        {
            customCommand.modifyRoles(customCommand.getRoles().toArray(new String[0])); //If this method finds it already has the roles, it removes them. By plugging in all existing roles, it removes all of them.
            embed.setDescription("Role toggle disabled!");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
    }

    /**
     * Modifies the audio for the custom command.
     * @param msgEvent the context
     * @param customCommand the command
     * @param value the pre-defined data to enter
     */
    private void setNewAudioLink(GuildMessageReceivedEvent msgEvent, CustomCommandBuilder customCommand, String value)
    {
        CCResponseType responseType = getResponseType(value);
        EmbedBuilder embed = getEmbedStyle(msgEvent);
        if (value == null)
        {
            embed.setDescription("Please enter the new audio track link, or `disable`.");
            if (customCommand.getAudioLink() != null && !customCommand.getAudioLink().equals(""))
            {
                embed.getDescriptionBuilder().append("\n\nExisting link: ").append(customCommand.getAudioLink());
            }
            msgEvent.getChannel().sendMessage(embed.build()).queue();

            Message message = null;
            responseType = CCResponseType.INVALID_PERMISSIONS;
            while (responseType == CCResponseType.INVALID_PERMISSIONS)
            {
                message = mm.getNextMessage(msgEvent.getChannel());
                responseType = getResponseType(message);
            }
            value = message.getContentDisplay();
        }
        if (responseType == CCResponseType.VALID)
        {
            customCommand.setAudioLink(value);
            embed.setDescription("Audio set!");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
        else if (responseType == CCResponseType.RESET)
        {
            customCommand.setAudioLink("");
            embed.setDescription("Audio disabled!");
            msgEvent.getChannel().sendMessage(embed.build()).queue();
        }
    }

    /**
     * Gets the validity of a response, including a permissions check
     * @param message the message to check
     * @return
     */
    private CCResponseType getResponseType(Message message)
    {
        if (message == null)
        {
            return CCResponseType.INVALID;
        }

        if (guildSettings.isPermitted(message.getMember(), getClass()))
        {
            String messageContent = message.getContentDisplay().toLowerCase();
            return getResponseType(messageContent);
        }
        else
        {
            return CCResponseType.INVALID_PERMISSIONS;
        }
    }

    /**
     * Gets the validity of a message's content. As such, it will not run a permissions check.
     * @param messageContent the content of a message
     * @return
     */
    private CCResponseType getResponseType(String messageContent)
    {
        if (messageContent == null)
        {
            return CCResponseType.INVALID;
        }

        if (messageContent.equalsIgnoreCase(guildSettings.getCommandPrefix()+"quit") || messageContent.equals("quit"))
        {
            return CCResponseType.QUIT;
        }
        else if (messageContent.equals("none") || messageContent.equals("none.") || messageContent.equals("disable") || messageContent.equals("reset"))
        {
            return CCResponseType.RESET;
        }
        else
        {
            return CCResponseType.VALID;
        }
    }

    /**
     * Gets the standard embed style for this command
     * @param msgEvent
     * @return
     */
    private EmbedBuilder getEmbedStyle(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(CmdUtil.getHighlightColour(msgEvent.getGuild().getSelfMember()));
        return embed;
    }
}
