package cavern.miner.world.gen;

import java.util.Random;
import java.util.Set;

import cavern.miner.util.BlockStateHelper;
import cavern.miner.vein.Vein;
import cavern.miner.world.VeinProvider;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;

public class VeinGenerator
{
	protected final VeinProvider provider;

	protected final Random rand = new Random();

	public VeinGenerator(VeinProvider provider)
	{
		this.provider = provider;
	}

	public void makeVeins(IWorld world, IChunk chunk)
	{
		provider.getVeins(world, chunk, rand).stream().filter(o -> o != null && o.getCount() > 0 && o.getSize() > 0).forEach(o -> make(o, world, chunk));
	}

	protected void make(Vein vein, IWorld world, IChunk chunk)
	{
		int max = world.getMaxHeight() - 1;
		ChunkPos chunkPos = chunk.getPos();
		int xStart = chunkPos.getXStart();
		int zStart = chunkPos.getZStart();
		Set<BlockState> targetBlocks = vein.getTargetBlocks();
		BlockPos.Mutable pos = new BlockPos.Mutable();

		for (int veinCount = 0; veinCount < vein.getCount(); ++veinCount)
		{
			int yChance = rand.nextInt(3) + 3;
			int originX = rand.nextInt(16);
			int originY = MathHelper.nextInt(rand, vein.getMinHeight(), vein.getMaxHeight());
			int originZ = rand.nextInt(16);
			int x = originX;
			int y = originY;
			int z = originZ;
			Direction.Axis prev = null;

			for (int blockCount = 0; blockCount < vein.getSize(); ++blockCount)
			{
				int checkCount = 0;

				while (blockCount > 0)
				{
					Direction.Axis next;
					int checkX = x;
					int checkY = y;
					int checkZ = z;

					if (prev == null)
					{
						next = rand.nextInt(yChance) == 0 ? Direction.Axis.Y : rand.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
					}
					else switch (prev)
					{
						case X:
							next = rand.nextInt(yChance - 1) == 0 ? Direction.Axis.Y : Direction.Axis.Z;
							break;
						case Y:
							next = rand.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
							break;
						case Z:
							next = rand.nextInt(yChance - 1) == 0 ? Direction.Axis.Y : Direction.Axis.X;
							break;
						default:
							next = rand.nextInt(yChance) == 0 ? Direction.Axis.Y : rand.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
					}

					switch (next)
					{
						case X:
							if (x <= 0)
							{
								checkX = 1;
							}
							else if (x >= 15)
							{
								checkX = 14;
							}
							else
							{
								checkX = x + (rand.nextBoolean() ? 1 : -1);
							}

							break;
						case Y:
							if (y <= 0)
							{
								checkY = 1;
							}
							else if (y >= max - 1)
							{
								checkY = max - 2;
							}
							else
							{
								checkY = y + (rand.nextBoolean() ? 1 : -1);
							}

							break;
						case Z:
							if (z <= 0)
							{
								checkZ = 1;
							}
							else if (z >= 15)
							{
								checkZ = 14;
							}
							else
							{
								checkZ = z + (rand.nextBoolean() ? 1 : -1);
							}

							break;
					}

					BlockState state = chunk.getBlockState(pos.setPos(xStart + checkX, checkY, zStart + checkZ));

					if (targetBlocks.stream().anyMatch(o -> BlockStateHelper.equals(state, o)))
					{
						x = checkX;
						y = checkY;
						z = checkZ;

						break;
					}

					if (++checkCount > 10)
					{
						break;
					}
				}

				BlockState state = chunk.getBlockState(pos.setPos(xStart + x, y, zStart + z));

				if (targetBlocks.stream().anyMatch(o -> BlockStateHelper.equals(state, o)))
				{
					chunk.setBlockState(pos, vein.getBlockState(), false);
				}
			}
		}
	}
}