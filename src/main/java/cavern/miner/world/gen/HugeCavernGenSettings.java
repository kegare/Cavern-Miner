package cavern.miner.world.gen;

import cavern.miner.config.dimension.HugeCavernConfig;
import cavern.miner.init.CaveBiomes;
import net.minecraft.world.biome.Biome;

public class HugeCavernGenSettings extends CavernGenSettings
{
	@Override
	public Biome getDefaultBiome()
	{
		return CaveBiomes.HUGE_CAVERN.get();
	}

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