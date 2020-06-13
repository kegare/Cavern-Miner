package cavern.miner.storage;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

public class MinerRecord implements INBTSerializable<CompoundNBT>
{
	private final Map<Block, Integer> entries = new HashMap<>();

	public int add(Block block)
	{
		return entries.merge(block, 1, Integer::sum);
	}

	public int get(Block block)
	{
		return entries.getOrDefault(block, 0);
	}

	public void set(Block block, int count)
	{
		if (count > 0)
		{
			entries.put(block, count);
		}
	}

	public ImmutableList<Map.Entry<Block, Integer>> getEntries()
	{
		return ImmutableList.copyOf(entries.entrySet());
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		CompoundNBT nbt = new CompoundNBT();

		for (Map.Entry<Block, Integer> entry : entries.entrySet())
		{
			nbt.putInt(entry.getKey().getRegistryName().toString(), entry.getValue());
		}

		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		for (String key : nbt.keySet())
		{
			int count = nbt.getInt(key);

			if (count > 0)
			{
				Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(key));

				if (block != null && !(block instanceof AirBlock))
				{
					set(block, count);
				}
			}
		}
	}
}