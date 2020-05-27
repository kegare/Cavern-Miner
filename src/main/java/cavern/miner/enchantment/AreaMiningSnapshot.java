package cavern.miner.enchantment;

import com.google.common.collect.Sets;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AreaMiningSnapshot extends MiningSnapshot
{
	public AreaMiningSnapshot(EnchantmentMiner ench, World world, BlockPos pos, IBlockState state, EntityLivingBase entity)
	{
		super(ench, world, pos, state, entity);
	}

	public int getRowRange()
	{
		return getLevel();
	}

	public int getColumnRange()
	{
		return getLevel();
	}

	@Override
	public MiningSnapshot checkForMining()
	{
		miningTargets = Sets.newTreeSet(this);

		switch (EnumFacing.getDirectionFromEntityLiving(originPos, miner).getAxis())
		{
			case X:
				checkX();
				break;
			case Y:
				checkY();
				break;
			case Z:
				checkZ();
				break;
		}

		return this;
	}

	protected void checkX()
	{
		int row = getRowRange();
		int column = getColumnRange();

		for (int i = -row; i <= row; ++i)
		{
			for (int j = -column; j <= column; ++j)
			{
				offer(originPos.add(0, j, i));
			}
		}
	}

	protected void checkY()
	{
		int row = getRowRange();
		int column = getColumnRange();

		for (int i = -row; i <= row; ++i)
		{
			for (int j = -column; j <= column; ++j)
			{
				offer(originPos.add(i, 0, j));
			}
		}
	}

	protected void checkZ()
	{
		int row = getRowRange();
		int column = getColumnRange();

		for (int i = -row; i <= row; ++i)
		{
			for (int j = -column; j <= column; ++j)
			{
				offer(originPos.add(i, j, 0));
			}
		}
	}
}