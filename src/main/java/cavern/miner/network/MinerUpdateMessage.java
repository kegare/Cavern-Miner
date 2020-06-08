package cavern.miner.network;

import java.util.function.Supplier;

import cavern.miner.CavernMod;
import cavern.miner.client.ClientProxy;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.storage.MinerRank;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class MinerUpdateMessage
{
	private int point;
	private MinerRank.DisplayEntry rank;

	private boolean failed;

	public MinerUpdateMessage(int point, MinerRank.DisplayEntry rank)
	{
		this.point = point;
		this.rank = rank;
	}

	public MinerUpdateMessage(int point, MinerRank.RankEntry rank)
	{
		this(point, new MinerRank.DisplayEntry(rank));
	}

	private MinerUpdateMessage(boolean failed)
	{
		this.failed = failed;
	}

	public MinerRank.RankEntry getRank()
	{
		return rank == null ? null : rank.getParent();
	}

	public static MinerUpdateMessage decode(final PacketBuffer buf)
	{
		try
		{
			int point = buf.readInt();

			return new MinerUpdateMessage(point, new MinerRank.DisplayEntry(buf));
		}
		catch (IndexOutOfBoundsException e)
		{
			CavernMod.LOG.error("MinerUpdateMessage: Unexpected end of packet.\\nMessage: " + ByteBufUtil.hexDump(buf, 0, buf.writerIndex()), e);

			return new MinerUpdateMessage(true);
		}
	}

	public static void encode(final MinerUpdateMessage msg, final PacketBuffer buf)
	{
		buf.writeInt(msg.point);

		msg.rank.write(buf);
	}

	public static void handle(final MinerUpdateMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		if (!msg.failed)
		{
			ctx.get().enqueueWork(() ->
			{
				PlayerEntity player = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientProxy::getClientPlayer);

				if (player != null)
				{
					player.getCapability(CaveCapabilities.MINER).ifPresent(o -> o.setPoint(msg.point).setDisplayRank(msg.rank));
				}
			});
		}

		ctx.get().setPacketHandled(true);
	}
}