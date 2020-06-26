package cavern.miner.enchantment;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AreaMinerSnapshot extends MinerSnapshot
{
	public AreaMinerSnapshot(EnchantmentMiner ench, World world, BlockPos pos, BlockState state, LivingEntity entity)
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
	public MinerSnapshot checkForMining()
	{
		switch (Direction.getFacingDirections(miner)[0].getAxis())
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