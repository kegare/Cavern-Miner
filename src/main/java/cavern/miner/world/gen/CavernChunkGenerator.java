package cavern.miner.world.gen;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import cavern.miner.world.dimension.CavernDimension;
import cavern.miner.world.spawner.CaveMobSpawner;
import cavern.miner.world.spawner.WorldSpawnerType;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.server.ServerWorld;

public class CavernChunkGenerator<T extends GenerationSettings> extends ChunkGenerator<T>
{
	public CavernChunkGenerator(IWorld world, BiomeProvider biomeProvider, T settings)
	{
		super(world, biomeProvider, settings);
	}

	@Override
	public int getGroundHeight()
	{
		return 50;
	}

	@Override
	public void generateSurface(WorldGenRegion region, IChunk chunk)
	{
		ChunkPos pos = chunk.getPos();
		SharedSeedRandom rand = new SharedSeedRandom();

		rand.setBaseChunkSeed(pos.x, pos.z);

		makeBedrock(chunk, rand);
	}

	protected void makeBedrock(IChunk chunk, Random rand)
	{
		BlockPos.Mutable posCache = new BlockPos.Mutable();
		int xStart = chunk.getPos().getXStart();
		int zStart = chunk.getPos().getZStart();
		T settings = getSettings();
		int max = settings.getBedrockFloorHeight();
		int min = settings.getBedrockRoofHeight();

		for (BlockPos pos : BlockPos.getAllInBoxMutable(xStart, 0, zStart, xStart + 15, 0, zStart + 15))
		{
			if (min > 0)
			{
				for (int y = min; y >= min - 4; --y)
				{
					if (y >= min - rand.nextInt(5))
					{
						chunk.setBlockState(posCache.setPos(pos.getX(), y, pos.getZ()), Blocks.BEDROCK.getDefaultState(), false);
					}
				}
			}

			if (max < 256)
			{
				for (int y = max + 4; y >= max; --y)
				{
					if (y <= max + rand.nextInt(5))
					{
						chunk.setBlockState(posCache.setPos(pos.getX(), y, pos.getZ()), Blocks.BEDROCK.getDefaultState(), false);
					}
				}
			}
		}
	}

	@Override
	public void makeBase(IWorld world, IChunk chunk)
	{
		BlockPos.Mutable posCache = new BlockPos.Mutable();
		int xStart = chunk.getPos().getXStart();
		int zStart = chunk.getPos().getZStart();
		T settings = getSettings();
		int max = settings.getBedrockRoofHeight() - 1;
		int min = settings.getBedrockFloorHeight() + 1;

		for (BlockPos pos : BlockPos.getAllInBoxMutable(xStart, 0, zStart, xStart + 15, 0, zStart + 15))
		{
			for (int y = max; y >= min; --y)
			{
				chunk.setBlockState(posCache.setPos(pos.getX(), y, pos.getZ()), settings.getDefaultBlock(), false);
			}
		}
	}

	@Override
	public int func_222529_a(int x, int z, Type heightmapType)
	{
		return 0;
	}

	@Override
	public void spawnMobs(ServerWorld world, boolean spawnHostileMobs, boolean spawnPeacefulMobs)
	{
		if (!spawnHostileMobs)
		{
			return;
		}

		if (world.getWorldInfo().getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES)
		{
			return;
		}

		if (world.getDifficulty() == Difficulty.PEACEFUL || !world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING))
		{
			return;
		}

		if (world.getDimension() instanceof CavernDimension)
		{
			CavernDimension cavern = (CavernDimension)world.getDimension();

			if (cavern.getSpawnerType() == WorldSpawnerType.CAVERN)
			{
				cavern.getCaveMobSpawner().ifPresent(CaveMobSpawner::spawnMobs);
			}
		}
	}

	@Override
	public List<SpawnListEntry> getPossibleCreatures(EntityClassification creatureType, BlockPos pos)
	{
		if (world.getDimension() instanceof CavernDimension)
		{
			if (((CavernDimension)world.getDimension()).getSpawnerType() != WorldSpawnerType.VANILLA)
			{
				return Collections.emptyList();
			}
		}

		return super.getPossibleCreatures(creatureType, pos);
	}
}