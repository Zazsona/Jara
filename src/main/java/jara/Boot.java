package jara;

import configuration.SettingsUtil;
import gui.headed.HeadedGUIManager;
import javafx.application.Application;

public class Boot 
{
	public static void main(String[] args)
	{
		SettingsUtil.initialise();
		Core.initialiseDiscordConnection(SettingsUtil.getGlobalSettings().getToken()); //Once this is called, the bot appears online.
		Core.startListeners(); //Once this is called, Jara can respond to guild changes
		Core.enableCommands(); //Once this is called, commands can be used.

		CommandRegister.getCommandsInCategory(CommandRegister.ADMIN);
		CommandRegister.getCommandsInCategory(CommandRegister.ADMIN);
		Application.launch(HeadedGUIManager.class);
	}
}
