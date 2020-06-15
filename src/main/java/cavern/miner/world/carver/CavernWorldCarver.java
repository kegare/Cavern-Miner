package cavern.miner.world.carver;

import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.carver.CaveWorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;

public class CavernWorldCarver extends CaveWorldCarver
{
	public CavernWorldCarver(Function<Dynamic<?>, ? extends ProbabilityConfig> config, int maxHeight)
	{
		super(config, maxHeight);
	}

	@Override
	protected int func_222724_a()
	{
		return 35;
	}

	@Override
	protected double func_222725_b()
	{
		return 1.1D;
	}

	@Override
	protected int generateCaveStartY(Random rand)
	{
		return rand.nextInt(maxHeight - 50) + 5;
	}

	@Override
	protected boolean func_225556_a_(IChunk chunk, Function<BlockPos, Biome> biomes, BitSet carvingMask, Random rand, BlockPos.Mutable posHere, BlockPos.Mutable posAbove, BlockPos.Mutable posBelow, int seaLevel, int chunkX, int chunkZ, int globalX, int globalZ, int x, int y, int z, AtomicBoolean foundSurface)
	{
		int i = x | z << 4 | y << 8;

		if (carvingMask.get(i))
		{
			return false;
		}
		else
		{
			carvingMask.set(i);
			posHere.setPos(globalX, y, globalZ);

			BlockState stateHere = chunk.getBlockState(posHere);
			BlockState stateAbove = chunk.getBlockState(posAbove.setPos(posHere).move(Direction.UP));

			if (!canCarveBlock(stateHere, stateAbove))
			{
				return false;
			}
			else
			{
				if (y < 11)
				{
					chunk.setBlockState(posHere, LAVA.getBlockState(), false);
				}
				else
				{
					chunk.setBlockState(posHere, CAVE_AIR, false);
				}

				return true;
			}
		}
	}

}