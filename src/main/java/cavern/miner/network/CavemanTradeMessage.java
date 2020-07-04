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
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class CavemanTradeMessage
{
	private final int entityId;
	private final List<CavemanTrade.TradeEntry> entries;
	private final int[] inactiveEntries;

	public CavemanTradeMessage(int entityId, List<CavemanTrade.TradeEntry> entries, int[] inactiveEntries)
	{
		this.entityId = entityId;
		this.entries = entries;
		this.inactiveEntries = inactiveEntries;
	}

	public CavemanTradeMessage(int entityId, CompoundNBT nbt, int[] inactiveEntries)
	{
		this(entityId, new ArrayList<>(), inactiveEntries);

		nbt.getList("Entries", Constants.NBT.TAG_COMPOUND).forEach(o -> entries.add(CavemanTrade.read((CompoundNBT)o)));
	}

	public int getEntityId()
	{
		return entityId;
	}

	public List<CavemanTrade.TradeEntry> getEntries()
	{
		return entries;
	}

	public int[] getInactiveEntries()
	{
		return inactiveEntries;
	}

	public CompoundNBT serializeNBT()
	{
		CompoundNBT nbt = new CompoundNBT();
		ListNBT listNBT = new ListNBT();

		entries.forEach(o -> listNBT.add(CavemanTrade.write(o)));

		nbt.put("Entries", listNBT);

		return nbt;
	}

	public static CavemanTradeMessage decode(final PacketBuffer buf)
	{
		try
		{
			return new CavemanTradeMessage(buf.readInt(), buf.readCompoundTag(), buf.readVarIntArray());
		}
		catch (IndexOutOfBoundsException e)
		{
			CavernMod.LOG.error("CavemanTradeMessage: Unexpected end of packet.\\nMessage: " + ByteBufUtil.hexDump(buf, 0, buf.writerIndex()), e);

			return new CavemanTradeMessage(0, Collections.emptyList(), new int[0]);
		}
	}

	public static void encode(final CavemanTradeMessage msg, final PacketBuffer buf)
	{
		buf.writeInt(msg.entityId);
		buf.writeCompoundTag(msg.serializeNBT());
		buf.writeVarIntArray(msg.inactiveEntries);
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