package cavern.miner.world;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.client.render.EmptyRenderer;
import cavern.miner.init.CaveBiomes;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveDimensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.OverworldGenSettings;

public class CavernDimension extends Dimension
{
	public static final VeinProvider VEINS = new CavernVeinProvider();

	public CavernDimension(World world, DimensionType type)
	{
		super(world, type, 0);
		this.setSkyRenderer(EmptyRenderer.INSTANCE);
		this.setCloudRenderer(EmptyRenderer.INSTANCE);
		this.setWeatherRenderer(EmptyRenderer.INSTANCE);
	}

	@Override
	public ChunkGenerator<? extends GenerationSettings> createChunkGenerator()
	{
		BiomeProviderType<SingleBiomeProviderSettings, SingleBiomeProvider> biomeProvider = BiomeProviderType.FIXED;
		OverworldGenSettings genSettings = new CavernGenSettings();
		SingleBiomeProviderSettings biomeSettings = biomeProvider.createSettings(world.getWorldInfo()).setBiome(CaveBiomes.CAVERN.get());

		return new CavernChunkGenerator<>(world, biomeProvider.create(biomeSettings), genSettings);
	}

	@Override
	public BlockPos getSpawnPoint()
	{
		CavePortalList portalList = world.getCapability(CaveCapabilities.CAVE_PORTAL_LIST).orElse(null);

		if (portalList == null || portalList.isPortalEmpty())
		{
			return BlockPos.ZERO;
		}

		CavernPortalBlock portal = CaveDimensions.getPortalBlock(getType());

		if (portal != null)
		{
			BlockPos pos = portalList.getPortalPositions(portal).stream().findAny().orElse(null);

			if (pos != null)
			{
				return pos;
			}
		}

		return portalList.getPortalPositions().stream().findAny().orElse(BlockPos.ZERO);
	}

	@Override
	public BlockPos findSpawn(ChunkPos chunkPos, boolean checkValid)
	{
		return getSpawnPoint();
	}

	@Override
	public BlockPos findSpawn(int posX, int posZ, boolean checkValid)
	{
		return getSpawnPoint();
	}

	@Override
	public DimensionType getRespawnDimension(ServerPlayerEntity player)
	{
		boolean noPortal = world.getCapability(CaveCapabilities.CAVE_PORTAL_LIST).map(CavePortalList::isPortalEmpty).orElse(true);

		if (noPortal && player.getBedLocation(getType()) == null)
		{
			return DimensionType.OVERWORLD;
		}

		return getType();
	}

	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks)
	{
		return 0;
	}

	@Override
	public void updateWeather(Runnable defaultLogic) {}

	@Override
	public boolean isSurfaceWorld()
	{
		return false;
	}

	@Override
	public Vec3d getFogColor(float celestialAngle, float partialTicks)
	{
		return Vec3d.ZERO;
	}

	@Override
	public boolean canRespawnHere()
	{
		return false;
	}

	@Override
	public boolean doesXZShowFog(int x, int z)
	{
		return false;
	}

	@Override
	public SleepResult canSleepAt(PlayerEntity player, BlockPos pos)
	{
		return SleepResult.ALLOW;
	}

	@Override
	public int getSeaLevel()
	{
		return 10;
	}
}