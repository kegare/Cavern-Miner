package cavern.miner.network;

import java.util.function.Supplier;

import cavern.miner.CavernMod;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.miner.Miner;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class MinerUpdateMessage
{
	private int point;
	private int rank;

	private boolean failed;

	public MinerUpdateMessage(Miner miner)
	{
		this(miner.getPoint(), miner.getRank().ordinal());
	}

	public MinerUpdateMessage(int point, int rank)
	{
		this.point = point;
		this.rank = rank;
	}

	private MinerUpdateMessage(boolean failed)
	{
		this.failed = failed;
	}

	public static MinerUpdateMessage decode(final PacketBuffer buf)
	{
		try
		{
			int point = buf.readInt();
			int rank = buf.readInt();

			return new MinerUpdateMessage(point, rank);
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
		buf.writeInt(msg.rank);
	}

	public static void handle(final MinerUpdateMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(msg, ctx.get()));
	}

	@OnlyIn(Dist.CLIENT)
	public static void handleClient(final MinerUpdateMessage msg, final NetworkEvent.Context ctx)
	{
		if (!msg.failed)
		{
			ctx.enqueueWork(() ->
			{
				Minecraft mc = Minecraft.getInstance();

				if (mc.player != null)
				{
					mc.player.getCapability(CaveCapabilities.MINER).ifPresent(o -> o.setPoint(msg.point).setRank(msg.rank));
				}
			});
		}

		ctx.setPacketHandled(true);
	}
}