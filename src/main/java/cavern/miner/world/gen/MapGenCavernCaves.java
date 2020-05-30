package cavern.miner.world.gen;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenCaves;

public class MapGenCavernCaves extends MapGenCaves
{
	protected static final IBlockState BLK_STONE = Blocks.STONE.getDefaultState();
	protected static final IBlockState BLK_DIRT = Blocks.DIRT.getDefaultState();
	protected static final IBlockState BLK_GRASS = Blocks.GRASS.getDefaultState();
	protected static final IBlockState BLK_WATER = Blocks.WATER.getDefaultState();

	protected Biome[] biomesForGeneration;

	public void generate(World worldIn, int x, int z, ChunkPrimer primer, Biome[] biomes)
	{
		super.generate(worldIn, x, z, primer);

		biomesForGeneration = biomes;
	}

	@Nullable
	protected Biome getBaseBiome()
	{
		if (biomesForGeneration == null)
		{
			return null;
		}

		Biome baseBiome = null;

		for (Biome biome : biomesForGeneration)
		{
			if (baseBiome == null)
			{
				baseBiome = biome;
			}
			else if (baseBiome != biome)
			{
				return null;
			}
		}

		return baseBiome;
	}

	@Override
	protected void addTunnel(long caveSeed, int chunkX, int chunkZ, ChunkPrimer primer, double blockX, double blockY, double blockZ, float scale, float leftRightRadian, float upDownRadian, int currentY, int targetY, double scaleHeight)
	{
		Random random = new Random(caveSeed);
		int max = world.getActualHeight() - 1;
		double centerX = (chunkX << 4) + 8;
		double centerZ = (chunkZ << 4) + 8;
		float leftRightChange = 0.0F;
		float upDownChange = 0.0F;

		if (targetY <= 0)
		{
			int blockRangeY = range * 16 - 16;
			targetY = blockRangeY - random.nextInt(blockRangeY / 4);
		}

		boolean createFinalRoom = false;

		if (currentY == -1)
		{
			currentY = targetY / 2;
			createFinalRoom = true;
		}

		int nextInterHeight = random.nextInt(targetY / 2) + targetY / 4;

		for (boolean chance = random.nextInt(10) == 0; currentY < targetY; ++currentY)
		{
			double roomWidth = 2.0D + MathHelper.sin(currentY * (float)Math.PI / targetY) * scale;
			double roomHeight = roomWidth * scaleHeight;
			float moveHorizontal = MathHelper.cos(upDownRadian);
			float moveVertical = MathHelper.sin(upDownRadian);
			blockX += MathHelper.cos(leftRightRadian) * moveHorizontal;
			blockY += moveVertical;
			blockZ += MathHelper.sin(leftRightRadian) * moveHorizontal;

			if (chance)
			{
				upDownRadian *= 0.95F;
			}
			else
			{
				upDownRadian *= 0.8F;
			}

			upDownRadian += upDownChange * 0.1F;
			leftRightRadian += leftRightChange * 0.1F;
			upDownChange *= 0.9F;
			leftRightChange *= 0.75F;
			upDownChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			leftRightChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

			if (!createFinalRoom && currentY == nextInterHeight && scale > 1.0F && targetY > 0)
			{
				addTunnel(random.nextLong(), chunkX, chunkZ, primer, blockX, blockY, blockZ, random.nextFloat() * 0.5F + 0.5F, leftRightRadian - (float)Math.PI / 2F, upDownRadian / 3.0F, currentY, targetY, 1.0D);
				addTunnel(random.nextLong(), chunkX, chunkZ, primer, blockX, blockY, blockZ, random.nextFloat() * 0.5F + 0.5F, leftRightRadian + (float)Math.PI / 2F, upDownRadian / 3.0F, currentY, targetY, 1.0D);

				return;
			}

			if (createFinalRoom || random.nextInt(4) != 0)
			{
				double distanceX = blockX - centerX;
				double distanceZ = blockZ - centerZ;
				double distanceY = targetY - currentY;
				double maxDistance = scale + 20.0F;

				if (distanceX * distanceX + distanceZ * distanceZ - distanceY * distanceY > maxDistance * maxDistance)
				{
					return;
				}

				if (blockX >= centerX - 16.0D - roomWidth * 2.0D && blockZ >= centerZ - 16.0D - roomWidth * 2.0D && blockX <= centerX + 16.0D + roomWidth * 2.0D && blockZ <= centerZ + 16.0D + roomWidth * 2.0D)
				{
					int xLow = Math.max(MathHelper.floor(blockX - roomWidth) - chunkX * 16 - 1, 0);
					int xHigh = Math.min(MathHelper.floor(blockX + roomWidth) - chunkX * 16 + 1, 16);
					int yLow = Math.max(MathHelper.floor(blockY - roomHeight) - 1, 1);
					int yHigh = Math.min(MathHelper.floor(blockY + roomHeight) + 1, max - 4);
					int zLow = Math.max(MathHelper.floor(blockZ - roomWidth) - chunkZ * 16 - 1, 0);
					int zHigh = Math.min(MathHelper.floor(blockZ + roomWidth) - chunkZ * 16 + 1, 16);

					for (int x = xLow; x < xHigh; ++x)
					{
						double xScale = (chunkX * 16 + x + 0.5D - blockX) / roomWidth;

						for (int z = zLow; z < zHigh; ++z)
						{
							double zScale = (chunkZ * 16 + z + 0.5D - blockZ) / roomWidth;

							if (xScale * xScale + zScale * zScale < 1.0D)
							{
								for (int y = yHigh - 1; y >= yLow; --y)
								{
									double yScale = (y + 0.5D - blockY) / roomHeight;

									if (yScale > -0.7D && xScale * xScale + yScale * yScale + zScale * zScale < 1.0D)
									{
										digBlock(primer, x, y, z, chunkX, chunkZ, false, null, null);
									}
								}
							}
						}
					}

					if (createFinalRoom)
					{
						break;
					}
				}
			}
		}
	}

	@Override
	protected void recursiveGenerate(World world, int chunkX, int chunkZ, int x, int z, ChunkPrimer primer)
	{
		int max = world.provider.getActualHeight() - 1;
		int chance = rand.nextInt(rand.nextInt(rand.nextInt(25) + 1) + 1);

		if (rand.nextInt(4) != 0)
		{
			chance = 0;
		}

		for (int i = 0; i < chance; ++i)
		{
			double blockX = (chunkX << 4) + rand.nextInt(16);
			double blockY = rand.nextInt(rand.nextInt(max - 8) + 8);
			double blockZ = (chunkZ << 4) + rand.nextInt(16);
			int count = 1;

			if (rand.nextInt(4) == 0)
			{
				addRoom(rand.nextLong(), x, z, primer, blockX, blockY, blockZ);

				count += rand.nextInt(4);
			}

			for (int j = 0; j < count; ++j)
			{
				float leftRightRadian = rand.nextFloat() * (float)Math.PI * 2.0F;
				float upDownRadian = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
				float scale = rand.nextFloat() * 3.0F + rand.nextFloat();

				if (rand.nextInt(8) == 0)
				{
					scale *= rand.nextFloat() * rand.nextFloat() * 3.5F + 1.0F;
				}

				addTunnel(rand.nextLong(), x, z, primer, blockX, blockY, blockZ, scale, leftRightRadian, upDownRadian, 0, 0, 1.15D);
			}
		}
	}

	@Override
	protected void digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop, IBlockState state, IBlockState up)
	{
		if (y - 1 < 10)
		{
			data.setBlockState(x, y, z, BLK_LAVA);
		}
		else
		{
			data.setBlockState(x, y, z, BLK_AIR);
		}
	}
}