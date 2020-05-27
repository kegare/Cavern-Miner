package cavern.miner.world;

import cavern.miner.config.HugeCavernConfig;
import cavern.miner.data.WorldData;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProviderHugeCavern extends WorldProviderCavern
{
	@Override
	public IChunkGenerator createChunkGenerator()
	{
		return new ChunkGeneratorHugeCavern(world);
	}

	@Override
	public DimensionType getDimensionType()
	{
		return CaveDimensions.HUGE_CAVERN;
	}

	@Override
	public int getActualHeight()
	{
		return HugeCavernConfig.halfHeight ? 128 : 256;
	}

	@Override
	public int getMonsterSpawn()
	{
		return HugeCavernConfig.monsterSpawn;
	}

	@Override
	public double getBrightness()
	{
		return HugeCavernConfig.caveBrightness;
	}

	@Override
	public boolean canDropChunk(int x, int z)
	{
		return HugeCavernConfig.keepPortalChunk && !WorldData.get(world).hasPortal(x, z);
	}
}