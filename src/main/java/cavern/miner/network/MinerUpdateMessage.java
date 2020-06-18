package cavern.miner.network;

import java.util.function.Supplier;

import javax.annotation.Nullable;

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
	private final int point;
	private final MinerRank.DisplayEntry displayRank;

	public MinerUpdateMessage(int point, MinerRank.DisplayEntry rank)
	{
		this.point = point;
		this.displayRank = rank;
	}

	public MinerUpdateMessage(int point, MinerRank.RankEntry rank)
	{
		this(point, new MinerRank.DisplayEntry(rank));
	}

	public int getPoint()
	{
		return point;
	}

	@Nullable
	public MinerRank.RankEntry getRank()
	{
		return displayRank == null ? null : displayRank.getParent();
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

			return new MinerUpdateMessage(0, (MinerRank.DisplayEntry)null);
		}
	}

	public static void encode(final MinerUpdateMessage msg, final PacketBuffer buf)
	{
		buf.writeInt(msg.point);

		msg.displayRank.write(buf);
	}

	public static void handle(final MinerUpdateMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		if (msg.displayRank != null)
		{
			ctx.get().enqueueWork(() ->
			{
				PlayerEntity player = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientProxy::getPlayer);

				if (player != null)
				{
					player.getCapability(CaveCapabilities.MINER).ifPresent(o -> o.setPoint(msg.point).setDisplayRank(msg.displayRank));
				}
			});
		}

		ctx.get().setPacketHandled(true);
	}
}