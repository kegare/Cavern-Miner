package cavern.miner.network;

import java.util.function.Supplier;

import cavern.miner.CavernMod;
import cavern.miner.client.ClientProxy;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.storage.MinerRecord;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class MinerRecordMessage
{
	private final CompoundNBT nbt;

	public MinerRecordMessage(CompoundNBT nbt)
	{
		this.nbt = nbt;
	}

	public MinerRecordMessage(MinerRecord record)
	{
		this.nbt = record.serializeNBT();
	}

	public static MinerRecordMessage decode(final PacketBuffer buf)
	{
		try
		{
			CompoundNBT nbt = buf.readCompoundTag();

			return new MinerRecordMessage(nbt);
		}
		catch (IndexOutOfBoundsException e)
		{
			CavernMod.LOG.error("MinerRecordMessage: Unexpected end of packet.\\nMessage: " + ByteBufUtil.hexDump(buf, 0, buf.writerIndex()), e);

			return new MinerRecordMessage(new CompoundNBT());
		}
	}

	public static void encode(final MinerRecordMessage msg, final PacketBuffer buf)
	{
		buf.writeCompoundTag(msg.nbt);
	}

	public static void handle(final MinerRecordMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		if (msg.nbt != null)
		{
			ctx.get().enqueueWork(() ->
			{
				PlayerEntity player = DistExecutor.safeCallWhenOn(Dist.CLIENT, () -> ClientProxy::getPlayer);

				if (player != null)
				{
					player.getCapability(CaveCapabilities.MINER).ifPresent(o ->
					{
						o.getRecord().deserializeNBT(msg.nbt);

						DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientProxy::displayMinerRecordScreen);
					});
				}
			});
		}

		ctx.get().setPacketHandled(true);
	}
}