package cavern.miner.network.client;

import cavern.miner.data.Miner;
import cavern.miner.data.MiningCache;
import cavern.miner.util.BlockMeta;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.GameData;

public class MiningMessage implements IPlayerMessage<MiningMessage, IMessage>
{
	private int point;
	private int rank;
	private IBlockState state;
	private int amount;

	public MiningMessage() {}

	public MiningMessage(Miner miner, IBlockState state, int amount)
	{
		this.point = miner.getPoint();
		this.rank = miner.getRank();
		this.state = state;
		this.amount = amount;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		point = buf.readInt();
		rank = buf.readInt();
		state = GameData.getBlockStateIDMap().getByValue(buf.readInt());
		amount = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(point);
		buf.writeInt(rank);
		buf.writeInt(GameData.getBlockStateIDMap().get(state));
		buf.writeInt(amount);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage process(EntityPlayerSP player)
	{
		Miner miner = Miner.get(player, true);

		if (miner != null)
		{
			miner.setPoint(point, false);
			miner.setRank(rank, false);
			miner.addMiningRecord(new BlockMeta(state));
		}

		MiningCache.get(player).setLastMining(state, amount);

		return null;
	}
}