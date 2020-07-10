package cavern.miner.world.vein;

import com.google.common.collect.ImmutableSet;

import cavern.miner.util.BlockStateHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class Vein implements Comparable<Vein>
{
	private final BlockState block;
	private final Vein.Properties properties;

	public Vein(BlockState block, Vein.Properties properties)
	{
		this.block = block;
		this.properties = properties;
	}

	public BlockState getBlockState()
	{
		return block;
	}

	public ImmutableSet<BlockState> getTargetBlocks()
	{
		if (properties.targetBlocks == null)
		{
			return ImmutableSet.of();
		}

		return ImmutableSet.copyOf(properties.targetBlocks);
	}

	public int getCount()
	{
		return properties.count;
	}

	public int getSize()
	{
		return properties.size;
	}

	public int getMinHeight()
	{
		return properties.minHeight;
	}

	public int getMaxHeight()
	{
		return properties.maxHeight;
	}

	public boolean isTargetBlock(BlockState... states)
	{
		if (properties.targetBlocks == null || properties.targetBlocks.length == 0)
		{
			return false;
		}

		for (BlockState state : states)
		{
			for (BlockState target : properties.targetBlocks)
			{
				if (BlockStateHelper.equals(state, target))
				{
					return true;
				}
			}
		}

		return false;
	}

	public boolean isStoneTarget()
	{
		return properties.targetBlocks == Properties.NATURAL_STONES || isTargetBlock(Properties.NATURAL_STONES);
	}

	@Override
	public int compareTo(Vein o)
	{
		int i = Integer.compare(getCount(), o.getCount());

		if (i == 0)
		{
			i = Integer.compare(getSize(), o.getSize());
		}

		return i;
	}

	public static class Properties
	{
		private static final BlockState[] NATURAL_STONES = {Blocks.STONE.getDefaultState(), Blocks.ANDESITE.getDefaultState(), Blocks.DIORITE.getDefaultState(), Blocks.GRANITE.getDefaultState()};

		private BlockState[] targetBlocks = NATURAL_STONES;
		private int count = 10;
		private int size = 5;
		private int minHeight = 1;
		private int maxHeight = 255;

		public Vein.Properties target(BlockState... value)
		{
			targetBlocks = value;

			return this;
		}

		public Vein.Properties count(int value)
		{
			count = value;

			return this;
		}

		public Vein.Properties size(int value)
		{
			size = value;

			return this;
		}

		public Vein.Properties min(int value)
		{
			minHeight = value;

			return this;
		}

		public Vein.Properties max(int value)
		{
			maxHeight = value;

			return this;
		}
	}
}