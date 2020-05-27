package cavern.miner.network.client;

import cavern.miner.data.Miner;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MinerMessage implements IPlayerMessage<MinerMessage, IMessage>
{
	private int point;
	private int rank;

	public MinerMessage() {}

	public MinerMessage(Miner miner)
	{
		this.point = miner.getPoint();
		this.rank = miner.getRank();
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		point = buf.readInt();
		rank = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(point);
		buf.writeInt(rank);
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
		}

		return null;
	}
}