package com.zazsona.jara.listeners;

import com.zazsona.jara.ModuleAttributes;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CommandListener extends JaraListener
{
    /**
     * Fires when a command is received and successfully executed
     * @param msgEvent the context of the command
     * @param moduleAttributes the command's module
     */
    public void onCommandSuccess(GuildMessageReceivedEvent msgEvent, ModuleAttributes moduleAttributes)
    {

    }

    /**
     * Fires when a command is received but not executed, due to permissions or being disabled
     * @param msgEvent the context of the command
     * @param moduleAttributes the command's module
     */
    public void onCommandFailure(GuildMessageReceivedEvent msgEvent, ModuleAttributes moduleAttributes)
    {

    }
}
