package cavern.miner.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
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

	private boolean changed = true;

	private NonNullList<ItemStack> cachedList;

	public static ItemStackTagList create()
	{
		return new ItemStackTagList();
	}

	public static ItemStackTagList from(ItemStackTagList list)
	{
		return new ItemStackTagList(list);
	}

	protected ItemStackTagList()
	{
		this.entryList = NonNullList.create();
		this.tagList = NonNullList.create();
	}

	protected ItemStackTagList(ItemStackTagList list)
	{
		this.entryList = list.entryList;
		this.tagList = list.tagList;
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
		changed = entryList.add(entry);

		return this;
	}

	public ItemStackTagList add(IItemProvider entry)
	{
		changed = entryList.add(new ItemStack(entry));

		return this;
	}

	public ItemStackTagList add(Tag<Item> tag)
	{
		changed = tagList.add(tag);

		return this;
	}

	public boolean addEntries(Collection<ItemStack> entries)
	{
		return entryList.addAll(entries);
	}

	public boolean addTags(Collection<Tag<Item>> tags)
	{
		return tagList.addAll(tags);
	}

	public boolean remove(ItemStack entry)
	{
		if (entryList.remove(entry))
		{
			changed = true;

			return true;
		}

		return false;
	}

	public boolean remove(Tag<Item> tag)
	{
		if (tagList.remove(tag))
		{
			changed = true;

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

		changed = true;
	}

	public NonNullList<ItemStack> toList()
	{
		if (tagList.isEmpty())
		{
			return entryList;
		}

		NonNullList<ItemStack> list = NonNullList.create();

		if (!entryList.isEmpty())
		{
			list.addAll(entryList);
		}

		for (Tag<Item> tag : tagList)
		{
			list.addAll(tag.getAllElements().stream().map(ItemStack::new).collect(Collectors.toList()));
		}

		return list;
	}

	public NonNullList<ItemStack> getCachedList()
	{
		if (changed)
		{
			cachedList = toList();

			changed = false;
		}

		return cachedList;
	}

	public ItemStack getRandomElement(Random random)
	{
		NonNullList<ItemStack> list = getCachedList();

		if (list.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		return list.get(random.nextInt(list.size()));
	}

	@Override
	public Iterator<ItemStack> iterator()
	{
		return getCachedList().iterator();
	}

	public Stream<ItemStack> stream()
	{
		return getCachedList().stream();
	}

	public Stream<ItemStack> parallelStream()
	{
		return getCachedList().parallelStream();
	}
}