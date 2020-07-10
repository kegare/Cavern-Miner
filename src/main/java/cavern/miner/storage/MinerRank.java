package cavern.miner.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSortedSet;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class MinerRank
{
	public static final RankEntry BEGINNER = new RankEntry("BEGINNER", 0, new ItemStack(Items.WOODEN_PICKAXE));

	private static final List<RankEntry> ENTRIES = new ArrayList<>();
	private static final Map<String, RankEntry> NAME_LOOKUP = new HashMap<>();

	private MinerRank() {}

	public static void load(Iterable<RankEntry> entries)
	{
		ENTRIES.clear();
		NAME_LOOKUP.clear();

		int index = 0;
		RankEntry prevEntry = BEGINNER;

		ENTRIES.add(prevEntry);
		NAME_LOOKUP.put(prevEntry.getName().toUpperCase(), prevEntry);

		prevEntry.index = index;

		Iterator<RankEntry> iterator = entries.iterator();

		while (iterator.hasNext())
		{
			RankEntry entry = iterator.next();

			if (ENTRIES.contains(entry) || !ENTRIES.add(entry))
			{
				iterator.remove();
			}
			else
			{
				entry.index = ++index;
				prevEntry.nextEntry = entry;

				prevEntry = entry;

				NAME_LOOKUP.put(entry.getName().toUpperCase(), entry);
			}
		}
	}

	public static Optional<RankEntry> byIndex(int index)
	{
		return index < 0 || index >= ENTRIES.size() ? Optional.empty() : Optional.ofNullable(ENTRIES.get(index));
	}

	public static Optional<RankEntry> byName(@Nullable String name)
	{
		return name == null ? Optional.empty() : Optional.ofNullable(NAME_LOOKUP.get(name.toUpperCase()));
	}

	public static Set<RankEntry> getEntries()
	{
		return ImmutableSortedSet.copyOf(ENTRIES);
	}

	public static class RankEntry implements Comparable<RankEntry>
	{
		private final String name;
		private final String translationKey;
		private final int phase;
		private final ItemStack iconItem;

		private int index = -1;
		private RankEntry nextEntry = this;

		public RankEntry(String name, String key, int phase, ItemStack iconItem)
		{
			this.name = name;
			this.translationKey = key;
			this.phase = phase;
			this.iconItem = iconItem;
		}

		public RankEntry(String name, int phase, ItemStack iconItem)
		{
			this(name, "cavern.miner." + name.toLowerCase(), phase, iconItem);
		}

		public String getName()
		{
			return name;
		}

		public String getTranslationKey()
		{
			return translationKey;
		}

		public int getPhase()
		{
			return phase;
		}

		public ItemStack getIconItem()
		{
			return iconItem;
		}

		public int getIndex()
		{
			return index;
		}

		public RankEntry getNextEntry()
		{
			return nextEntry;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null || !(obj instanceof RankEntry))
			{
				return false;
			}

			RankEntry o = (RankEntry)obj;

			return getName().toUpperCase().equals(o.getName().toUpperCase());
		}

		@Override
		public int hashCode()
		{
			return getName().toUpperCase().hashCode();
		}

		@Override
		public int compareTo(RankEntry o)
		{
			int i = Integer.compare(getIndex(), o.getIndex());

			if (i == 0)
			{
				i = Integer.compare(getPhase(), o.getPhase());

				if (i == 0)
				{
					i = getName().compareTo(o.getName());
				}
			}

			return i;
		}
	}

	public static class DisplayEntry
	{
		private final String name;
		private final String translationKey;
		private final ItemStack iconItem;
		private final int nextPhase;

		private RankEntry parent;

		public DisplayEntry(RankEntry entry)
		{
			this.name = entry.getName();
			this.translationKey = entry.getTranslationKey();
			this.iconItem = entry.getIconItem();

			RankEntry next = entry.getNextEntry();

			this.nextPhase = entry.equals(next) ? -1 : next.getPhase();
			this.parent = entry;
		}

		public DisplayEntry(PacketBuffer buf)
		{
			this.name = buf.readString();
			this.translationKey = buf.readString();
			this.iconItem = buf.readItemStack();
			this.nextPhase = buf.readInt();
		}

		public String getName()
		{
			return name;
		}

		public String getTranslationKey()
		{
			return translationKey;
		}

		@OnlyIn(Dist.CLIENT)
		public String getDisplayName()
		{
			String key = getTranslationKey();

			if (I18n.hasKey(key))
			{
				return I18n.format(key);
			}

			return getName();
		}

		public ItemStack getIconItem()
		{
			return iconItem;
		}

		public int getNextPhase()
		{
			return nextPhase;
		}

		@Nullable
		public RankEntry getParent()
		{
			return parent;
		}

		public void write(PacketBuffer buf)
		{
			buf.writeString(getName());
			buf.writeString(getTranslationKey());
			buf.writeItemStack(getIconItem());
			buf.writeInt(getNextPhase());
		}
	}
}