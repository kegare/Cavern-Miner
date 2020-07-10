package cavern.miner.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;

public class ItemStackTagList implements Iterable<ItemStack>
{
	private final NonNullList<ItemStack> entryList;
	private final NonNullList<Tag<Item>> tagList;

	private NonNullList<ItemStack> allList;

	public ItemStackTagList()
	{
		this(NonNullList.create(), NonNullList.create());
	}

	public ItemStackTagList(NonNullList<ItemStack> entries, NonNullList<Tag<Item>> tags)
	{
		this.entryList = entries;
		this.tagList = tags;
	}

	public NonNullList<ItemStack> getEntryList()
	{
		return entryList;
	}

	public NonNullList<Tag<Item>> getTagList()
	{
		return tagList;
	}

	public ItemStackTagList add(ItemStack entry)
	{
		if (entryList.add(entry))
		{
			allList = null;
		}

		return this;
	}

	public ItemStackTagList add(IItemProvider entry)
	{
		if (entryList.add(new ItemStack(entry)))
		{
			allList = null;
		}

		return this;
	}

	public ItemStackTagList add(Tag<Item> tag)
	{
		if (tagList.add(tag))
		{
			allList = null;
		}

		return this;
	}

	public ItemStackTagList addEntries(Collection<ItemStack> entries)
	{
		if (entryList.addAll(entries))
		{
			allList = null;
		}

		return this;
	}

	public ItemStackTagList addTags(Collection<Tag<Item>> tags)
	{
		if (tagList.addAll(tags))
		{
			allList = null;
		}

		return this;
	}

	public boolean remove(ItemStack entry)
	{
		if (entryList.remove(entry))
		{
			allList = null;

			return true;
		}

		return false;
	}

	public boolean remove(Tag<Item> tag)
	{
		if (tagList.remove(tag))
		{
			allList = null;

			return true;
		}

		return false;
	}

	public boolean contains(ItemStack entry)
	{
		for (ItemStack stack : entryList)
		{
			if (ItemStack.areItemsEqualIgnoreDurability(stack, entry))
			{
				return true;
			}
		}

		for (Tag<Item> tag : tagList)
		{
			if (entry.getItem().isIn(tag))
			{
				return true;
			}
		}

		return false;
	}

	public boolean contains(Item entry)
	{
		for (ItemStack state : entryList)
		{
			if (state.getItem() == entry)
			{
				return true;
			}
		}

		for (Tag<Item> tag : tagList)
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

	public NonNullList<ItemStack> toList()
	{
		return tagList.isEmpty() ? entryList :
			Stream.concat(entryList.stream(), tagList.stream().flatMap(o -> o.getAllElements().stream()).map(ItemStack::new)).collect(Collectors.toCollection(NonNullList::create));
	}

	public NonNullList<ItemStack> getAll()
	{
		if (allList == null)
		{
			allList = toList();
		}

		return allList;
	}

	@Override
	public Iterator<ItemStack> iterator()
	{
		return getAll().iterator();
	}
}