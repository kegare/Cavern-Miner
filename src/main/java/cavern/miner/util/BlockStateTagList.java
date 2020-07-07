package cavern.miner.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.Tag;
import net.minecraft.util.NonNullList;

public class BlockStateTagList implements Iterable<BlockState>
{
	private final NonNullList<BlockState> entryList;
	private final NonNullList<Tag<Block>> tagList;

	private NonNullList<BlockState> allList;

	public BlockStateTagList()
	{
		this(NonNullList.create(), NonNullList.create());
	}

	public BlockStateTagList(NonNullList<BlockState> entries, NonNullList<Tag<Block>> tags)
	{
		this.entryList = entries;
		this.tagList = tags;
	}

	public NonNullList<BlockState> getEntryList()
	{
		return entryList;
	}

	public NonNullList<Tag<Block>> getTagList()
	{
		return tagList;
	}

	public BlockStateTagList add(BlockState entry)
	{
		if (entryList.add(entry))
		{
			allList = null;
		}

		return this;
	}

	public BlockStateTagList add(Block entry)
	{
		if (entryList.add(entry.getDefaultState()))
		{
			allList = null;
		}

		return this;
	}

	public BlockStateTagList add(Tag<Block> tag)
	{
		if (tagList.add(tag))
		{
			allList = null;
		}

		return this;
	}

	public BlockStateTagList addEntries(Collection<BlockState> entries)
	{
		if (entryList.addAll(entries))
		{
			allList = null;
		}

		return this;
	}

	public BlockStateTagList addTags(Collection<Tag<Block>> tags)
	{
		if (tagList.addAll(tags))
		{
			allList = null;
		}

		return this;
	}

	public boolean remove(BlockState entry)
	{
		if (entryList.remove(entry))
		{
			allList = null;

			return true;
		}

		return false;
	}

	public boolean remove(Tag<Block> tag)
	{
		if (tagList.remove(tag))
		{
			allList = null;

			return true;
		}

		return false;
	}

	public boolean contains(BlockState entry)
	{
		if (entryList.contains(entry))
		{
			return true;
		}

		for (Tag<Block> tag : tagList)
		{
			if (entry.getBlock().isIn(tag))
			{
				return true;
			}
		}

		return false;
	}

	public boolean contains(Block entry)
	{
		for (BlockState state : entryList)
		{
			if (state.getBlock() == entry)
			{
				return true;
			}
		}

		for (Tag<Block> tag : tagList)
		{
			if (entry.isIn(tag))
			{
				return true;
			}
		}

		return false;
	}

	public boolean isEmpty()
	{
		return entryList.isEmpty() && tagList.isEmpty();
	}

	public void clear()
	{
		entryList.clear();
		tagList.clear();

		allList = null;
	}

	public NonNullList<BlockState> toList()
	{
		return tagList.isEmpty() ? entryList :
			Stream.concat(entryList.stream(), tagList.stream().flatMap(o -> o.getAllElements().stream()).map(Block::getDefaultState)).collect(Collectors.toCollection(NonNullList::create));
	}

	public NonNullList<BlockState> getAll()
	{
		if (allList == null)
		{
			allList = toList();
		}

		return allList;
	}

	@Override
	public Iterator<BlockState> iterator()
	{
		return getAll().iterator();
	}
}