package jara;

import configuration.SettingsUtil;
import gui.HeadedGUI;

import java.awt.*;
import java.io.IOException;

public class Boot 
{
	public static void main(String[] args)
	{
		SettingsUtil.initialise();
		connectToDiscord(); //Once this is called, the bot appears online. If the login details are incorrect, it will hold the thread and prompt the user.
		Core.startListeners(); //Once this is called, Jara can respond to guild changes
		Core.enableCommands(); //Once this is called, commands can be used.

		CommandRegister.getCommandsInCategory(CommandRegister.ADMIN);
		CommandRegister.getCommandsInCategory(CommandRegister.ADMIN);
	}
	private static void connectToDiscord()
	{
		boolean loggedIn = Core.initialiseDiscordConnection(SettingsUtil.getGlobalSettings().getToken());
		while (!loggedIn)
		{
			String token = "";
			if (GraphicsEnvironment.isHeadless())
			{
				//token = HeadlessGUI.updateToken(); //TODO: Fix
			}
			else
			{
				token = HeadedGUI.updateToken();
			}

			SettingsUtil.getGlobalSettings().setToken(token);

			try
			{
				SettingsUtil.getGlobalSettings().save();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			loggedIn = Core.initialiseDiscordConnection(SettingsUtil.getGlobalSettings().getToken());
		}
	}
}
