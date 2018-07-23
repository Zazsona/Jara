package configuration;

import java.util.ArrayList;


public class JsonFormats 
{
	public class GlobalSettingsJson
	{
		String token;
		GlobalCommandConfigJson[] commandConfig;
	}
	public class GlobalCommandConfigJson
	{
		String commandKey;
		boolean enabled;
	}
	public class GuildSettingsJson
	{
		String gameCategoryID;
		GuildCommandConfigJson[] commandConfig;
	}
	public class GuildCommandConfigJson
	{
		String commandKey;
		boolean enabled;
		ArrayList<String> roleIDs;
	}
}
