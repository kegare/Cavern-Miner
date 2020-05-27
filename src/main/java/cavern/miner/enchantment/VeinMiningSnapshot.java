package cavern.miner.enchantment;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cavern.miner.util.CaveUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class VeinMiningSnapshot extends MiningSnapshot
{
	private static final int[][] CHECK_OFFSETS = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}, {-1, 0, 0}, {0, -1, 0}, {0, 0, -1}};

	private BlockPos checkPos;

	public VeinMiningSnapshot(EnchantmentMiner ench, World world, BlockPos pos, IBlockState state, EntityLivingBase entity)
	{
		super(ench, world, pos, state, entity);
	}

	public int getMiningLimit()
	{
		return getLevel() * 5;
	}

	@Override
	public MiningSnapshot checkForMining()
	{
		checkPos = originPos;
		miningTargets = Sets.newTreeSet(this);

		checkChain();

		int limit = getMiningLimit();

		if (miningTargets.size() > limit)
		{
			List<BlockPos> list = Lists.newArrayListWithCapacity(limit);

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
		IBlockState state = world.getBlockState(pos);

		if (state.getBlock().isAir(state, world, pos) || state.getBlockHardness(world, pos) < 0.0F)
		{
			return false;
		}

		return isRedstoneOre(state) && isRedstoneOre(originState) || CaveUtils.isBlockEqual(state, originState);
	}

	private boolean isRedstoneOre(IBlockState state)
	{
		return state.getBlock() == Blocks.REDSTONE_ORE || state.getBlock() == Blocks.LIT_REDSTONE_ORE;
	}
}