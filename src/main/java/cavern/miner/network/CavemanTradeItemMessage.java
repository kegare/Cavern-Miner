package cavern.miner.network;

import java.util.function.Supplier;

import cavern.miner.CavernMod;
import cavern.miner.entity.CavemanTrade;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.storage.Miner;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

public class CavemanTradeItemMessage
{
	private final ItemStack stack;
	private final int cost;

	public CavemanTradeItemMessage(ItemStack stack, int cost)
	{
		this.stack = stack;
		this.cost = cost;
	}

	public CavemanTradeItemMessage(CavemanTrade.TradeEntry entry)
	{
		this.stack = entry.createTradeItem();
		this.cost = entry.getCost();
	}

	public static CavemanTradeItemMessage decode(final PacketBuffer buf)
	{
		try
		{
			return new CavemanTradeItemMessage(buf.readItemStack(), buf.readInt());
		}
		catch (IndexOutOfBoundsException e)
		{
			CavernMod.LOG.error("CavemanTradeItemMessage: Unexpected end of packet.\\nMessage: " + ByteBufUtil.hexDump(buf, 0, buf.writerIndex()), e);

			return new CavemanTradeItemMessage(CavemanTrade.EMPTY);
		}
	}

	public static void encode(final CavemanTradeItemMessage msg, final PacketBuffer buf)
	{
		buf.writeItemStack(msg.stack);
		buf.writeInt(msg.cost);
	}

	public static void handle(final CavemanTradeItemMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		if (!msg.stack.isEmpty())
		{
			ctx.get().enqueueWork(() ->
			{
				ServerPlayerEntity player = ctx.get().getSender();

				if (player == null)
				{
					return;
				}

				Miner miner = player.getCapability(CaveCapabilities.MINER).orElse(null);

				if (miner == null || miner.getPoint() < msg.cost)
				{
					return;
				}

				miner.addPoint(-msg.cost).sendToClient();

				ServerWorld world = player.getServerWorld();
				BlockPos pos = player.getPosition();

				world.addEntity(new ItemEntity(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, msg.stack));
			});
		}

		ctx.get().setPacketHandled(true);
	}
}