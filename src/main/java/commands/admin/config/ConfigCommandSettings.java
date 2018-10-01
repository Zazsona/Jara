package commands.admin.config;

import configuration.GuildSettings;
import configuration.SettingsUtil;
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

    public void showMenu(GuildMessageReceivedEvent msgEvent)
    {
        EmbedBuilder embed = ConfigMain.getEmbedStyle(msgEvent);
        StringBuilder descBuilder = new StringBuilder();
        descBuilder.append("Please enter the command you would like to modify.\n\n");
        for (String key : SettingsUtil.getGlobalSettings().getEnabledCommands())
        {
            descBuilder.append("**").append(key).append("**\n");
        }
        embed.setDescription(descBuilder.toString());
        channel.sendMessage(embed.build()).queue();

        while (true)
        {
            Message msg = msgManager.getNextMessage(channel);
            if (guildSettings.isPermitted(msg.getMember(), ConfigMain.class)) //If the message is from someone with config permissions
            {
                if (msg.getContentDisplay().equalsIgnoreCase("/quit"))
                {
                    embed.setDescription("Config closed.");
                    channel.sendMessage(embed.build()).queue();
                    break;
                }
                CommandAttributes ca = CommandRegister.getCommand(msg.getContentDisplay());
                if (ca != null)
                {
                    descBuilder = new StringBuilder();
                    descBuilder.append("**Command:** ").append(ca.getCommandKey()).append("\n");
                    descBuilder.append("**Description:** ").append(ca.getDescription()).append("\n");
                    descBuilder.append("**Enabled:** ");
                    if (guildSettings.isCommandEnabled(ca.getCommandKey()))
                    {
                        descBuilder.append("Yes\n");
                    }
                    else
                    {
                        descBuilder.append("No\n");
                    }
                    descBuilder.append("**Roles:** ");
                    for (String roleID : guildSettings.getPermissions(ca.getCommandKey()))
                    {
                        descBuilder.append(channel.getGuild().getRoleById(roleID).getName());
                    }
                    descBuilder.append("\n==========\n");
                    descBuilder.append("**Controls:**\n");
                    descBuilder.append("Enable/Disable\n");
                    descBuilder.append("AddRoles [RoleName1 RoleName2 ...RoleNameN]\n");
                    descBuilder.append("RemoveRoles [RoleName1 RoleName2 ...RoleNameN]\n");
                    descBuilder.append("/Quit - Exit");
                    embed.setDescription(descBuilder.toString());
                    channel.sendMessage(embed.build()).queue();

                    while (true)
                    {
                        msg = msgManager.getNextMessage(channel);
                        String request = msg.getContentDisplay().toLowerCase();
                        if (request.startsWith("addroles") || request.startsWith("removeroles"))
                        {
                            int permissionsChanged = 0;
                            String[] params = request.split(" ");
                            for (String roleName : params)
                            {
                                List<Role> rolesWithName = channel.getGuild().getRolesByName(roleName, true);
                                if (rolesWithName.size() > 0)
                                {
                                    ArrayList<String> roleIDs = new ArrayList<>();
                                    for (Role role : rolesWithName)
                                    {
                                        permissionsChanged++;
                                        roleIDs.add(role.getId());
                                    }

                                    if (request.startsWith("addroles"))
                                    {
                                        guildSettings.addPermissions(roleIDs, ca.getCommandKey());
                                    }
                                    else
                                    {
                                        guildSettings.removePermissions(roleIDs, ca.getCommandKey());
                                    }
                                }
                            }
                            embed.setDescription("**"+permissionsChanged+"** roles have been updated for "+ca.getCommandKey()+".");
                            channel.sendMessage(embed.build()).queue();
                            try
                            {
                                guildSettings.save();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                                embed.setDescription("An error occurred when saving roles.");
                                e.printStackTrace();
                            }

                        }
                        else if (request.equals("enable"))
                        {
                            guildSettings.modifyCommandConfiguration(true, null, ca.getCommandKey());
                            embed.setDescription(ca.getCommandKey()+" is now enabled.");
                            channel.sendMessage(embed.build()).queue();
                            try
                            {
                                guildSettings.save();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                                embed.setDescription("An error occurred when saving state.");
                                e.printStackTrace();
                            }
                        }
                        else if (request.equals("disable"))
                        {
                            guildSettings.modifyCommandConfiguration(false, null, ca.getCommandKey());
                            embed.setDescription(ca.getCommandKey()+" is now disabled.");
                            channel.sendMessage(embed.build()).queue();
                            try
                            {
                                guildSettings.save();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                                embed.setDescription("An error occurred when saving state.");
                                e.printStackTrace();
                            }
                        }
                        else if (request.endsWith("quit"))
                        {
                            embed.setDescription("Config closed.");
                            channel.sendMessage(embed.build()).queue();
                            break;
                        }
                    }
                    break;
                }

            }
        }
    }
}
