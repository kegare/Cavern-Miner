package cavern.miner.util;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;

public class BlockStateHelper
{
	public static boolean equals(@Nullable BlockState o1, @Nullable BlockState o2)
	{
		if (o1 == null || o2 == null)
		{
			return false;
		}

		if (o1.getBlock() != o2.getBlock())
		{
			return false;
		}

		return o1.getValues().equals(o2.getValues());
	}
}