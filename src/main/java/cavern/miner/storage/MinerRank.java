package cavern.miner.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

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
	private static final Map<String, RankEntry> ENTRY_MAP = new HashMap<>();

	private MinerRank() {}

	public static void load(Iterable<RankEntry> entries)
	{
		ENTRIES.clear();
		ENTRY_MAP.clear();

		int order = 0;
		RankEntry prevEntry = BEGINNER;

		ENTRIES.add(prevEntry);
		ENTRY_MAP.put(prevEntry.getName().toUpperCase(), prevEntry);

		prevEntry.entryOrder = order;

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
				entry.entryOrder = ++order;
				prevEntry.nextEntry = entry;

				prevEntry = entry;

				ENTRY_MAP.put(entry.getName().toUpperCase(), entry);
			}
		}
	}

	public static Optional<RankEntry> byName(String name)
	{
		return Optional.ofNullable(ENTRY_MAP.get(name.toUpperCase()));
	}

	public static ImmutableList<RankEntry> getEntries()
	{
		return ImmutableList.copyOf(ENTRIES);
	}

	public static class RankEntry implements Comparable<RankEntry>
	{
		private final String name;
		private final String translationKey;
		private final int phase;
		private final ItemStack iconItem;

		private int entryOrder = -1;
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

		public int getEntryOrder()
		{
			return entryOrder;
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
			int i = Integer.compare(getEntryOrder(), o.getEntryOrder());

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

			if (entry.equals(next))
			{
				this.nextPhase = -1;
			}
			else
			{
				this.nextPhase = next.getPhase();
			}

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