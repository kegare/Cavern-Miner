package cavern.miner.world.gen;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import cavern.miner.world.dimension.CavernDimension;
import cavern.miner.world.spawner.NaturalSpawner;
import cavern.miner.world.spawner.NaturalSpawnerType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.Direction;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

public class CavernChunkGenerator extends ChunkGenerator<CavernGenSettings>
{
	private static final BlockState BEDROCK = Blocks.BEDROCK.getDefaultState();

	private NaturalSpawner naturalSpawner;

	public CavernChunkGenerator(IWorld world, BiomeProvider biomeProvider, CavernGenSettings settings)
	{
		super(world, biomeProvider, settings);
	}

	@Override
	public int getGroundHeight()
	{
		return getSettings().getGroundHeight();
	}

	@Override
	public void makeBase(IWorld world, IChunk chunk)
	{
		BlockPos.Mutable posCache = new BlockPos.Mutable();
		ChunkPos chunkPos = chunk.getPos();
		int xStart = chunkPos.getXStart();
		int zStart = chunkPos.getZStart();
		CavernGenSettings settings = getSettings();
		int floor = settings.getBedrockFloorHeight() + 1;
		int roof = settings.getBedrockRoofHeight() - 1;
		int ground = getGroundHeight();

		for (BlockPos pos : BlockPos.getAllInBoxMutable(xStart, 0, zStart, xStart + 15, 0, zStart + 15))
		{
			for (int y = roof; y >= floor; --y)
			{
				posCache.setPos(pos.getX(), y, pos.getZ());

				if (ground > 0 && y >= ground)
				{
					chunk.setBlockState(posCache, settings.getGroundUnderBlock(), false);
				}
				else
				{
					chunk.setBlockState(posCache, settings.getDefaultBlock(), false);
				}
			}
		}
	}

	@Override
	public void generateSurface(WorldGenRegion region, IChunk chunk)
	{
		ChunkPos pos = chunk.getPos();
		SharedSeedRandom rand = new SharedSeedRandom();

		rand.setBaseChunkSeed(pos.x, pos.z);

		makeBorders(chunk, rand);
	}

	protected void makeBorders(IChunk chunk, Random rand)
	{
		BlockPos.Mutable posCache = new BlockPos.Mutable();
		int xStart = chunk.getPos().getXStart();
		int zStart = chunk.getPos().getZStart();
		CavernGenSettings settings = getSettings();
		int floor = settings.getBedrockFloorHeight();
		int roof = settings.getBedrockRoofHeight();
		boolean flat = settings.isFlatBedrock();
		int ground = getGroundHeight();

		for (BlockPos pos : BlockPos.getAllInBoxMutable(xStart, 0, zStart, xStart + 15, 0, zStart + 15))
		{
			if (floor < 256)
			{
				if (flat)
				{
					chunk.setBlockState(posCache.setPos(pos.getX(), floor, pos.getZ()), BEDROCK, false);
				}
				else
				{
					for (int y = floor + 4; y >= floor; --y)
					{
						if (y <= floor + rand.nextInt(5))
						{
							chunk.setBlockState(posCache.setPos(pos.getX(), y, pos.getZ()), BEDROCK, false);
						}
					}
				}
			}

			if (roof > 0)
			{
				if (flat)
				{
					chunk.setBlockState(posCache.setPos(pos.getX(), roof, pos.getZ()), BEDROCK, false);
				}
				else
				{
					for (int y = roof; y >= roof - 4; --y)
					{
						if (y >= roof - rand.nextInt(5))
						{
							chunk.setBlockState(posCache.setPos(pos.getX(), y, pos.getZ()), BEDROCK, false);
						}
					}
				}
			}

			if (ground > 0)
			{
				for (int y = ground; y >= ground - 4; --y)
				{
					if (y >= ground - rand.nextInt(5))
					{
						chunk.setBlockState(posCache.setPos(pos.getX(), y, pos.getZ()), settings.getGroundUnderBlock(), false);
					}
				}
			}
		}
	}

	@Override
	public void generateCarvers(BiomeManager manager, IChunk chunk, GenerationStage.Carving carving)
	{
		super.generateCarvers(manager, chunk, carving);

		int ground = getGroundHeight();

		if (ground <= 0)
		{
			return;
		}

		BlockPos.Mutable posHere = new BlockPos.Mutable();
		BlockPos.Mutable posAbove = new BlockPos.Mutable();
		int xStart = chunk.getPos().getXStart();
		int zStart = chunk.getPos().getZStart();
		CavernGenSettings settings = getSettings();
		int roof = settings.getBedrockRoofHeight() - 1;

		for (BlockPos pos : BlockPos.getAllInBoxMutable(xStart, 0, zStart, xStart + 15, 0, zStart + 15))
		{
			for (int y = roof; y > ground; --y)
			{
				BlockState stateHere = chunk.getBlockState(posHere.setPos(pos.getX(), y, pos.getZ()));
				BlockState stateAbove = chunk.getBlockState(posAbove.setPos(posHere).move(Direction.UP));

				if (stateHere.getBlock() == settings.getGroundUnderBlock().getBlock() && stateAbove.isAir(chunk, posAbove))
				{
					chunk.setBlockState(posHere, settings.getGroundTopBlock(), false);
				}
			}
		}
	}

	@Override
	public void decorate(WorldGenRegion region)
	{
		int centerX = region.getMainChunkX();
		int centerZ = region.getMainChunkZ();
		int xStart = centerX << 4;
		int zStart = centerZ << 4;
		BlockPos pos = new BlockPos(xStart, 0, zStart);
		Biome biome = getBiome(region.getBiomeManager(), pos.add(8, 8, 8));
		SharedSeedRandom rand = new SharedSeedRandom();
		long seed = rand.setDecorationSeed(region.getSeed(), xStart, zStart);

		decorate(region, centerX, centerZ, pos, biome, rand, seed,
			GenerationStage.Decoration.RAW_GENERATION, GenerationStage.Decoration.LOCAL_MODIFICATIONS,
			GenerationStage.Decoration.UNDERGROUND_STRUCTURES, GenerationStage.Decoration.UNDERGROUND_ORES, GenerationStage.Decoration.UNDERGROUND_DECORATION);

		if (getGroundHeight() > 0)
		{
			decorate(region, centerX, centerZ, pos, biome, rand, seed, GenerationStage.Decoration.VEGETAL_DECORATION);
		}
	}

	public void decorate(WorldGenRegion region, int centerX, int centerZ, BlockPos pos, Biome biome, SharedSeedRandom rand, long seed, GenerationStage.Decoration... stages)
	{
		for (GenerationStage.Decoration stage : stages)
		{
			try
			{
				biome.decorate(stage, this, region, seed, rand, pos);
			}
			catch (Exception e)
			{
				CrashReport report = CrashReport.makeCrashReport(e, "Biome decoration");
				report.makeCategory("Generation").addDetail("CenterX", centerX).addDetail("CenterZ", centerZ).addDetail("Step", stage).addDetail("Seed", seed).addDetail("Biome", ForgeRegistries.BIOMES.getKey(biome));

				throw new ReportedException(report);
			}
		}
	}

	@Override
	public int getHeight(int x, int z, Heightmap.Type heightmapType)
	{
		return 0;
	}

	@Override
	public void spawnMobs(ServerWorld world, boolean spawnHostileMobs, boolean spawnPeacefulMobs)
	{
		if (world.getWorldInfo().getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES)
		{
			return;
		}

		if (!world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING))
		{
			return;
		}

		if (world.getDimension() instanceof CavernDimension)
		{
			CavernDimension cavern = (CavernDimension)world.getDimension();

			if (cavern.getSpawnerType() == NaturalSpawnerType.CAVERN)
			{
				if (naturalSpawner == null)
				{
					naturalSpawner = cavern.createNaturalSpawner();
				}

				if (naturalSpawner != null)
				{
					naturalSpawner.spawnMobs();
				}
			}
		}
	}

	@Override
	public List<SpawnListEntry> getPossibleCreatures(EntityClassification creatureType, BlockPos pos)
	{
		if (world.getDimension() instanceof CavernDimension)
		{
			if (((CavernDimension)world.getDimension()).getSpawnerType() != NaturalSpawnerType.VANILLA)
			{
				return Collections.emptyList();
			}
		}

		return super.getPossibleCreatures(creatureType, pos);
	}
}