package cavern.miner.world.dimension;

import cavern.miner.config.HugeCavernConfig;
import cavern.miner.config.client.ClientConfig;
import cavern.miner.init.CaveBiomes;
import cavern.miner.world.spawner.CaveMobSpawner;
import cavern.miner.world.spawner.HugeCavernMobSpawner;
import cavern.miner.world.spawner.WorldSpawnerType;
import cavern.miner.world.vein.HugeCavernVeinProvider;
import cavern.miner.world.vein.VeinProvider;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HugeCavernDimension extends CavernDimension
{
	private static final Vec3d FOG_COLOR = new Vec3d(0.1D, 0.1D, 0.1D);

	public HugeCavernDimension(World world, DimensionType type)
	{
		super(world, type);
	}

	@Override
	public Biome getBiome()
	{
		return CaveBiomes.HUGE_CAVERN.orElse(Biomes.PLAINS);
	}

	@Override
	public VeinProvider createVeinProvider()
	{
		return new HugeCavernVeinProvider();
	}

	@Override
	public WorldSpawnerType getSpawnerType()
	{
		return HugeCavernConfig.INSTANCE.spawnerType.get();
	}

	@Override
	public CaveMobSpawner createCaveMobSpawner()
	{
		if (world instanceof ServerWorld)
		{
			return new HugeCavernMobSpawner((ServerWorld)world);
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
	public boolean doesXZShowFog(int x, int z)
	{
		return ClientConfig.INSTANCE.caveFog.get();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public Vec3d getFogColor(float celestialAngle, float partialTicks)
	{
		return ClientConfig.INSTANCE.caveFog.get() ? FOG_COLOR : Vec3d.ZERO;
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
		return 0.8F;
	}
}