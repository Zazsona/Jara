package configuration;

import jara.CommandRegister;
import jara.Core;

import java.util.ArrayList;


public class JsonFormats 
{
	public abstract class SettingsJson
	{
		//Empty (for now...)
	}
	public abstract class CommandConfigJson
	{
		private String commandKey;
		private boolean enabled;

		protected CommandConfigJson(String commandKey, boolean enabled)
		{
			this.commandKey = commandKey;
			setEnabled(enabled);
		}
		public String getCommandKey()
		{
			return commandKey;
		}
		public boolean isEnabled()
		{
			return enabled;
		}
		public boolean setEnabled(boolean newState)
		{
			if (!newState && CommandRegister.getCommand(getCommandKey()).isDisableable())
			{
				enabled = false;
			}
			else
			{
				enabled = true;
			}
			return isEnabled();
		}
	}


	public class GlobalSettingsJson extends SettingsJson
	{
		private String token;
		private GlobalCommandConfigJson[] commandConfig;
		public String getToken()
		{
			return token;
		}
		public void setToken(String newToken)
		{
			Core.initialiseDiscordConnection(newToken); //Verify the new token works (If it does not, this method recursively calls for a new token)
			Core.getShardManager().shutdown();
			token = newToken;
		}
		public GlobalCommandConfigJson[] getCommandConfig()
		{
			return commandConfig;
		}
		public void setCommandConfig(GlobalCommandConfigJson[] commandConfig)
		{
			this.commandConfig = commandConfig;
		}
	}
	public class GlobalCommandConfigJson extends CommandConfigJson
	{
		public GlobalCommandConfigJson(String commandKey, boolean enabled)
		{
			super(commandKey, enabled);
		}
	}
	public class GuildSettingsJson extends SettingsJson
	{
		private String gameCategoryID;
		private GuildCommandConfigJson[] commandConfig;

		public String getGameCategoryID()
		{
			return gameCategoryID;
		}
		public void setGameCategoryID(String newID)
		{
			gameCategoryID = newID;
		}

		public GuildCommandConfigJson[] getCommandConfig()
		{
			return commandConfig;
		}
		public void setCommandConfig(GuildCommandConfigJson[] commandConfig)
		{
			this.commandConfig = commandConfig;
		}

	}
	public class GuildCommandConfigJson extends CommandConfigJson
	{
		private ArrayList<String> roleIDs;

		public GuildCommandConfigJson(String commandKey, boolean enabled, ArrayList<String> roleIDs)
		{
			super(commandKey, enabled);
			this.roleIDs = roleIDs;
		}

		public ArrayList<String> getPermittedRoles()
		{
			return roleIDs;
		}



	}
}
