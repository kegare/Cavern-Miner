package cavern.miner.network;

import java.util.function.Supplier;

import cavern.miner.CavernMod;
import cavern.miner.client.ClientProxy;
import cavern.miner.init.CaveCapabilities;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class MinerPointMessage
{
	private final int point;

	public MinerPointMessage(int point)
	{
		this.point = point;
	}

	public static MinerPointMessage decode(final PacketBuffer buf)
	{
		try
		{
			int point = buf.readInt();

			return new MinerPointMessage(point);
		}
		catch (IndexOutOfBoundsException e)
		{
			CavernMod.LOG.error("MinerPointMessage: Unexpected end of packet.\\nMessage: " + ByteBufUtil.hexDump(buf, 0, buf.writerIndex()), e);

			return new MinerPointMessage(-1);
		}
	}

	public static void encode(final MinerPointMessage msg, final PacketBuffer buf)
	{
		buf.writeInt(msg.point);
	}

	public static void handle(final MinerPointMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		if (msg.point >= 0)
		{
			ctx.get().enqueueWork(() ->
			{
				PlayerEntity player = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientProxy::getPlayer);

				if (player != null)
				{
					player.getCapability(CaveCapabilities.MINER).ifPresent(o -> o.setPoint(msg.point));
				}
			});
		}

		ctx.get().setPacketHandled(true);
	}
}