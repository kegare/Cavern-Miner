package cavern.miner.network.client;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import cavern.miner.data.Miner;
import cavern.miner.util.BlockMeta;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.GameData;

public class MiningRecordsMessage implements IPlayerMessage<MiningRecordsMessage, IMessage>
{
	private Map<BlockMeta, Integer> records;

	public MiningRecordsMessage() {}

	public MiningRecordsMessage(Miner miner)
	{
		this.records = miner.getMiningRecords();
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		records = Maps.newHashMap();

		int size = buf.readInt();

		for (int i = 0; i < size; ++i)
		{
			IBlockState state = GameData.getBlockStateIDMap().getByValue(buf.readInt());
			int count = buf.readInt();

			records.put(new BlockMeta(state), count);
		}
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(records.size());

		for (Entry<BlockMeta, Integer> record : records.entrySet())
		{
			buf.writeInt(GameData.getBlockStateIDMap().get(record.getKey().getBlockState()));
			buf.writeInt(record.getValue());
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IMessage process(EntityPlayerSP player)
	{
		Miner miner = Miner.get(player, true);

		if (miner != null)
		{
			for (Entry<BlockMeta, Integer> record : records.entrySet())
			{
				miner.setMiningRecord(record.getKey(), record.getValue());
			}
		}

		return null;
	}
}