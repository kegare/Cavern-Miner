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

	private boolean changed = true;

	private NonNullList<BlockState> cachedList;

	public static BlockStateTagList create()
	{
		return new BlockStateTagList();
	}

	public static BlockStateTagList from(BlockStateTagList list)
	{
		return new BlockStateTagList(list);
	}

	protected BlockStateTagList()
	{
		this.entryList = NonNullList.create();
		this.tagList = NonNullList.create();
	}

	protected BlockStateTagList(BlockStateTagList list)
	{
		this.entryList = list.entryList;
		this.tagList = list.tagList;
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
		changed = entryList.add(entry);

		return this;
	}

	public BlockStateTagList add(Block entry)
	{
		changed = entryList.add(entry.getDefaultState());

		return this;
	}

	public BlockStateTagList add(Tag<Block> tag)
	{
		changed = tagList.add(tag);

		return this;
	}

	public boolean addEntries(Collection<BlockState> entries)
	{
		return entryList.addAll(entries);
	}

	public boolean addTags(Collection<Tag<Block>> tags)
	{
		return tagList.addAll(tags);
	}

	public boolean remove(BlockState entry)
	{
		if (entryList.remove(entry))
		{
			changed = true;

			return true;
		}

		return false;
	}

	public boolean remove(Tag<Block> tag)
	{
		if (tagList.remove(tag))
		{
			changed = true;

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
			if (entry.getBlock().isIn(tag))
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

		changed = true;
	}

	public NonNullList<BlockState> toList()
	{
		if (tagList.isEmpty())
		{
			return entryList;
		}

		NonNullList<BlockState> list = NonNullList.create();

		if (!entryList.isEmpty())
		{
			list.addAll(entryList);
		}

		for (Tag<Block> tag : tagList)
		{
			list.addAll(tag.getAllElements().stream().map(Block::getDefaultState).collect(Collectors.toList()));
		}

		return list;
	}

	public NonNullList<BlockState> getCachedList()
	{
		if (changed)
		{
			cachedList = toList();

			changed = false;
		}

		return cachedList;
	}

	@Override
	public Iterator<BlockState> iterator()
	{
		return getCachedList().iterator();
	}

	public Stream<BlockState> stream()
	{
		return getCachedList().stream();
	}

	public Stream<BlockState> parallelStream()
	{
		return getCachedList().parallelStream();
	}
}