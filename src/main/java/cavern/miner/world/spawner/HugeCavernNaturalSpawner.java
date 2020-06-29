package cavern.miner.world.spawner;

import cavern.miner.config.dimension.HugeCavernConfig;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;

public class HugeCavernNaturalSpawner extends NaturalSpawner
{
	public HugeCavernNaturalSpawner(ServerWorld world)
	{
		super(world);
	}

	@Override
	public int getChunkRadius(PlayerEntity player)
	{
		return HugeCavernConfig.INSTANCE.chunkRadius.get();
	}

	@Override
	public int getHeightRadius(EntityClassification type)
	{
		return HugeCavernConfig.INSTANCE.heightRadius.get();
	}

	@Override
	public int getMaxCount(EntityClassification type)
	{
		if (!type.getAnimal() && !type.getPeacefulCreature())
		{
			return HugeCavernConfig.INSTANCE.maxCount.get();
		}

		return super.getMaxCount(type);
	}

	@Override
	public int getSafeDistance(EntityClassification type)
	{
		return HugeCavernConfig.INSTANCE.safeDistance.get();
	}
}