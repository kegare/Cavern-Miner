package cavern.miner.world;

import cavern.miner.block.BlockCavernPortal;
import cavern.miner.client.CaveMusics;
import cavern.miner.client.renderer.EmptyRenderer;
import cavern.miner.config.CavernConfig;
import cavern.miner.data.WorldData;
import cavern.miner.entity.CaveEntityRegistry;
import cavern.miner.world.CaveEntitySpawner.IWorldEntitySpawner;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderCavern extends WorldProvider implements CustomSeedProvider, IWorldEntitySpawner
{
	private CustomSeed seedData;

	private final CaveEntitySpawner entitySpawner = new CaveEntitySpawner(this);

	protected boolean spawnHostileMobs = true;
	protected boolean spawnPeacefulMobs = true;

	@Override
	protected void init()
	{
		hasSkyLight = false;
		seedData = world instanceof WorldServer ? new CustomSeed(world.getWorldInfo().getDimensionData(getDimension())) : new CustomSeed();
		biomeProvider = new BiomeProvider(world.getWorldInfo());
	}

	@Override
	public IChunkGenerator createChunkGenerator()
	{
		return new ChunkGeneratorCavern(world);
	}

	@Override
	public DimensionType getDimensionType()
	{
		return CaveDimensions.CAVERN;
	}

	@Override
	public CustomSeed getSeedData()
	{
		return seedData;
	}

	@Override
	public long getSeed()
	{
		if (seedData != null)
		{
			if (world instanceof WorldServer)
			{
				return seedData.getSeed();
			}

			return seedData.getSeedValue(world.getWorldInfo().getSeed());
		}

		return super.getSeed();
	}

	@Override
	public int getActualHeight()
	{
		return CavernConfig.halfHeight ? 128 : 256;
	}

	public int getMonsterSpawn()
	{
		return CavernConfig.monsterSpawn;
	}

	public double getBrightness()
	{
		return CavernConfig.caveBrightness;
	}

	@Override
	protected void generateLightBrightnessTable()
	{
		float f = (float)getBrightness();

		for (int i = 0; i <= 15; ++i)
		{
			float f1 = 1.0F - i / 15.0F;

			lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F) * (1.0F - f) + f;
		}
	}

	@Override
	public boolean isSurfaceWorld()
	{
		return false;
	}

	@Override
	public boolean canCoordinateBeSpawn(int x, int z)
	{
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public MusicType getMusicType()
	{
		return CaveMusics.CAVES;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float[] calcSunriseSunsetColors(float angle, float ticks)
	{
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Vec3d getFogColor(float angle, float ticks)
	{
		return new Vec3d(0.01D, 0.01D, 0.01D);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isSkyColored()
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Vec3d getSkyColor(Entity cameraEntity, float partialTicks)
	{
		return new Vec3d(0.01D, 0.01D, 0.01D);
	}

	@Override
	public int getAverageGroundLevel()
	{
		return 10;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IRenderHandler getSkyRenderer()
	{
		if (super.getSkyRenderer() == null)
		{
			setSkyRenderer(EmptyRenderer.INSTANCE);
		}

		return super.getSkyRenderer();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IRenderHandler getCloudRenderer()
	{
		if (super.getCloudRenderer() == null)
		{
			setCloudRenderer(EmptyRenderer.INSTANCE);
		}

		return super.getCloudRenderer();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IRenderHandler getWeatherRenderer()
	{
		if (super.getWeatherRenderer() == null)
		{
			setWeatherRenderer(EmptyRenderer.INSTANCE);
		}

		return super.getWeatherRenderer();
	}

	@Override
	public boolean shouldMapSpin(String entity, double posX, double posY, double posZ)
	{
		return posY < 0 || posY >= getActualHeight();
	}

	@Override
	public int getRespawnDimension(EntityPlayerMP player)
	{
		if (WorldData.get(world).isPortalEmpty() || player.getBedLocation(getDimension()) == null)
		{
			return 0;
		}

		return super.getRespawnDimension(player);
	}

	@Override
	public BlockPos getSpawnPoint()
	{
		WorldData data = WorldData.get(world);

		if (data.isPortalEmpty())
		{
			return BlockPos.ORIGIN;
		}

		BlockCavernPortal portal = CaveDimensions.getPortalBlock(getDimensionType());

		if (portal != null)
		{
			BlockPos pos = data.getPortalPositions(portal).stream().findAny().orElse(null);

			if (pos != null)
			{
				return pos;
			}
		}

		return data.getPortalPositions().stream().findAny().orElse(BlockPos.ORIGIN);
	}

	@Override
	public BlockPos getRandomizedSpawnPoint()
	{
		return getSpawnPoint();
	}

	@Override
	public boolean isDaytime()
	{
		return false;
	}

	@Override
	public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful)
	{
		super.setAllowedSpawnTypes(allowHostile, allowPeaceful);

		spawnHostileMobs = allowHostile;
		spawnPeacefulMobs = allowPeaceful;
	}

	@Override
	public double getHorizon()
	{
		return getActualHeight();
	}

	@Override
	public WorldSleepResult canSleepAt(EntityPlayer player, BlockPos pos)
	{
		return WorldSleepResult.ALLOW;
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
	public boolean canDropChunk(int x, int z)
	{
		return CavernConfig.keepPortalChunk && !WorldData.get(world).hasPortal(x, z);
	}

	@Override
	public void onWorldSave()
	{
		if (seedData != null)
		{
			NBTTagCompound nbt = world.getWorldInfo().getDimensionData(getDimension());

			world.getWorldInfo().setDimensionData(getDimension(), seedData.getCompound(nbt));
		}
	}

	@Override
	public void onWorldUpdateEntities()
	{
		if (world instanceof WorldServer)
		{
			WorldServer worldServer = (WorldServer)world;

			if (worldServer.getGameRules().getBoolean("doMobSpawning") && worldServer.getWorldInfo().getTerrainType() != WorldType.DEBUG_ALL_BLOCK_STATES)
			{
				entitySpawner.findChunksForSpawning(worldServer, spawnHostileMobs, spawnPeacefulMobs, worldServer.getWorldInfo().getWorldTotalTime() % 400L == 0L);
			}
		}
	}

	@Override
	public Integer getMaxNumberOfCreature(WorldServer world, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate, EnumCreatureType type)
	{
		if (!type.getPeacefulCreature())
		{
			return getMonsterSpawn();
		}

		return null;
	}

	@Override
	public EntityLiving createSpawnCreature(WorldServer world, EnumCreatureType type, BlockPos pos, Biome.SpawnListEntry entry)
	{
		if (type != EnumCreatureType.MONSTER)
		{
			return null;
		}

		if (world.rand.nextInt(30) == 0)
		{
			Biome.SpawnListEntry spawnEntry = WeightedRandom.getRandomItem(world.rand, CaveEntityRegistry.SPAWNS);

			try
			{
				return spawnEntry.newInstance(world);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return null;
	}
}