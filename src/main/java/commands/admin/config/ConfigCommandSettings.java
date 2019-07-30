package commands.admin.config;

import configuration.GuildSettings;
import jara.CommandAttributes;
import jara.CommandRegister;
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

    public ConfigCommandSettings(GuildSettings guildSettings, TextChannel channel)
    {
        this.guildSettings = guildSettings;
        this.channel = channel;
        msgManager = new MessageManager();
    }

    public void parseAsParameters(GuildMessageReceivedEvent msgEvent, String[] parameters) throws IOException
    {
        if (parameters.length > 2)
        {
            EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
            CommandAttributes ca = CommandRegister.getCommand(parameters[2]);
            if (ca == null)
            {
                ca = guildSettings.getCustomCommandAttributes(parameters[2].toLowerCase());
                if (ca == null)
                {
                    embed.setDescription("Command "+parameters[2]+" not found.");
                    channel.sendMessage(embed.build()).queue();
                    return;
                }
            }
            else
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
                                modifyRoles(embed, ca, roles.toString().trim(), msgEvent.getMessage().getMentionedRoles());
                            }
                            else
                            {
                                for (int i = 4; i<parameters.length; i++)
                                {
                                    roles.append(parameters[i]).append(" ");
                                }
                                modifyRoles(embed, ca, roles.toString().trim());
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
                        modifyState(embed, ca, true);
                    }
                    else if (commandTask.equals("disable"))
                    {
                        modifyState(embed, ca, false);
                    }
                    else
                    {
                        embed.setDescription("Unrecognised command config option.");
                        channel.sendMessage(embed.build()).queue();
                    }
                }
                else
                {
                    showMenu(ca, embed);
                }
            }
        }
        else
        {
            getCommand(msgEvent);
        }
    }

    public void getCommand(GuildMessageReceivedEvent msgEvent) throws IOException
    {
        CommandAttributes ca;
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        embed.setDescription("Please enter the command you would like to modify.");
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = msgManager.getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                if (!(((ca = CommandRegister.getCommand(msg.getContentDisplay())) == null) && ((ca = guildSettings.getCustomCommandAttributes(msg.getContentDisplay())) == null))) //Ensure CommandAttributes is not null
                {
                    showMenu(ca, embed);
                    break;
                }
                else if (msg.getContentDisplay().equalsIgnoreCase("quit"))
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

    public void showMenu(CommandAttributes ca, EmbedBuilder embed) throws IOException
    {
        while (true)
        {
            embed.setDescription(getCommandProfile(ca));
            channel.sendMessage(embed.build()).queue();

            Message msg = msgManager.getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                String request = msg.getContentDisplay().toLowerCase();
                if (request.startsWith("addroles") || request.startsWith("removeroles"))
                {
                    if (msg.getMentionedRoles().size() > 0)
                    {
                        modifyRoles(embed, ca, request, msg.getMentionedRoles());
                    }
                    else
                    {
                        modifyRoles(embed, ca, request);
                    }
                }
                else if (request.equals("enable"))
                {
                    modifyState(embed, ca, true);
                }
                else if (request.equals("disable"))
                {
                    modifyState(embed, ca, false);
                }
                else if (request.startsWith("quit"))
                {
                    break;
                }
                else
                {
                    embed.setDescription("Unrecognised option. Please try again.");
                    channel.sendMessage(embed.build()).queue();
                }
            }
        }
    }

    private void modifyState(EmbedBuilder embed, CommandAttributes ca, boolean newState) throws IOException
    {
        guildSettings.setCommandConfiguration(newState, null, ca.getCommandKey());
        embed.setDescription((newState) ? ca.getCommandKey()+" is now enabled." : ca.getCommandKey()+" is now disabled.");
        channel.sendMessage(embed.build()).queue();
    }

    private void modifyRoles(EmbedBuilder embed, CommandAttributes ca, String request, List<Role> roles) throws IOException
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
            success = guildSettings.addPermissions(roleIDs, ca.getCommandKey());
        }
        else if (request.startsWith("removeroles"))
        {
            success = guildSettings.removePermissions(roleIDs, ca.getCommandKey());
        }
        embed.setDescription((success) ? "Roles have been updated for "+ca.getCommandKey()+"." : "No roles have been changed for "+ca.getCommandKey()+".");
        channel.sendMessage(embed.build()).queue();
    }

    private void modifyRoles(EmbedBuilder embed, CommandAttributes ca, String request) throws IOException
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
            success = guildSettings.addPermissions(roleIDs, ca.getCommandKey());
        }
        else if (request.startsWith("removeroles"))
        {
            success = guildSettings.removePermissions(roleIDs, ca.getCommandKey());
        }

        embed.setDescription((success) ? "Roles have been updated for "+ca.getCommandKey()+"." : "No roles have been changed for "+ca.getCommandKey()+".");
        channel.sendMessage(embed.build()).queue();
    }

    private String getCommandProfile(CommandAttributes ca)
    {
        StringBuilder profileBuilder = new StringBuilder();
        profileBuilder.append("**Command:** ").append(ca.getCommandKey()).append("\n");
        profileBuilder.append("**Description:** ").append(ca.getDescription()).append("\n");
        profileBuilder.append("**Enabled:** ");
        if (guildSettings.isCommandEnabled(ca.getCommandKey()))
        {
            profileBuilder.append("Yes\n");
        }
        else
        {
            profileBuilder.append("No\n");
        }
        profileBuilder.append("**Roles:** ");
        for (String roleID : guildSettings.getPermissions(ca.getCommandKey()))
        {
            profileBuilder.append(channel.getGuild().getRoleById(roleID).getName().replace("@", "")).append(", "); //removing "@" prevents pinging with the @everyone role.
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
