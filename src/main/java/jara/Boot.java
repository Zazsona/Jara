package jara;

public class Boot 
{
	public static void main(String[] args)
	{
		//The idea here is that new elements unrelated to Discord (External dash, API starts, config setting, etc. can all be called here)
		Core.initialiseDiscordConnection(args[0]); //Pass the client token.
	}
}
