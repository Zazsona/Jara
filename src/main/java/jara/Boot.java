package jara;


public class Boot 
{
	public static void main(String[] args)
	{
		Core.initialiseDiscordConnection(GlobalSettingsManager.getGlobalClientToken()); //Once this is called, the bot appears online.
		Core.enableCommands(); //Once this is called, commands can be used.
		
	}
}
