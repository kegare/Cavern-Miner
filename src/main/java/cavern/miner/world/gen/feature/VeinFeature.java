package cavern.miner.world.gen.feature;

import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mojang.datafixers.Dynamic;

import cavern.miner.vein.Vein;
import cavern.miner.world.CavernDimension;
import cavern.miner.world.vein.VeinProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
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
			boolean ret = false;

			for (Vein vein : provider.getVeins(world, new ChunkPos(pos), rand))
			{
				for (BlockPos genPos : getPositions(rand, vein, pos).collect(Collectors.toSet()))
				{
					if (placeVein(world, rand, genPos, vein))
					{
						ret = true;
					}
				}
			}

			return ret;
		}

		return false;
	}

	public Stream<BlockPos> getPositions(Random random, Vein vein, BlockPos pos)
	{
		return IntStream.range(0, vein.getCount()).mapToObj(count ->
		{
			int x = random.nextInt(16) + pos.getX();
			int z = random.nextInt(16) + pos.getZ();
			int y = MathHelper.nextInt(random, vein.getMinHeight() + vein.getSize(), vein.getMaxHeight() - vein.getSize());

			return new BlockPos(x, y, z);
		});
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
		double[] d = new double[size * 4];

		for (int i = 0; i < size; ++i)
		{
			float f = i / (float)size;
			double d0 = MathHelper.lerp(f, xStart, xEnd);
			double d1 = MathHelper.lerp(f, yStart, yEnd);
			double d2 = MathHelper.lerp(f, zStart, zEnd);
			double d3 = random.nextDouble() * size / 16.0D;
			double d4 = ((MathHelper.sin((float)Math.PI * f) + 1.0F) * d3 + 1.0D) / 2.0D;

			d[i * 4 + 0] = d0;
			d[i * 4 + 1] = d1;
			d[i * 4 + 2] = d2;
			d[i * 4 + 3] = d4;
		}

		for (int i = 0; i < size - 1; ++i)
		{
			if (!(d[i * 4 + 3] <= 0.0D))
			{
				for (int j = i + 1; j < size; ++j)
				{
					if (!(d[j * 4 + 3] <= 0.0D))
					{
						double d0 = d[i * 4 + 0] - d[j * 4 + 0];
						double d1 = d[i * 4 + 1] - d[j * 4 + 1];
						double d2 = d[i * 4 + 2] - d[j * 4 + 2];
						double d3 = d[i * 4 + 3] - d[j * 4 + 3];

						if (d3 * d3 > d0 * d0 + d1 * d1 + d2 * d2)
						{
							if (d3 > 0.0D)
							{
								d[j * 4 + 3] = -1.0D;
							}
							else
							{
								d[i * 4 + 3] = -1.0D;
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < size; ++i)
		{
			double d0 = d[i * 4 + 3];

			if (!(d0 < 0.0D))
			{
				double d1 = d[i * 4 + 0];
				double d2 = d[i * 4 + 1];
				double d3 = d[i * 4 + 2];
				int i1 = Math.max(MathHelper.floor(d1 - d0), x);
				int i2 = Math.max(MathHelper.floor(d2 - d0), y);
				int i3 = Math.max(MathHelper.floor(d3 - d0), z);
				int i4 = Math.max(MathHelper.floor(d1 + d0), i1);
				int i5 = Math.max(MathHelper.floor(d2 + d0), i2);
				int i6 = Math.max(MathHelper.floor(d3 + d0), i3);

				for (int j = i1; j <= i4; ++j)
				{
					double d4 = (j + 0.5D - d1) / d0;

					if (d4 * d4 < 1.0D)
					{
						for (int k = i2; k <= i5; ++k)
						{
							double d5 = (k + 0.5D - d2) / d0;

							if (d4 * d4 + d5 * d5 < 1.0D)
							{
								for (int l = i3; l <= i6; ++l)
								{
									double d6 = (l + 0.5D - d3) / d0;

									if (d4 * d4 + d5 * d5 + d6 * d6 < 1.0D)
									{
										int index = j - x + (k - y) * width + (l - z) * width * height;

										if (!posSet.get(index))
										{
											posSet.set(index);
											pos.setPos(j, k, l);

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