package cavern.miner.storage;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSortedSet;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.ForgeRegistries;

public class MinerRecord implements INBTSerializable<CompoundNBT>, Comparator<Map.Entry<Block, Integer>>
{
	private final Map<Block, Integer> entries = new HashMap<>();

	public int add(Block block)
	{
		return entries.merge(block, 1, Integer::sum);
	}

	public int add(BlockState state)
	{
		return add(state.getBlock());
	}

	public int get(Block block)
	{
		return entries.getOrDefault(block, 0);
	}

	public int get(BlockState state)
	{
		return get(state.getBlock());
	}

	public void set(Block block, int count)
	{
		if (count > 0)
		{
			entries.put(block, count);
		}
	}

	public void set(BlockState state, int count)
	{
		set(state.getBlock(), count);
	}

	public Set<Map.Entry<Block, Integer>> getEntries()
	{
		return ImmutableSortedSet.copyOf(this, entries.entrySet());
	}

	@Override
	public int compare(Map.Entry<Block, Integer> o1, Map.Entry<Block, Integer> o2)
	{
		int i = o2.getValue().compareTo(o1.getValue());

		if (i == 0)
		{
			i = o1.getKey().getRegistryName().compareTo(o2.getKey().getRegistryName());
		}

		return i;
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