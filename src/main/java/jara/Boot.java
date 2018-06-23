package jara;

import java.util.HashMap;

public class Boot 
{
	public static void main(String[] args)
	{
		//The idea here is that new elements unrelated to Discord (External dash, API starts, config setting, etc. can all be called here)
		Core.initialiseDiscordConnection(args[0]); //Pass the client token.
		HashMap<String, Boolean> temp = new HashMap<String, Boolean>();
		temp.put("Ping", true);						//TODO: Read this HashMap from a config file
		Core.enableCommands(temp); 
	}
}
