package cavern.miner.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import cavern.miner.CavernMod;
import cavern.miner.client.handler.network.CavemanTradeMessageHandler;
import cavern.miner.entity.CavemanTrade;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class CavemanTradeMessage
{
	private final List<CavemanTrade.TradeEntry> entries;

	public CavemanTradeMessage(List<CavemanTrade.TradeEntry> entries)
	{
		this.entries = entries;
	}

	public CavemanTradeMessage(CompoundNBT nbt)
	{
		this(new ArrayList<>());

		ListNBT listNBT = nbt.getList("Entries", Constants.NBT.TAG_COMPOUND);

		for (INBT entry : listNBT)
		{
			CompoundNBT entryNBT = (CompoundNBT)entry;
			String type = entryNBT.getString("EntryType");

			if (type.equals("Item"))
			{
				entries.add(new CavemanTrade.ItemStackEntry(entryNBT));
			}
			else if (type.equals("EnchantedBook"))
			{
				entries.add(new CavemanTrade.EnchantedBookEntry(entryNBT));
			}
			else if (type.equals("Effect"))
			{
				entries.add(new CavemanTrade.EffectEntry(entryNBT));
			}
		}
	}

	public List<CavemanTrade.TradeEntry> getEntries()
	{
		return entries;
	}

	public CompoundNBT serializeNBT()
	{
		CompoundNBT nbt = new CompoundNBT();
		ListNBT listNBT = new ListNBT();

		for (CavemanTrade.TradeEntry entry : entries)
		{
			CompoundNBT entryNBT = entry.serializeNBT();

			if (entry instanceof CavemanTrade.ItemStackEntry)
			{
				entryNBT.putString("EntryType", "Item");
			}
			else if (entry instanceof CavemanTrade.EnchantedBookEntry)
			{
				entryNBT.putString("EntryType", "EnchantedBook");
			}
			else if (entry instanceof CavemanTrade.EffectEntry)
			{
				entryNBT.putString("EntryType", "Effect");
			}

			listNBT.add(entryNBT);
		}

		nbt.put("Entries", listNBT);

		return nbt;
	}

	public static CavemanTradeMessage decode(final PacketBuffer buf)
	{
		try
		{
			return new CavemanTradeMessage(buf.readCompoundTag());
		}
		catch (IndexOutOfBoundsException e)
		{
			CavernMod.LOG.error("CavemanTradeMessage: Unexpected end of packet.\\nMessage: " + ByteBufUtil.hexDump(buf, 0, buf.writerIndex()), e);

			return new CavemanTradeMessage(Collections.emptyList());
		}
	}

	public static void encode(final CavemanTradeMessage msg, final PacketBuffer buf)
	{
		buf.writeCompoundTag(msg.serializeNBT());
	}

	public static void handle(final CavemanTradeMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		if (!msg.entries.isEmpty())
		{
			ctx.get().enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> new CavemanTradeMessageHandler(msg)));
		}

		ctx.get().setPacketHandled(true);
	}
}