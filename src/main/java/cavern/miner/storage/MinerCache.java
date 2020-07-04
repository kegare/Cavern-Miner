package cavern.miner.storage;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.Util;

public class MinerCache
{
	private BlockState lastBlock;
	private int lastPoint;

	private long lastTime;

	private int combo;

	@Nullable
	public BlockState getLastBlock()
	{
		return lastBlock;
	}

	public int getLastPoint()
	{
		return lastPoint;
	}

	public long getLastTime()
	{
		return lastTime;
	}

	public int getCombo()
	{
		return combo;
	}

	public void put(BlockState state, int point)
	{
		lastBlock = state;
		lastPoint = point;

		lastTime = Util.milliTime();

		++combo;
	}

	public void updateCombo()
	{
		if (combo == 0)
		{
			return;
		}

		if (Util.milliTime() - lastTime > 10000L)
		{
			combo = 0;
		}
	}
}