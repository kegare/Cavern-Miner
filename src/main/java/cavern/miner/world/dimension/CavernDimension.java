package cavern.miner.world.dimension;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.client.renderer.EmptyRenderer;
import cavern.miner.config.GeneralConfig;
import cavern.miner.config.dimension.CavernConfig;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveChunkGeneratorTypes;
import cavern.miner.init.CaveDimensions;
import cavern.miner.init.CaveSounds;
import cavern.miner.storage.CavePortalList;
import cavern.miner.storage.Caver;
import cavern.miner.world.gen.CavernChunkGenerator;
import cavern.miner.world.gen.CavernGenSettings;
import cavern.miner.world.spawner.CavernNaturalSpawner;
import cavern.miner.world.spawner.NaturalSpawner;
import cavern.miner.world.spawner.NaturalSpawnerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IRenderHandler;

public class CavernDimension extends Dimension
{
	private static final Vec3d FOG_COLOR = new Vec3d(0.01D, 0.01D, 0.01D);

	private final float[] lightBrightnessTable;

	public CavernDimension(World world, DimensionType type)
	{
		super(world, type, 0);
		this.lightBrightnessTable = createLightBrightnessTable();
	}

	protected ChunkGeneratorType<CavernGenSettings, CavernChunkGenerator> getGeneratorType()
	{
		return CaveChunkGeneratorTypes.CAVERN.get();
	}

	@Override
	public ChunkGenerator<? extends GenerationSettings> createChunkGenerator()
	{
		ChunkGeneratorType<CavernGenSettings, CavernChunkGenerator> type = getGeneratorType();
		CavernGenSettings settings = type.createSettings();

		return type.create(world, BiomeProviderType.FIXED.create(BiomeProviderType.FIXED.createSettings(world.getWorldInfo()).setBiome(settings.getDefaultBiome())), settings);
	}

	public NaturalSpawnerType getSpawnerType()
	{
		return CavernConfig.INSTANCE.spawnerType.get();
	}

	@Nullable
	public NaturalSpawner createNaturalSpawner()
	{
		if (world instanceof ServerWorld)
		{
			return new CavernNaturalSpawner((ServerWorld)world);
		}

		return null;
	}

	protected float[] createLightBrightnessTable()
	{
		float[] table = new float[16];
		float brightness = getLightBrightness();

		for (int i = 0; i < table.length; ++i)
		{
			float f = i / 15.0F;
			float f1 = f / (4.0F - 3.0F * f);

			table[i] = MathHelper.lerp(brightness, f1, 1.0F);
		}

		return table;
	}

	public float getLightBrightness()
	{
		return CavernConfig.INSTANCE.lightBrightness.get().floatValue();
	}

	@Override
	public float getLightBrightness(int level)
	{
		return lightBrightnessTable[level];
	}

	@Override
	public BlockPos getSpawnPoint()
	{
		CavePortalList portalList = world.getCapability(CaveCapabilities.CAVE_PORTAL_LIST).orElse(null);

		if (portalList == null || portalList.isPortalEmpty())
		{
			return BlockPos.ZERO.up(50);
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

		return portalList.getPortalPositions().stream().findAny().orElse(BlockPos.ZERO.up(50));
	}

	@Override
	public BlockPos findSpawn(ChunkPos chunkPos, boolean checkValid)
	{
		CavePortalList portalList = world.getCapability(CaveCapabilities.CAVE_PORTAL_LIST).orElse(null);

		if (portalList == null || portalList.isPortalEmpty())
		{
			return null;
		}

		final BlockPos originPos = chunkPos.asBlockPos().add(8, 50, 8);
		final Comparator<BlockPos> comparator = Comparator.comparingDouble(o -> o.distanceSq(originPos));

		CavernPortalBlock portal = CaveDimensions.getPortalBlock(getType());

		if (portal != null)
		{
			BlockPos pos = portalList.getPortalPositions(portal).stream().min(comparator).orElse(null);

			if (pos != null)
			{
				return pos;
			}
		}

		return portalList.getPortalPositions().stream().min(comparator).orElse(null);
	}

	@Override
	public BlockPos findSpawn(int posX, int posZ, boolean checkValid)
	{
		CavePortalList portalList = world.getCapability(CaveCapabilities.CAVE_PORTAL_LIST).orElse(null);

		if (portalList == null || portalList.isPortalEmpty())
		{
			return null;
		}

		final BlockPos originPos = new BlockPos(posX, 50, posZ);
		final Comparator<BlockPos> comparator = Comparator.comparingDouble(o -> o.distanceSq(originPos));

		CavernPortalBlock portal = CaveDimensions.getPortalBlock(getType());

		if (portal != null)
		{
			BlockPos pos = portalList.getPortalPositions(portal).stream().min(comparator).orElse(null);

			if (pos != null)
			{
				return pos;
			}
		}

		return portalList.getPortalPositions().stream().min(comparator).orElse(null);
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
		return 0.5F;
	}

	@Override
	public void updateWeather(Runnable defaultLogic) {}

	@Override
	public boolean isSurfaceWorld()
	{
		return false;
	}

	@Override
	public boolean canRespawnHere()
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
	public boolean canDoLightning(Chunk chunk)
	{
		return false;
	}

	@Override
	public boolean canDoRainSnowIce(Chunk chunk)
	{
		return false;
	}

	@Override
	public int getSeaLevel()
	{
		return 256;
	}

	public SoundEvent getMusic()
	{
		return CaveSounds.MUSIC_CAVERN.get();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public IRenderHandler getCloudRenderer()
	{
		return EmptyRenderer.INSTANCE;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public IRenderHandler getWeatherRenderer()
	{
		return EmptyRenderer.INSTANCE;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean doesXZShowFog(int x, int z)
	{
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public Vec3d getFogColor(float celestialAngle, float partialTicks)
	{
		return FOG_COLOR;
	}

	@OnlyIn(Dist.CLIENT)
	public float getFogDensity(Entity entity)
	{
		return (float)entity.getPosY() / (getActualHeight() * 0.5F) * 0.005F;
	}

	@OnlyIn(Dist.CLIENT)
	public float getFogDepth(Entity entity)
	{
		return MathHelper.clamp((float)entity.getPosY() / (getActualHeight() * 0.5F) * 0.65F, 0.0F, 0.8F);
	}
}