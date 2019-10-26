package commands.admin.config;

import configuration.GuildSettings;
import configuration.SettingsUtil;
import jara.ModuleAttributes;
import jara.ModuleRegister;
import jara.MessageManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ConfigCommandSettings
{
    private final GuildSettings guildSettings;
    private final TextChannel channel;
    private final MessageManager msgManager;

    /**
     * Constructor
     * @param guildSettings the guild settings to modify
     * @param channel the channel to run on
     */
    public ConfigCommandSettings(GuildSettings guildSettings, TextChannel channel)
    {
        this.guildSettings = guildSettings;
        this.channel = channel;
        msgManager = new MessageManager();
    }

    /**
     * Runs through the config using the navigation options supplied in a single message
     * @param msgEvent context
     * @param parameters the parameters to parse
     * @throws IOException unable to write to file
     */
    public void parseAsParameters(GuildMessageReceivedEvent msgEvent, String[] parameters) throws IOException
    {
        if (parameters.length > 2)
        {
            EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
            ModuleAttributes ma = ModuleRegister.getModule(parameters[2]);
            if (ma == null)
            {
                ma = guildSettings.getCustomCommandSettings().getCommandAttributes(parameters[2].toLowerCase());
                if (ma == null)
                {
                    embed.setDescription("Command "+parameters[2]+" not found.");
                    channel.sendMessage(embed.build()).queue();
                    return;
                }
            }
            else if (ma.getCommandClass() != null)
            {
                if (parameters.length > 3)
                {
                    String commandTask = parameters[3].toLowerCase();
                    if (commandTask.startsWith("addroles") || commandTask.startsWith("removeroles"))
                    {
                        StringBuilder roles = new StringBuilder();
                        if (parameters.length > 4)
                        {
                            if (commandTask.startsWith("add"))
                                roles.append("addroles ");
                            else
                                roles.append("removeroles ");

                            if (msgEvent.getMessage().getMentionedRoles().size() > 0)
                            {
                                modifyRoles(embed, ma, roles.toString().trim(), msgEvent.getMessage().getMentionedRoles());
                            }
                            else
                            {
                                for (int i = 4; i<parameters.length; i++)
                                {
                                    roles.append(parameters[i]).append(" ");
                                }
                                modifyRoles(embed, ma, roles.toString().trim());
                            }
                        }
                        else
                        {
                            embed.setDescription("You haven't specified any roles!");
                            channel.sendMessage(embed.build()).queue();
                        }
                    }
                    else if (commandTask.equals("enable"))
                    {
                        modifyState(embed, ma, true);
                    }
                    else if (commandTask.equals("disable"))
                    {
                        modifyState(embed, ma, false);
                    }
                    else
                    {
                        embed.setDescription("Unrecognised command config option.");
                        channel.sendMessage(embed.build()).queue();
                    }
                }
                else
                {
                    showMenu(ma, embed);
                }
            }
            else
            {
                embed.setDescription("No command is available for module "+ma.getKey()+".");
                channel.sendMessage(embed.build()).queue();
            }
        }
        else
        {
            getCommand(msgEvent);
        }
    }

    /**
     * Prompts the user to select a command, and loads that command's config
     * @param msgEvent context
     * @throws IOException unable to write to file
     */
    public void getCommand(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        ModuleAttributes ma;
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        embed.setDescription("Please enter the command you would like to modify.");
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = msgManager.getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                if (!(((ma = ModuleRegister.getModule(msg.getContentDisplay())) == null) && ((ma = guildSettings.getCustomCommandSettings().getCommandAttributes(msg.getContentDisplay())) == null))) //Ensure ModuleAttributes is not null
                {
                    if (ma.getCommandClass() != null)
                    {
                        showMenu(ma, embed);
                        break;
                    }
                    else
                    {
                        embed.setDescription("No command is available for module "+ma.getKey()+".");
                        channel.sendMessage(embed.build()).queue();
                    }
                }
                else if (msg.getContentDisplay().equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || msg.getContentDisplay().equalsIgnoreCase("quit"))
                {
                    return;
                }
                else
                {
                    embed.setDescription("Unrecognised command. Please try again.");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }

    /**
     * Shows the main navigation menu for a command, and directs the user to a submenu or exiting.
     * @param ma the module attributes for the command
     * @param embed the embed style
     * @throws IOException unable to write to file
     */
    public void showMenu(ModuleAttributes ma, EmbedBuilder embed) throws IOException
    {
        while (true)
        {
            embed.setDescription(getCommandProfile(ma));
            channel.sendMessage(embed.build()).queue();

            Message msg = msgManager.getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                String request = msg.getContentDisplay().toLowerCase();
                if (request.startsWith("addroles") || request.startsWith("removeroles"))
                {
                    if (msg.getMentionedRoles().size() > 0)
                    {
                        modifyRoles(embed, ma, request, msg.getMentionedRoles());
                    }
                    else
                    {
                        modifyRoles(embed, ma, request);
                    }
                }
                else if (request.equals("enable"))
                {
                    modifyState(embed, ma, true);
                }
                else if (request.equals("disable"))
                {
                    modifyState(embed, ma, false);
                }
                else if (request.equalsIgnoreCase(SettingsUtil.getGuildCommandPrefix(msg.getGuild().getId())+"quit") || request.equalsIgnoreCase("quit"))
                {
                    return;
                }
                else
                {
                    embed.setDescription("Unrecognised option. Please try again.");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }

    /**
     * Modifies the enabled state of a command
     * @param embed the embed style
     * @param ma the module attributes of the command
     * @param newState the new enabled state
     * @throws IOException unable to write to file
     */
    private void modifyState(EmbedBuilder embed, ModuleAttributes ma, boolean newState) throws IOException
    {
        guildSettings.setCommandConfiguration(newState, null, ma.getKey());
        embed.setDescription((newState) ? ma.getKey()+" is now enabled." : ma.getKey()+" is now disabled.");
        channel.sendMessage(embed.build()).queue();
    }

    /**
     * Modifies the enabled state of a command
     * @param embed the embed style
     * @param ma the module attributes of the command
     * @param request the message content, containing "addroles" or "removeroles"
     * @param roles the roles to modify
     * @throws IOException unable to write to file
     */
    private void modifyRoles(EmbedBuilder embed, ModuleAttributes ma, String request, List<Role> roles) throws IOException
    {
        request = request.toLowerCase();
        ArrayList<String> roleIDs = new ArrayList<>();
        for (Role role : roles)
        {
            roleIDs.add(role.getId());
        }
        boolean success = false;
        if (request.startsWith("addroles"))
        {
            success = guildSettings.addPermissions(roleIDs, ma.getKey());
        }
        else if (request.startsWith("removeroles"))
        {
            success = guildSettings.removePermissions(roleIDs, ma.getKey());
        }
        embed.setDescription((success) ? "Roles have been updated for "+ma.getKey()+"." : "No roles have been changed for "+ma.getKey()+".");
        channel.sendMessage(embed.build()).queue();
    }

    /**
     * Modifies the enabled state of a command
     * @param embed the embed style
     * @param ma the module attributes of the command
     * @param request the message content, containing "addroles" or "removeroles"
     * @throws IOException unable to write to file
     */
    private void modifyRoles(EmbedBuilder embed, ModuleAttributes ma, String request) throws IOException
    {
        request = request.toLowerCase();
        String requestedRoles = request.replace("addroles ", "").replace("removeroles ", "");
        String[] params = requestedRoles.split(",");
        ArrayList<String> roleIDs = new ArrayList<>();
        for (String roleName : params)
        {
            List<Role> rolesWithName = channel.getGuild().getRolesByName(roleName.trim(), true);
            if (roleName.equalsIgnoreCase("everyone") && rolesWithName.size() == 0)
            {
                rolesWithName.add(channel.getGuild().getPublicRole());
            }

            if (rolesWithName.size() > 0)
            {
                rolesWithName.forEach((role) -> roleIDs.add(role.getId()));
            }
            else if (Pattern.matches("[0-9]*", roleName)) //Is it an ID?
            {
                roleIDs.add(roleName);
            }
        }
        boolean success = false;
        if (request.startsWith("addroles"))
        {
            success = guildSettings.addPermissions(roleIDs, ma.getKey());
        }
        else if (request.startsWith("removeroles"))
        {
            success = guildSettings.removePermissions(roleIDs, ma.getKey());
        }

        embed.setDescription((success) ? "Roles have been updated for "+ma.getKey()+"." : "No roles have been changed for "+ma.getKey()+".");
        channel.sendMessage(embed.build()).queue();
    }

    /**
     * Gets the command details and menu controls
     * @param ma the command's module attributes
     * @return menu content to display
     */
    private String getCommandProfile(ModuleAttributes ma)
    {
        StringBuilder profileBuilder = new StringBuilder();
        profileBuilder.append("**Command:** ").append(ma.getKey()).append("\n");
        profileBuilder.append("**Description:** ").append(ma.getDescription()).append("\n");
        profileBuilder.append("**Configurable:** ").append((ma.getConfigClass() != null) ? "Yes" : "No").append("\n");
        profileBuilder.append("**Enabled:** ").append((guildSettings.isCommandEnabled(ma.getKey())) ? "Yes" : "No").append("\n");
        profileBuilder.append("**Roles:** ");
        for (String roleID : guildSettings.getPermissions(ma.getKey()))
        {
            Role role = channel.getGuild().getRoleById(roleID);
            if (role != null)
            {
                profileBuilder.append(role.getName().replace("@", "")).append(", "); //removing "@" prevents pinging with the @everyone role.
            }

        }
        profileBuilder.append("\n==========\n");
        profileBuilder.append("**Controls:**\n");
        profileBuilder.append("Enable/Disable\n");
        profileBuilder.append("AddRoles [RoleName1], [RoleName2], ...[RoleNameN]\n");
        profileBuilder.append("RemoveRoles [RoleName1], [RoleName2], ...[RoleNameN]\n");
        profileBuilder.append("Quit");
        return profileBuilder.toString();
    }
}
