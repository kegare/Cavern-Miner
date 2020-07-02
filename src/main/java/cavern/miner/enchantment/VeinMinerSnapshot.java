package cavern.miner.enchantment;

import java.util.ArrayList;
import java.util.List;

import cavern.miner.util.BlockStateHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;

public class VeinMinerSnapshot extends MinerSnapshot
{
	private static final int[][] CHECK_OFFSETS = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}, {-1, 0, 0}, {0, -1, 0}, {0, 0, -1}};

	private BlockPos checkPos;

	public VeinMinerSnapshot(EnchantmentMiner ench, IBlockReader reader, BlockPos pos, BlockState state, LivingEntity entity)
	{
		super(ench, reader, pos, state, entity);
	}

	public int getMiningLimit()
	{
		return getLevel() * 5;
	}

	@Override
	public MinerSnapshot checkForMining()
	{
		checkPos = originPos;

		checkChain();

		int limit = getMiningLimit();

		if (miningTargets.size() > limit)
		{
			List<BlockPos> list = new ArrayList<>(limit);

			for (BlockPos pos : miningTargets)
			{
				if (list.size() >= limit)
				{
					break;
				}

				list.add(pos);
			}

			miningTargets.clear();
			miningTargets.addAll(list);
		}

		return this;
	}

	protected void checkChain()
	{
		boolean flag;

		do
		{
			flag = false;

			BlockPos pos = checkPos;

			for (int[] offset : CHECK_OFFSETS)
			{
				if (offer(pos.add(offset[0], offset[1], offset[2])))
				{
					checkChain();

					if (!flag)
					{
						flag = true;
					}
				}
			}
		}
		while (flag);
	}

	@Override
	public boolean offer(BlockPos pos)
	{
		if (MathHelper.floor(Math.sqrt(originPos.distanceSq(pos))) > 30)
		{
			return false;
		}

		if (super.offer(pos))
		{
			checkPos = pos;

			return true;
		}

		return false;
	}

	@Override
	public boolean validTarget(BlockPos pos)
	{
		BlockState state = reader.getBlockState(pos);

		if (state.getBlock().isAir(state, reader, pos) || state.getBlockHardness(reader, pos) < 0.0F)
		{
			return false;
		}

		return BlockStateHelper.equals(state, originState);
	}
}