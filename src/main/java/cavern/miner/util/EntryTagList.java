package cavern.miner.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import net.minecraft.tags.Tag;
import net.minecraft.util.NonNullList;

public class EntryTagList<E> implements Iterable<E>
{
	private final NonNullList<E> entryList;
	private final NonNullList<Tag<E>> tagList;

	private boolean changed = true;

	private NonNullList<E> cachedList;

	public static <E> EntryTagList<E> create()
	{
		return new EntryTagList<>();
	}

	public static <E> EntryTagList<E> from(EntryTagList<E> list)
	{
		return new EntryTagList<>(list);
	}

	protected EntryTagList()
	{
		this.entryList = NonNullList.create();
		this.tagList = NonNullList.create();
	}

	protected EntryTagList(EntryTagList<E> list)
	{
		this.entryList = list.entryList;
		this.tagList = list.tagList;
	}

	public NonNullList<E> getEntryList()
	{
		return entryList;
	}

	public NonNullList<Tag<E>> getTagList()
	{
		return tagList;
	}

	public EntryTagList<E> add(E entry)
	{
		changed = entryList.add(entry);

		return this;
	}

	public EntryTagList<E> add(Tag<E> tag)
	{
		changed = tagList.add(tag);

		return this;
	}

	public boolean addEntries(Collection<E> entries)
	{
		return entryList.addAll(entries);
	}

	public boolean addTags(Collection<Tag<E>> tags)
	{
		return tagList.addAll(tags);
	}

	public boolean remove(E entry)
	{
		if (entryList.remove(entry))
		{
			changed = true;

			return true;
		}

		return false;
	}

	public boolean remove(Tag<E> tag)
	{
		if (tagList.remove(tag))
		{
			changed = true;

			return true;
		}

		return false;
	}

	public boolean contains(E entry)
	{
		if (entryList.contains(entry))
		{
			return true;
		}

		for (Tag<E> tag : tagList)
		{
			if (tag.contains(entry))
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

	public NonNullList<E> toList()
	{
		if (tagList.isEmpty())
		{
			return entryList;
		}

		NonNullList<E> list = NonNullList.create();

		if (!entryList.isEmpty())
		{
			list.addAll(entryList);
		}

		for (Tag<E> tag : tagList)
		{
			list.addAll(tag.getAllElements());
		}

		return list;
	}

	public NonNullList<E> getCachedList()
	{
		if (changed)
		{
			cachedList = toList();

			changed = false;
		}

		return cachedList;
	}

	@Override
	public Iterator<E> iterator()
	{
		return getCachedList().iterator();
	}

	public Stream<E> stream()
	{
		return getCachedList().stream();
	}

	public Stream<E> parallelStream()
	{
		return getCachedList().parallelStream();
	}
}