package cavern.miner.world.gen.feature;

import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;

import com.google.common.collect.AbstractIterator;
import com.mojang.datafixers.Dynamic;

import cavern.miner.world.dimension.CavernDimension;
import cavern.miner.world.vein.Vein;
import cavern.miner.world.vein.VeinProvider;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

public class VeinFeature extends Feature<NoFeatureConfig>
{
	public VeinFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> factory)
	{
		super(factory);
	}

	@Override
	public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config)
	{
		if (world.getDimension() instanceof CavernDimension)
		{
			VeinProvider provider = ((CavernDimension)world.getDimension()).getVeinProvider();
			boolean result = false;

			for (Vein vein : provider.getVeins())
			{
				for (BlockPos genPos : getPositions(world, pos, rand, vein))
				{
					if (genPos.equals(BlockPos.ZERO))
					{
						continue;
					}

					if (placeVein(world, rand, genPos, vein))
					{
						result = true;
					}
				}
			}

			for (Vein vein : provider.getAutoEntries())
			{
				for (BlockPos genPos : getPositions(world, pos, rand, vein))
				{
					if (genPos.equals(BlockPos.ZERO))
					{
						continue;
					}

					if (placeVein(world, rand, genPos, vein))
					{
						result = true;
					}
				}
			}

			return result;
		}

		return false;
	}

	public Iterable<BlockPos> getPositions(IWorld world, BlockPos pos, Random random, Vein vein)
	{
		return () -> new AbstractIterator<BlockPos>()
		{
			final int size = vein.getSize();
			final int min = vein.getMinHeight() + size;
			final int max = vein.getMaxHeight() - size;
			final IntList targetY = new IntArrayList();
			final BlockPos.Mutable findPos = new BlockPos.Mutable();

			int count = vein.getCount();

			@Override
			protected BlockPos computeNext()
			{
				if (--count <= 0)
				{
					return endOfData();
				}

				int x = random.nextInt(16) + pos.getX();
				int z = random.nextInt(16) + pos.getZ();

				targetY.clear();

				for (int y = Math.max(min, 1); y <= Math.min(max, world.getMaxHeight() - 1); ++y)
				{
					if (!world.isAirBlock(findPos.setPos(x, y, z)) && vein.isTargetBlock(world.getBlockState(findPos)))
					{
						targetY.add(y);
					}
				}

				return targetY.isEmpty() ? BlockPos.ZERO : findPos.setPos(x, targetY.getInt(random.nextInt(targetY.size())), z);
			}
		};
	}

	public boolean placeVein(IWorld world, Random rand, BlockPos pos, Vein vein)
	{
		int size = vein.getSize();
		float angle = rand.nextFloat() * (float)Math.PI;
		float f = size / 8.0F;
		int i = MathHelper.ceil((size / 16.0F * 2.0F + 1.0F) / 2.0F);
		double xStart = pos.getX() + MathHelper.sin(angle) * f;
		double xEnd = pos.getX() - MathHelper.sin(angle) * f;
		double zStart = pos.getZ() + MathHelper.cos(angle) * f;
		double zEnd = pos.getZ() - MathHelper.cos(angle) * f;
		double yStart = pos.getY() + rand.nextInt(3) - 2;
		double yEnd = pos.getY() + rand.nextInt(3) - 2;
		int x = pos.getX() - MathHelper.ceil(f) - i;
		int y = pos.getY() - 2 - i;
		int z = pos.getZ() - MathHelper.ceil(f) - i;
		int width = 2 * (MathHelper.ceil(f) + i);
		int height = 2 * (2 + i);

		for (int j = x; j <= x + width; ++j)
		{
			for (int k = z; k <= z + width; ++k)
			{
				if (y <= world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, j, k))
				{
					return doPlace(world, rand, vein, xStart, xEnd, zStart, zEnd, yStart, yEnd, x, y, z, width, height);
				}
			}
		}

		return false;
	}

	protected boolean doPlace(IWorld world, Random random, Vein vein, double xStart, double xEnd, double zStart, double zEnd, double yStart, double yEnd, int x, int y, int z, int width, int height)
	{
		int size = vein.getSize();
		int count = 0;
		BitSet posSet = new BitSet(width * height * width);
		BlockPos.Mutable pos = new BlockPos.Mutable();
		double[] dc = new double[size * 4];

		for (int i = 0; i < size; ++i)
		{
			float f = i / (float)size;
			double centerX = MathHelper.lerp(f, xStart, xEnd);
			double centerY = MathHelper.lerp(f, yStart, yEnd);
			double centerZ = MathHelper.lerp(f, zStart, zEnd);
			double d = random.nextDouble() * size / 16.0D;
			double ds = ((MathHelper.sin((float)Math.PI * f) + 1.0F) * d + 1.0D) / 2.0D;

			dc[i * 4 + 0] = centerX;
			dc[i * 4 + 1] = centerY;
			dc[i * 4 + 2] = centerZ;
			dc[i * 4 + 3] = ds;
		}

		for (int i = 0; i < size - 1; ++i)
		{
			if (!(dc[i * 4 + 3] <= 0.0D))
			{
				for (int j = i + 1; j < size; ++j)
				{
					if (!(dc[j * 4 + 3] <= 0.0D))
					{
						double d0 = dc[i * 4 + 0] - dc[j * 4 + 0];
						double d1 = dc[i * 4 + 1] - dc[j * 4 + 1];
						double d2 = dc[i * 4 + 2] - dc[j * 4 + 2];
						double d3 = dc[i * 4 + 3] - dc[j * 4 + 3];

						if (d3 * d3 > d0 * d0 + d1 * d1 + d2 * d2)
						{
							if (d3 > 0.0D)
							{
								dc[j * 4 + 3] = -1.0D;
							}
							else
							{
								dc[i * 4 + 3] = -1.0D;
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < size; ++i)
		{
			double ds = dc[i * 4 + 3];

			if (!(ds < 0.0D))
			{
				double centerX = dc[i * 4 + 0];
				double centerY = dc[i * 4 + 1];
				double centerZ = dc[i * 4 + 2];
				int minX = Math.max(MathHelper.floor(centerX - ds), x);
				int minY = Math.max(MathHelper.floor(centerY - ds), y);
				int minZ = Math.max(MathHelper.floor(centerZ - ds), z);
				int maxX = Math.max(MathHelper.floor(centerX + ds), minX);
				int maxY = Math.max(MathHelper.floor(centerY + ds), minY);
				int maxZ = Math.max(MathHelper.floor(centerZ + ds), minZ);

				for (int posX = minX; posX <= maxX; ++posX)
				{
					double dx = (posX + 0.5D - centerX) / ds;

					if (dx * dx < 1.0D)
					{
						for (int posY = minY; posY <= maxY; ++posY)
						{
							double dy = (posY + 0.5D - centerY) / ds;

							if (dx * dx + dy * dy < 1.0D)
							{
								for (int posZ = minZ; posZ <= maxZ; ++posZ)
								{
									double dz = (posZ + 0.5D - centerZ) / ds;

									if (dx * dx + dy * dy + dz * dz < 1.0D)
									{
										int index = posX - x + (posY - y) * width + (posZ - z) * width * height;

										if (!posSet.get(index))
										{
											posSet.set(index);
											pos.setPos(posX, posY, posZ);

											if (vein.isTargetBlock(world.getBlockState(pos)))
											{
												world.setBlockState(pos, vein.getBlockState(), 2);

												++count;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return count > 0;
	}
}