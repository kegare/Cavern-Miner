package cavern.miner.world.gen;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class MapGenCavelandRavine extends MapGenCavernRavine
{
	@Override
	protected void recursiveGenerate(World world, int chunkX, int chunkZ, int x, int z, ChunkPrimer primer)
	{
		if (rand.nextInt(25) == 0)
		{
			int max = world.getActualHeight() - 1;
			int ground = world.provider.getAverageGroundLevel();
			double blockX = (chunkX << 4) + rand.nextInt(16);
			double blockY = rand.nextInt(rand.nextInt(max / 2) + ground + 10);
			double blockZ = (chunkZ << 4) + rand.nextInt(16);
			float leftRightRadian = rand.nextFloat() * (float)Math.PI * 2.0F;
			float upDownRadian = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
			float scale = (rand.nextFloat() * 3.0F + rand.nextFloat()) * 2.0F;

			if (blockY > max - 40)
			{
				blockY = ground + rand.nextInt(10);
			}

			addTunnel(rand.nextLong(), x, z, primer, blockX, blockY, blockZ, scale, leftRightRadian, upDownRadian, 0, 0, 2.0D);
		}
	}

	@Override
	protected void digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop)
	{
		int ground = world.provider.getAverageGroundLevel();

		if (y < ground - 3)
		{
			data.setBlockState(x, y, z, BLK_STONE);
		}
		else if (y == ground - 3)
		{
			data.setBlockState(x, y, z, BLK_GRAVEL);
		}
		else if (y < ground)
		{
			IBlockState state = FLOWING_WATER;

			if (biomesForGeneration != null)
			{
				Biome biome = biomesForGeneration[x * 16 + z];

				if (biome != null && BiomeDictionary.hasType(biome, Type.COLD) && rand.nextInt(3) == 0)
				{
					state = BLK_ICE;
				}
			}

			data.setBlockState(x, y, z, state);
		}
		else
		{
			data.setBlockState(x, y, z, AIR);
		}
	}
}