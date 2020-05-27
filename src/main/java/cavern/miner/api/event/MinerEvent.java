package cavern.miner.api.event;

import cavern.miner.api.data.IMinerAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

public class MinerEvent extends PlayerEvent
{
	private final IMinerAccess miner;

	public MinerEvent(EntityPlayer player, IMinerAccess miner)
	{
		super(player);
		this.miner = miner;
	}

	public IMinerAccess getMiner()
	{
		return miner;
	}

	@Cancelable
	public static class AddPoint extends MinerEvent
	{
		private final int originalPoint;

		private int newPoint;

		public AddPoint(EntityPlayer player, IMinerAccess miner, int point)
		{
			super(player, miner);
			this.originalPoint = point;
			this.newPoint = point;
		}

		public int getPoint()
		{
			return originalPoint;
		}

		public int getNewPoint()
		{
			return newPoint;
		}

		public void setNewPoint(int point)
		{
			newPoint = point;
		}
	}

	@Cancelable
	public static class MineOre extends AddPoint
	{
		private final BlockPos pos;
		private final IBlockState state;

		public MineOre(EntityPlayer player, IMinerAccess miner, int point, BlockPos pos, IBlockState state)
		{
			super(player, miner, point);
			this.pos = pos;
			this.state = state;
		}

		public BlockPos getPos()
		{
			return pos;
		}

		public IBlockState getBlockState()
		{
			return state;
		}
	}

	public static class PromoteRank extends MinerEvent
	{
		public PromoteRank(EntityPlayer player, IMinerAccess miner)
		{
			super(player, miner);
		}
	}
}