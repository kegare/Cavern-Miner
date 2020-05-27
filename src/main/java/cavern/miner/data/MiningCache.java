package cavern.miner.data;

import org.apache.commons.lang3.ObjectUtils;

import cavern.miner.capability.CaveCapabilities;
import cavern.miner.config.MiningConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;

public class MiningCache
{
	private final EntityPlayer player;

	private IBlockState lastMiningBlock;
	private int lastMiningPoint;
	private int miningCombo;
	private long lastMiningTime;

	public MiningCache(EntityPlayer player)
	{
		this.player = player;
	}

	public IBlockState getLastMiningBlock()
	{
		return lastMiningBlock;
	}

	public int getLastMiningPoint()
	{
		return lastMiningPoint;
	}

	public int getMiningCombo()
	{
		return miningCombo;
	}

	public long getLastMiningTime()
	{
		return lastMiningTime;
	}

	public void setLastMining(IBlockState state, int point)
	{
		lastMiningBlock = state;
		lastMiningPoint = point;
		lastMiningTime = player.world.getTotalWorldTime();

		if (MiningConfig.miningCombo)
		{
			++miningCombo;
		}
	}

	public void onUpdate()
	{
		if (miningCombo == 0)
		{
			return;
		}

		if (player.world.getTotalWorldTime()  - lastMiningTime > 15 * 20)
		{
			miningCombo = 0;
		}
	}

	public static MiningCache get(EntityPlayer player)
	{
		return ObjectUtils.defaultIfNull(CaveCapabilities.getCapability(player, CaveCapabilities.MINING_CACHE), new MiningCache(player));
	}
}