package cavern.miner.world.spawner;

import cavern.miner.config.dimension.CavernConfig;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;

public class CavernMobSpawner extends CaveMobSpawner
{
	public CavernMobSpawner(ServerWorld world)
	{
		super(world);
	}

	@Override
	public int getChunkRadius(PlayerEntity player)
	{
		return CavernConfig.INSTANCE.chunkRadius.get();
	}

	@Override
	public int getHeightRadius(EntityClassification type)
	{
		return CavernConfig.INSTANCE.heightRadius.get();
	}

	@Override
	public int getMaxCount(EntityClassification type)
	{
		if (!type.getAnimal() && !type.getPeacefulCreature())
		{
			return CavernConfig.INSTANCE.maxCount.get();
		}

		return super.getMaxCount(type);
	}

	@Override
	public int getSafeDistance(EntityClassification type)
	{
		return CavernConfig.INSTANCE.safeDistance.get();
	}
}