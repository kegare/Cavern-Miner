package cavern.miner.network;

import java.util.function.Supplier;

import cavern.miner.CavernMod;
import cavern.miner.client.handler.network.LoadingScreenMessageHandler;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class LoadingScreenMessage
{
	private final Stage stage;

	private LoadingScreenMessage(Stage stage)
	{
		this.stage = stage;
	}

	public Stage getStage()
	{
		return stage;
	}

	public static LoadingScreenMessage decode(final PacketBuffer buf)
	{
		try
		{
			return buf.readEnumValue(Stage.class).create();
		}
		catch (IndexOutOfBoundsException e)
		{
			CavernMod.LOG.error("LoadingScreenMessage: Unexpected end of packet.\\nMessage: " + ByteBufUtil.hexDump(buf, 0, buf.writerIndex()), e);

			return Stage.ERROR.create();
		}
	}

	public static void encode(final LoadingScreenMessage msg, final PacketBuffer buf)
	{
		buf.writeEnumValue(msg.stage);
	}

	public static void handle(final LoadingScreenMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		if (msg.stage != Stage.ERROR)
		{
			ctx.get().enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> new LoadingScreenMessageHandler(msg)));
		}

		ctx.get().setPacketHandled(true);
	}

	public enum Stage
	{
		ERROR,
		LOAD,
		DONE;

		public LoadingScreenMessage create()
		{
			return new LoadingScreenMessage(this);
		}
	}
}