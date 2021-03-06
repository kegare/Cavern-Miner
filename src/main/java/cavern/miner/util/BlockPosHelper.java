package cavern.miner.util;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class BlockPosHelper
{
	@Nullable
	public static BlockPos findPos(IWorld world, BlockPos originPos, int radius, Predicate<BlockPos> predicate)
	{
		return findPos(world, originPos, radius, 1, world.getMaxHeight() - 1, predicate);
	}

	@Nullable
	public static BlockPos findPos(IWorld world, BlockPos originPos, int radius, int min, int max, Predicate<BlockPos> predicate)
	{
		BlockPos.Mutable findPos = new BlockPos.Mutable(originPos);
		int maxHeight = world.getMaxHeight();

		for (int i = 1; i <= radius; ++i)
		{
			for (int j = -i; j <= i; ++j)
			{
				for (int k = -i; k <= i; ++k)
				{
					if (Math.abs(j) < i && Math.abs(k) < i) continue;

					int x = originPos.getX() + j;
					int z = originPos.getZ() + k;

					if (!world.getWorldBorder().contains(findPos.setPos(x, 0, z)))
					{
						continue;
					}

					int dist = 0;
					boolean minFlag = false;
					boolean maxFlag = false;

					while (!minFlag || !maxFlag)
					{
						if (dist <= 0)
						{
							dist = -dist + 1;
						}
						else
						{
							dist = -dist;
						}

						if (dist > maxHeight)
						{
							break;
						}

						int y = originPos.getY() + dist;

						if (y < min)
						{
							minFlag = true;

							continue;
						}

						if (y > max)
						{
							maxFlag = true;

							continue;
						}

						if (predicate.test(findPos.setPos(x, y, z)))
						{
							return findPos.toImmutable();
						}
					}
				}
			}
		}

		return null;
	}
}