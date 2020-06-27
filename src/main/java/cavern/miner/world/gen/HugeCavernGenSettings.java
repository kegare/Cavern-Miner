package cavern.miner.world.gen;

import cavern.miner.config.dimension.HugeCavernConfig;

public class HugeCavernGenSettings extends CavernGenSettings
{
	@Override
	public boolean isFlatBedrock()
	{
		return HugeCavernConfig.INSTANCE.flatBedrock.get();
	}

	@Override
	public int getGroundHeight()
	{
		return 0;
	}
}