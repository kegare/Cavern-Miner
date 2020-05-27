package cavern.miner.world.gen;

import java.util.Random;

import cavern.miner.config.manager.CaveVein;
import cavern.miner.world.VeinProviderCavern;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class VeinGenerator
{
	private final VeinProviderCavern provider;

	public VeinGenerator(VeinProviderCavern provider)
	{
		this.provider = provider;
	}

	public void generate(World world, Random rand, Biome[] biomes, int chunkX, int chunkZ, ChunkPrimer primer)
	{
		int max = world.getActualHeight() - 1;

		for (CaveVein vein : provider.getVeins(chunkX, chunkZ))
		{
			if (vein == null || vein.getWeight() <= 0 || vein.getSize() <= 0)
			{
				continue;
			}

			for (int veinCount = 0; veinCount < vein.getWeight(); ++veinCount)
			{
				int yChance = rand.nextInt(3) + 3;
				int originX = rand.nextInt(16);
				int originY = MathHelper.getInt(rand, vein.getMinHeight(), vein.getMaxHeight());
				int originZ = rand.nextInt(16);
				int x = originX;
				int y = originY;
				int z = originZ;
				EnumFacing.Axis prev = null;

				for (int oreCount = 0; oreCount < vein.getSize(); ++oreCount)
				{
					int checkCount = 0;

					while (oreCount > 0)
					{
						EnumFacing.Axis next;
						int checkX = x;
						int checkY = y;
						int checkZ = z;

						if (prev == null)
						{
							next = rand.nextInt(yChance) == 0 ? EnumFacing.Axis.Y : rand.nextBoolean() ? EnumFacing.Axis.X : EnumFacing.Axis.Z;
						}
						else switch (prev)
						{
							case X:
								next = rand.nextInt(yChance - 1) == 0 ? EnumFacing.Axis.Y : EnumFacing.Axis.Z;
								break;
							case Y:
								next = rand.nextBoolean() ? EnumFacing.Axis.X : EnumFacing.Axis.Z;
								break;
							case Z:
								next = rand.nextInt(yChance - 1) == 0 ? EnumFacing.Axis.Y : EnumFacing.Axis.X;
								break;
							default:
								next = rand.nextInt(yChance) == 0 ? EnumFacing.Axis.Y : rand.nextBoolean() ? EnumFacing.Axis.X : EnumFacing.Axis.Z;
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

						IBlockState state = primer.getBlockState(checkX, checkY, checkZ);

						if (state.getBlock() == vein.getTarget().getBlock() && state.getBlock().getMetaFromState(state) == vein.getTarget().getMeta())
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

					IBlockState state = primer.getBlockState(x, y, z);

					if (state.getBlock() == vein.getTarget().getBlock() && state.getBlock().getMetaFromState(state) == vein.getTarget().getMeta())
					{
						if (vein.getBiomes() == null || vein.getBiomes().length <= 0 || vein.containsBiome(biomes[x * 16 + z]))
						{
							primer.setBlockState(x, y, z, vein.getBlockMeta().getBlockState());
						}
					}
				}
			}
		}
	}
}