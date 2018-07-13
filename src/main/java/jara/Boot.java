package jara;

import configuration.GlobalSettingsManager;

public class Boot 
{
	public static void main(String[] args)
	{
		GlobalSettingsManager.performFirstTimeSetup(args[0]);
		Core.initialiseDiscordConnection(GlobalSettingsManager.getGlobalClientToken()); //Once this is called, the bot appears online.
		Core.startListeners(); //Once this is called, Jara can respond to guild changes
		Core.enableCommands(); //Once this is called, commands can be used.
		//TODO: Have a check for new commands not noted in configs, and add them (read config, extend commandConfig array, save config) (Show gui for global)
	}
}
