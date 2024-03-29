package com.zazsona.jara.module;

import com.zazsona.jara.commands.CmdUtil;
import com.zazsona.jara.ModuleAttributes;
import com.zazsona.jara.ModuleManager;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ModuleClass
{
    private ModuleAttributes moduleAttributes;
    /**
     * Gets the {@link ModuleAttributes} for this module.
     * @return the module attributes
     */
    public ModuleAttributes getModuleAttributes()
    {
        if (moduleAttributes == null)
            moduleAttributes = ModuleManager.getModule(getClass());

        return moduleAttributes;
    }

    /**
     * Opens the help page to for this module.
     * @param msgEvent context
     */
    private void sendHelpInfo(GuildMessageReceivedEvent msgEvent)
    {
        CmdUtil.sendHelpInfo(msgEvent, getModuleAttributes().getKey());
    }
}
