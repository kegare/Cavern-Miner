package cavern.miner.world;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.client.render.EmptyRenderer;
import cavern.miner.config.GeneralConfig;
import cavern.miner.init.CaveBiomes;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveDimensions;
import cavern.miner.storage.CavePortalList;
import cavern.miner.storage.Caver;
import cavern.miner.world.gen.CavernChunkGenerator;
import cavern.miner.world.gen.CavernGenSettings;
import cavern.miner.world.spawner.CaveMobSpawner;
import cavern.miner.world.vein.CavernVeinProvider;
import cavern.miner.world.vein.VeinProvider;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.server.ServerWorld;

public class CavernDimension extends Dimension
{
	public static final VeinProvider VEINS = new CavernVeinProvider();

	protected final CaveMobSpawner caveMobSpawner;

	public CavernDimension(World world, DimensionType type)
	{
		super(world, type, 0);
		this.caveMobSpawner = createCaveMobSpawner();
		this.setSkyRenderer(EmptyRenderer.INSTANCE);
		this.setCloudRenderer(EmptyRenderer.INSTANCE);
		this.setWeatherRenderer(EmptyRenderer.INSTANCE);
	}

	@Override
	public ChunkGenerator<? extends GenerationSettings> createChunkGenerator()
	{
		SingleBiomeProviderSettings biomeSettings = BiomeProviderType.FIXED.createSettings(world.getWorldInfo()).setBiome(CaveBiomes.CAVERN.orElse(Biomes.PLAINS));

		return new CavernChunkGenerator<>(world, BiomeProviderType.FIXED.create(biomeSettings), new CavernGenSettings());
	}

	@Nullable
	public CaveMobSpawner createCaveMobSpawner()
	{
		if (world instanceof ServerWorld)
		{
			return new CaveMobSpawner((ServerWorld)world);
		}

		return null;
	}

	public Optional<CaveMobSpawner> getCaveMobSpawner()
	{
		return Optional.ofNullable(caveMobSpawner);
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
		if (world.isRemote)
		{
			return SleepResult.DENY;
		}

		if (player.isSleeping() || !player.isAlive())
		{
			player.sendStatusMessage(PlayerEntity.SleepResult.OTHER_PROBLEM.getMessage(), true);

			return SleepResult.DENY;
		}

		if (!player.isCreative())
		{
			AxisAlignedBB box = new AxisAlignedBB(pos);
			List<MonsterEntity> list = world.getEntitiesWithinAABB(MonsterEntity.class, box.grow(8.0D, 6.0D, 8.0D), o -> o.isPreventingPlayerRest(player));

			if (!list.isEmpty())
			{
				for (MonsterEntity monster : list)
				{
					if (monster.canEntityBeSeen(player))
					{
						monster.setAttackTarget(player);
					}
				}

				player.sendStatusMessage(PlayerEntity.SleepResult.NOT_SAFE.getMessage(), true);

				return SleepResult.DENY;
			}
		}

		int sleepWait = GeneralConfig.INSTANCE.sleepWait.get();

		if (sleepWait > 0)
		{
			Caver caver = player.getCapability(CaveCapabilities.CAVER).orElse(null);

			if (caver != null)
			{
				long sleepTime = caver.getSleepTime();
				long worldTime = world.getGameTime();

				if (sleepTime <= 0 || worldTime - sleepTime > sleepWait * 20L)
				{
					caver.setSleepTime(worldTime + 100L);
				}
				else
				{
					player.sendStatusMessage(new TranslationTextComponent("cavern.caver.no_sleep"), true);

					return SleepResult.DENY;
				}
			}
		}

		player.startSleeping(pos);

		if (world instanceof ServerWorld)
		{
			((ServerWorld)world).updateAllPlayersSleepingFlag();
		}

		return SleepResult.DENY;
	}

	@Override
	public int getSeaLevel()
	{
		return 10;
	}
}