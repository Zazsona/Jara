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
                    modifyRoles(embed, ca, request);
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

    private void modifyRoles(EmbedBuilder embed, CommandAttributes ca, String request) throws IOException
    {
        int permissionsChanged = 0;
        String[] params = request.split(" ");
        ArrayList<String> roleIDs = new ArrayList<>();

        for (String roleName : params)
        {
            if (roleName.equalsIgnoreCase("everyone"))
            {
                roleName = "@everyone";
            }
            List<Role> rolesWithName = channel.getGuild().getRolesByName(roleName, true);
            if (rolesWithName.size() > 0)
            {
                rolesWithName.forEach((role) -> roleIDs.add(role.getId()));
            }
            else if (Pattern.matches("[0-9]*", roleName)) //Is it an ID?
            {
                roleIDs.add(roleName);
            }
        }
        for (String roleID : roleIDs)
        {
            if (!guildSettings.getPermissions(ca.getCommandKey()).contains(roleID) && request.startsWith("addroles"))
            {
                guildSettings.addPermissions(roleIDs, ca.getCommandKey());
                permissionsChanged++;
            }
            else if (guildSettings.getPermissions(ca.getCommandKey()).contains(roleID) && request.startsWith("removeroles"))
            {
                guildSettings.removePermissions(roleIDs, ca.getCommandKey());
                permissionsChanged++;
            }
        }

        embed.setDescription("**"+permissionsChanged+"** roles have been updated for "+ca.getCommandKey()+".");
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
            profileBuilder.append(channel.getGuild().getRoleById(roleID).getName().replace("@", "")+", "); //removing "@" prevents pinging with the @everyone role.
        }
        profileBuilder.append("\n==========\n");
        profileBuilder.append("**Controls:**\n");
        profileBuilder.append("Enable/Disable\n");
        profileBuilder.append("AddRoles [RoleName1 RoleName2 ...RoleNameN]\n");
        profileBuilder.append("RemoveRoles [RoleName1 RoleName2 ...RoleNameN]\n");
        profileBuilder.append("Quit");
        return profileBuilder.toString();
    }
}
