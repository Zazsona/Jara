package configuration;

import java.util.ArrayList;


public class JsonFormats 
{
	public class GlobalSettingsJson
	{
		public String token;
		public GlobalCommandConfigJson[] commandConfig;
	}
	public class GlobalCommandConfigJson
	{
		public String commandKey;
		public boolean enabled;
	}
	public class GuildSettingsJson
	{
		public String gameCategoryID;
		public GuildCommandConfigJson[] commandConfig;
	}
	public class GuildCommandConfigJson
	{
		public String commandKey;
		public boolean enabled;
		public ArrayList<String> roleIDs;
	}
}
