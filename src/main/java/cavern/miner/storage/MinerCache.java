package cavern.miner.storage;

import net.minecraft.block.BlockState;

public class MinerCache
{
	private BlockState lastBlock;
	private int lastPoint;

	private long lastTime;

	private int combo;

	public BlockState getLastBlock()
	{
		return lastBlock;
	}

	public int getLastPoint()
	{
		return lastPoint;
	}

	public int getCombo()
	{
		return combo;
	}

	public long getLastTime()
	{
		return lastTime;
	}

	public void put(BlockState state, int point)
	{
		lastBlock = state;
		lastPoint = point;

		lastTime = System.currentTimeMillis();

		++combo;
	}

	public void updateCombo()
	{
		if (combo == 0)
		{
			return;
		}

		if (System.currentTimeMillis() - lastTime > 10000L)
		{
			combo = 0;
		}
	}
}