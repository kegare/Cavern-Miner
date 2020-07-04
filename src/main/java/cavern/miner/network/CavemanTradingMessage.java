package cavern.miner.network;

import java.util.function.Supplier;

import cavern.miner.CavernMod;
import cavern.miner.entity.CavemanEntity;
import cavern.miner.entity.CavemanTrade;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.storage.Miner;
import cavern.miner.storage.MinerRank;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

public class CavemanTradingMessage
{
	private final int entityId;
	private final int entryId;

	public CavemanTradingMessage(int entityId, int entryId)
	{
		this.entityId = entityId;
		this.entryId = entryId;
	}

	public static CavemanTradingMessage decode(final PacketBuffer buf)
	{
		try
		{
			return new CavemanTradingMessage(buf.readInt(), buf.readInt());
		}
		catch (IndexOutOfBoundsException e)
		{
			CavernMod.LOG.error("CavemanTradeEffectMessage: Unexpected end of packet.\\nMessage: " + ByteBufUtil.hexDump(buf, 0, buf.writerIndex()), e);

			return new CavemanTradingMessage(0, -1);
		}
	}

	public static void encode(final CavemanTradingMessage msg, final PacketBuffer buf)
	{
		buf.writeInt(msg.entityId);
		buf.writeInt(msg.entryId);
	}

	public static void handle(final CavemanTradingMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		if (msg.entityId != 0)
		{
			ctx.get().enqueueWork(() ->
			{
				ServerPlayerEntity player = ctx.get().getSender();

				if (player == null)
				{
					return;
				}

				ServerWorld world = player.getServerWorld();

				Entity entity = world.getEntityByID(msg.entityId);
				CavemanEntity caveman;

				if (entity != null && entity instanceof CavemanEntity)
				{
					caveman = (CavemanEntity)entity;
				}
				else return;

				caveman.setSitting(false);

				if (msg.entryId < 0)
				{
					return;
				}

				CavemanTrade.TradeEntry entry;

				try
				{
					entry = caveman.getTradeEntries().get(msg.entryId);
				}
				catch (IndexOutOfBoundsException e)
				{
					return;
				}

				int cost = entry.getCost();
				Miner miner = player.getCapability(CaveCapabilities.MINER).orElse(null);

				if (miner == null || miner.getPoint() < cost)
				{
					return;
				}

				if (MinerRank.getOrder(miner.getRank()) < MinerRank.getOrder(entry.getRank()))
				{
					return;
				}

				miner.addPoint(-cost).sendToClient();

				if (entry instanceof CavemanTrade.EffectEntry)
				{
					player.addPotionEffect(((CavemanTrade.EffectEntry)entry).getEffect());
				}
				else
				{
					BlockPos pos = player.getPosition();
					ItemStack stack = entry.createTradeItem();

					if (!stack.isEmpty())
					{
						world.addEntity(new ItemEntity(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, stack));
					}
				}

				caveman.getTradeEntries().remove(msg.entryId);
			});
		}

		ctx.get().setPacketHandled(true);
	}
}