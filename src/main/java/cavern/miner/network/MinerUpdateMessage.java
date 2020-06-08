package cavern.miner.network;

import java.util.function.Supplier;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import cavern.miner.CavernMod;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.storage.Miner;
import cavern.miner.storage.MinerRank;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

public class MinerUpdateMessage
{
	private int point;
	private MinerRank.RankEntry rank;

	private boolean pointOnly;

	private boolean failed;

	public MinerUpdateMessage(int point, MinerRank.RankEntry rank)
	{
		this.point = point;
		this.rank = rank;
	}

	public MinerUpdateMessage(int point)
	{
		this.point = point;
		this.pointOnly = true;
	}

	private MinerUpdateMessage(boolean failed)
	{
		this.failed = failed;
	}

	public MinerRank.RankEntry getRank()
	{
		return rank;
	}

	public static MinerUpdateMessage decode(final PacketBuffer buf)
	{
		try
		{
			int point = buf.readInt();
			boolean pointOnly = buf.readBoolean();

			if (pointOnly)
			{
				return new MinerUpdateMessage(point);
			}

			MinerRank.RankEntry rank = MinerRank.getOrCreate(JsonToNBT.getTagFromJson(buf.readString()));

			return new MinerUpdateMessage(point, rank);
		}
		catch (IndexOutOfBoundsException | CommandSyntaxException e)
		{
			CavernMod.LOG.error("MinerUpdateMessage: Unexpected end of packet.\\nMessage: " + ByteBufUtil.hexDump(buf, 0, buf.writerIndex()), e);

			return new MinerUpdateMessage(true);
		}
	}

	public static void encode(final MinerUpdateMessage msg, final PacketBuffer buf)
	{
		buf.writeInt(msg.point);
		buf.writeBoolean(msg.pointOnly);

		if (!msg.pointOnly)
		{
			buf.writeString(msg.rank.serializeNBT().toString());
		}
	}

	public static void handle(final MinerUpdateMessage msg, final Supplier<NetworkEvent.Context> ctx)
	{
		if (!msg.failed)
		{
			ctx.get().enqueueWork(() ->
			{
				PlayerEntity player = DistExecutor.safeRunForDist(() -> CavernMod.PROXY::getClientPlayer, () -> ctx.get()::getSender);

				if (player != null)
				{
					Miner miner = player.getCapability(CaveCapabilities.MINER).orElse(null);

					if (miner != null)
					{
						miner.setPoint(msg.point);

						if (!msg.pointOnly)
						{
							miner.setRank(msg.rank);
						}
					}
				}
			});
		}

		ctx.get().setPacketHandled(true);
	}
}