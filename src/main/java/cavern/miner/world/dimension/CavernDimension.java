package cavern.miner.world.dimension;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.client.renderer.EmptyRenderer;
import cavern.miner.config.GeneralConfig;
import cavern.miner.config.dimension.CavernConfig;
import cavern.miner.init.CaveBiomes;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveDimensions;
import cavern.miner.init.CaveSounds;
import cavern.miner.storage.CavePortalList;
import cavern.miner.storage.Caver;
import cavern.miner.world.gen.CavernChunkGenerator;
import cavern.miner.world.gen.CavernGenSettings;
import cavern.miner.world.spawner.NaturalSpawner;
import cavern.miner.world.spawner.CavernNaturalSpawner;
import cavern.miner.world.spawner.NaturalSpawnerType;
import cavern.miner.world.vein.CavernVeinProvider;
import cavern.miner.world.vein.VeinProvider;
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
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IRenderHandler;

public class CavernDimension extends Dimension
{
	private static final Vec3d FOG_COLOR = new Vec3d(0.01D, 0.01D, 0.01D);

	protected final CavernGenSettings settings;
	protected final VeinProvider veinProvider;
	protected final NaturalSpawner naturalSpawner;

	protected final float[] lightBrightnessTable = new float[16];

	public CavernDimension(World world, DimensionType type)
	{
		super(world, type, 0);
		this.settings = createGenerationSettings();
		this.veinProvider = createVeinProvider();
		this.naturalSpawner = createNaturalSpawner();

		float brightness = getLightBrightness();

		for (int i = 0; i <= 15; ++i)
		{
			float f = i / 15.0F;
			float f1 = f / (4.0F - 3.0F * f);

			this.lightBrightnessTable[i] = MathHelper.lerp(brightness, f1, 1.0F);
		}
	}

	public Biome getBiome()
	{
		return CaveBiomes.CAVERN.get();
	}

	@Override
	public ChunkGenerator<? extends GenerationSettings> createChunkGenerator()
	{
		return new CavernChunkGenerator<>(world, BiomeProviderType.FIXED.create(BiomeProviderType.FIXED.createSettings(world.getWorldInfo()).setBiome(getBiome())), settings);
	}

	public CavernGenSettings createGenerationSettings()
	{
		return new CavernGenSettings();
	}

	public VeinProvider createVeinProvider()
	{
		return new CavernVeinProvider();
	}

	public VeinProvider getVeinProvider()
	{
		return veinProvider;
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

	public Optional<NaturalSpawner> getNaturalSpawner()
	{
		return Optional.ofNullable(naturalSpawner);
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
	public boolean isSkyColored()
	{
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public IRenderHandler getSkyRenderer()
	{
		return EmptyRenderer.INSTANCE;
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
		if (settings.getGroundHeight() > 0)
		{
			return ((float)entity.getPosY() / settings.getGroundHeight()) * 0.005F;
		}

		return 0.0F;
	}

	@OnlyIn(Dist.CLIENT)
	public float getFogDepth(Entity entity)
	{
		if (settings.getGroundHeight() > 0)
		{
			return MathHelper.clamp(((float)entity.getPosY() / settings.getGroundHeight()) * 0.65F, 0.0F, 1.0F);
		}

		return 0.0F;
	}
}