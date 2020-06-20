package cavern.miner.network;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import cavern.miner.CavernMod;
import cavern.miner.client.ClientProxy;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;
import net.minecraftforge.fml.network.NetworkEvent;

public class LoadingScreenMessage
{
	private final Stage stage;

	public LoadingScreenMessage(Stage stage)
	{
		this.stage = stage;
	}

	public static LoadingScreenMessage decode(final PacketBuffer buf)
	{
		try
		{
			Stage stage = Stage.valueOf(buf.readString());

			return new LoadingScreenMessage(stage);
		}
		catch (IndexOutOfBoundsException e)
		{
			CavernMod.LOG.error("LoadingScreenMessage: Unexpected end of packet.\\nMessage: " + ByteBufUtil.hexDump(buf, 0, buf.writerIndex()), e);

			return new LoadingScreenMessage(Stage.ERROR);
		}
	}

	public static void encode(final LoadingScreenMessage msg, final PacketBuffer buf)
	{
		buf.writeString(msg.stage.name());
	}

	public static void handle(final LoadingScreenMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		if (msg.stage != Stage.ERROR)
		{
			ctx.get().enqueueWork(() -> msg.stage.run());
		}

		ctx.get().setPacketHandled(true);
	}

	public enum Stage
	{
		ERROR(null),
		LOAD(() -> ClientProxy::displayLoadingScreen),
		DONE(() -> ClientProxy::closeLoadingScreen);

		private final Supplier<SafeRunnable> task;

		private Stage(@Nullable Supplier<SafeRunnable> task)
		{
			this.task = task;
		}

		public void run()
		{
			if (task != null)
			{
				DistExecutor.safeRunWhenOn(Dist.CLIENT, task);
			}
		}
	}
}