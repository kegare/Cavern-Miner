package cavern.miner.network;

import java.util.function.Supplier;

import cavern.miner.CavernMod;
import cavern.miner.client.ClientProxy;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class LoadingScreenMessage
{
	private final int phase;

	public LoadingScreenMessage(int phase)
	{
		this.phase = phase;
	}

	public static LoadingScreenMessage decode(final PacketBuffer buf)
	{
		try
		{
			int phase = buf.readInt();

			return new LoadingScreenMessage(phase);
		}
		catch (IndexOutOfBoundsException e)
		{
			CavernMod.LOG.error("LoadingScreenMessage: Unexpected end of packet.\\nMessage: " + ByteBufUtil.hexDump(buf, 0, buf.writerIndex()), e);

			return new LoadingScreenMessage(-1);
		}
	}

	public static void encode(final LoadingScreenMessage msg, final PacketBuffer buf)
	{
		buf.writeInt(msg.phase);
	}

	public static void handle(final LoadingScreenMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		if (msg.phase >= 0)
		{
			ctx.get().enqueueWork(() ->
			{
				switch (msg.phase)
				{
					case 0:
						DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientProxy::displayLoadingScreen);
						break;
					case 1:
						DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientProxy::closeLoadingScreen);
						break;
					default:
				}
			});
		}

		ctx.get().setPacketHandled(true);
	}
}