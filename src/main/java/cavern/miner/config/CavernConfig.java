package cavern.miner.config;

import java.io.File;

public class CavernConfig
{
	public static final PortalTriggerConfig PORTAL_TRIGGER = new PortalTriggerConfig(getConfigDir(), "cavern");
	public static final PortalFrameConfig PORTAL_FRAME = new PortalFrameConfig(getConfigDir(), "cavern");

	public static final VeinConfig VEINS = new VeinConfig(getConfigDir(), "cavern");
	public static final VeinBlacklistConfig VEINS_BLACKLIST = new VeinBlacklistConfig(getConfigDir(), "cavern");

	public static File getConfigDir()
	{
		return new File(CavernModConfig.getConfigDir(), "cavern");
	}

	public static void loadConfig()
	{
		PORTAL_TRIGGER.loadFromFile();

		if (PORTAL_TRIGGER.getEntries().isEmpty())
		{
			PORTAL_TRIGGER.setDefault();
			PORTAL_TRIGGER.saveToFile();
		}

		PORTAL_FRAME.loadFromFile();

		if (PORTAL_FRAME.getEntries().isEmpty())
		{
			PORTAL_FRAME.setDefault();
			PORTAL_FRAME.saveToFile();
		}

		VEINS.loadFromFile();

		VEINS_BLACKLIST.loadFromFile();

		if (VEINS_BLACKLIST.getBlacklist().isEmpty())
		{
			VEINS_BLACKLIST.setDefault();
			VEINS_BLACKLIST.saveToFile();
		}
	}
}