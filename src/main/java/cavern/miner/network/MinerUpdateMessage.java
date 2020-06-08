package cavern.miner.network;

import java.util.function.Supplier;

import cavern.miner.CavernMod;
import cavern.miner.client.ClientProxy;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.storage.Miner;
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

	private boolean pointOnly;

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

	public MinerUpdateMessage(int point)
	{
		this.point = point;
		this.pointOnly = true;
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
			boolean pointOnly = buf.readBoolean();

			if (pointOnly)
			{
				return new MinerUpdateMessage(point);
			}

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
		buf.writeBoolean(msg.pointOnly);

		if (!msg.pointOnly)
		{
			msg.rank.write(buf);
		}
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
					Miner miner = player.getCapability(CaveCapabilities.MINER).orElse(null);

					if (miner != null)
					{
						miner.setPoint(msg.point);

						if (!msg.pointOnly)
						{
							miner.setDisplayRank(msg.rank);
						}
					}
				}
			});
		}

		ctx.get().setPacketHandled(true);
	}
}