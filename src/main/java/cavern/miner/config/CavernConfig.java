package cavern.miner.config;

public class CavernConfig
{
	public static final VeinConfig VEINS = new VeinConfig("cavern");
	public static final VeinBlacklistConfig VEINS_BLACKLIST = new VeinBlacklistConfig("cavern");

	public static void loadConfig()
	{
		VEINS.loadFromFile();

		VEINS_BLACKLIST.loadFromFile();

		if (VEINS_BLACKLIST.getBlacklist().isEmpty())
		{
			VEINS_BLACKLIST.setDefault();
			VEINS_BLACKLIST.saveToFile();
		}
	}
}