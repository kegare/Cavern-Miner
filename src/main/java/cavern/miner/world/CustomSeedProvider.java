package cavern.miner.world;

import javax.annotation.Nullable;

public interface CustomSeedProvider
{
	@Nullable
	CustomSeed getSeedData();
}