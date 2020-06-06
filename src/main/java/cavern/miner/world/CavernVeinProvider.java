package cavern.miner.world;

import cavern.miner.config.CavernConfig;
import cavern.miner.config.VeinBlacklistConfig;
import cavern.miner.config.VeinConfig;

public class CavernVeinProvider extends VeinProvider
{
	@Override
	public VeinConfig getConfig()
	{
		return CavernConfig.VEINS;
	}

	@Override
	public VeinBlacklistConfig getBlacklistConfig()
	{
		return CavernConfig.VEINS_BLACKLIST;
	}
}