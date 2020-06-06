package cavern.miner.network;

import java.util.function.Supplier;

import cavern.miner.CavernMod;
import cavern.miner.init.CaveCapabilities;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class MiningInteractMessage
{
	private BlockState state;
	private int point;

	private boolean failed;

	public MiningInteractMessage(BlockState state, int point)
	{
		this.state = state;
		this.point = point;
	}

	private MiningInteractMessage(boolean failed)
	{
		this.failed = failed;
	}

	public static MiningInteractMessage decode(final PacketBuffer buf)
	{
		try
		{
			BlockState state = Block.getStateById(buf.readVarInt());
			int point = buf.readInt();

			return new MiningInteractMessage(state, point);
		}
		catch (IndexOutOfBoundsException e)
		{
			CavernMod.LOG.error("MiningInteractMessage: Unexpected end of packet.\\nMessage: " + ByteBufUtil.hexDump(buf, 0, buf.writerIndex()), e);

			return new MiningInteractMessage(true);
		}
	}

	public static void encode(final MiningInteractMessage msg, final PacketBuffer buf)
	{
		buf.writeVarInt(Block.getStateId(msg.state));
		buf.writeInt(msg.point);
	}

	public static void handle(final MiningInteractMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		if (!msg.failed)
		{
			ctx.get().enqueueWork(() ->
			{
				PlayerEntity player = DistExecutor.safeRunForDist(() -> CavernMod.PROXY::getClientPlayer, () -> ctx.get()::getSender);

				if (player != null)
				{
					player.getCapability(CaveCapabilities.MINER).ifPresent(o -> o.getMiningCache().put(msg.state, msg.point));
				}
			});
		}

		ctx.get().setPacketHandled(true);
	}
}