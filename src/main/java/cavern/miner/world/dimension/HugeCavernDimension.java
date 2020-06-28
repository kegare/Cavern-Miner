package cavern.miner.world.dimension;

import cavern.miner.config.dimension.HugeCavernConfig;
import cavern.miner.init.CaveChunkGeneratorTypes;
import cavern.miner.world.gen.CavernChunkGenerator;
import cavern.miner.world.gen.CavernGenSettings;
import cavern.miner.world.spawner.HugeCavernNaturalSpawner;
import cavern.miner.world.spawner.NaturalSpawner;
import cavern.miner.world.spawner.NaturalSpawnerType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HugeCavernDimension extends CavernDimension
{
	public HugeCavernDimension(World world, DimensionType type)
	{
		super(world, type);
	}

	@Override
	protected ChunkGeneratorType<CavernGenSettings, CavernChunkGenerator> getGeneratorType()
	{
		return CaveChunkGeneratorTypes.HUGE_CAVERN.get();
	}

	@Override
	public NaturalSpawnerType getSpawnerType()
	{
		return HugeCavernConfig.INSTANCE.spawnerType.get();
	}

	@Override
	protected NaturalSpawner createNaturalSpawner()
	{
		if (world instanceof ServerWorld)
		{
			return new HugeCavernNaturalSpawner((ServerWorld)world);
		}

		return null;
	}

	@Override
	public float getLightBrightness()
	{
		return HugeCavernConfig.INSTANCE.lightBrightness.get().floatValue();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public float getFogDensity(Entity entity)
	{
		return 0.005F;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public float getFogDepth(Entity entity)
	{
		return MathHelper.clamp(1.0F - Math.abs(0.5F - ((world.getDayTime() % 24000L) / 24000.0F)), 0.0F, 0.8F);
	}
}