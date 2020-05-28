package cavern.miner.world;

import cavern.miner.config.CavernConfig;
import cavern.miner.config.manager.CaveVeinManager;

public class VeinProviderCavern extends VeinProvider
{
	@Override
	public CaveVeinManager getVeinManager()
	{
		return CavernConfig.autoVeins ? null : CavernConfig.VEINS;
	}

	@Override
	public String[] getBlacklist()
	{
		return CavernConfig.autoVeinBlacklist;
	}
}