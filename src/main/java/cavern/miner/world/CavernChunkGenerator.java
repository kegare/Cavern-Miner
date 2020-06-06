package cavern.miner.world;

import java.util.Random;

import net.minecraft.block.Blocks;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.WorldGenRegion;

public class CavernChunkGenerator<T extends GenerationSettings> extends ChunkGenerator<T>
{
	protected final VeinGenerator veinGenerator;

	public CavernChunkGenerator(IWorld world, BiomeProvider biomeProvider, T settings)
	{
		super(world, biomeProvider, settings);
		this.veinGenerator = new VeinGenerator(CavernDimension.VEINS);
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

		for(BlockPos pos : BlockPos.getAllInBoxMutable(xStart, 0, zStart, xStart + 15, 0, zStart + 15))
		{
			if (min > 0)
			{
				for(int y = min; y >= min - 4; --y)
				{
					if (y >= min - rand.nextInt(5))
					{
						chunk.setBlockState(posCache.setPos(pos.getX(), y, pos.getZ()), Blocks.BEDROCK.getDefaultState(), false);
					}
				}
			}

			if (max < 256)
			{
				for(int y = max + 4; y >= max; --y)
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
		int max = settings.getBedrockFloorHeight() - 1;
		int min = settings.getBedrockRoofHeight() + 1;

		for (BlockPos pos : BlockPos.getAllInBoxMutable(xStart, 0, zStart, xStart + 15, 0, zStart + 15))
		{
			for (int y = max; y >= min; --y)
			{
				chunk.setBlockState(posCache.setPos(pos.getX(), y, pos.getZ()), settings.getDefaultBlock(), false);
			}
		}
	}

	@Override
	public void func_225550_a_(BiomeManager biomeManager, IChunk chunk, GenerationStage.Carving carving)
	{
		super.func_225550_a_(biomeManager, chunk, carving);

		if (carving == GenerationStage.Carving.AIR)
		{
			veinGenerator.makeVeins(world, chunk);
		}
	}

	@Override
	public int func_222529_a(int par1, int par2, Type heightmapType)
	{
		return 0;
	}
}