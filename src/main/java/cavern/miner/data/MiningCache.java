package cavern.miner.data;

import org.apache.commons.lang3.ObjectUtils;

import cavern.miner.capability.CaveCapabilities;
import cavern.miner.config.MiningConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;

public class MiningCache
{
	private final EntityPlayer player;

	private IBlockState lastBlock;
	private int lastPoint;
	private int combo;
	private long lastTime;

	public MiningCache(EntityPlayer player)
	{
		this.player = player;
	}

	public IBlockState getLastBlock()
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

	public void setLastMining(IBlockState state, int point)
	{
		lastBlock = state;
		lastPoint = point;
		lastTime = player.world.getTotalWorldTime();

		if (MiningConfig.miningCombo)
		{
			++combo;
		}
	}

	public void onUpdate()
	{
		if (combo == 0)
		{
			return;
		}

		if (player.world.getTotalWorldTime()  - lastTime > 15 * 20)
		{
			combo = 0;
		}
	}

	public static MiningCache get(EntityPlayer player)
	{
		return ObjectUtils.defaultIfNull(CaveCapabilities.getCapability(player, CaveCapabilities.MINING_CACHE), new MiningCache(player));
	}
}