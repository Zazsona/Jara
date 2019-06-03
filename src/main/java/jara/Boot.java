package jara;

import configuration.SettingsUtil;
import gui.HeadedGUI;
import org.slf4j.LoggerFactory;

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
	}
	private static void connectToDiscord()
	{
		try
		{
			boolean loggedIn = Core.initialiseDiscordConnection(SettingsUtil.getGlobalSettings().getToken());
			if (!loggedIn)
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
				loggedIn = Core.initialiseDiscordConnection(SettingsUtil.getGlobalSettings().getToken());
				if (!loggedIn)
				{
					System.exit(0); //User closed the window before entering a valid token.
				}
			}
		}
		catch (IOException e)
		{
			LoggerFactory.getLogger(Boot.class).error("Could not save token. Please try again.");
			System.exit(1);
		}

	}
}
