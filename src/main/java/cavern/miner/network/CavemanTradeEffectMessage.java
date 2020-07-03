package cavern.miner.network;

import java.util.function.Supplier;

import cavern.miner.CavernMod;
import cavern.miner.entity.CavemanTrade;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.storage.Miner;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.fml.network.NetworkEvent;

public class CavemanTradeEffectMessage
{
	private final EffectInstance effect;
	private final int cost;

	public CavemanTradeEffectMessage(EffectInstance effect, int cost)
	{
		this.effect = effect;
		this.cost = cost;
	}

	public CavemanTradeEffectMessage(CavemanTrade.EffectEntry entry)
	{
		this.effect = entry.getEffect();
		this.cost = entry.getCost();
	}

	public static CavemanTradeEffectMessage decode(final PacketBuffer buf)
	{
		try
		{
			return new CavemanTradeEffectMessage(EffectInstance.read(buf.readCompoundTag()), buf.readInt());
		}
		catch (IndexOutOfBoundsException e)
		{
			CavernMod.LOG.error("CavemanTradeEffectMessage: Unexpected end of packet.\\nMessage: " + ByteBufUtil.hexDump(buf, 0, buf.writerIndex()), e);

			return new CavemanTradeEffectMessage(null, 0);
		}
	}

	public static void encode(final CavemanTradeEffectMessage msg, final PacketBuffer buf)
	{
		buf.writeCompoundTag(msg.effect.write(new CompoundNBT()));
		buf.writeInt(msg.cost);
	}

	public static void handle(final CavemanTradeEffectMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		if (msg.effect != null)
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

				player.addPotionEffect(msg.effect);
			});
		}

		ctx.get().setPacketHandled(true);
	}
}