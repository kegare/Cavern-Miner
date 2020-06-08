package cavern.miner.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;

public final class MinerRank
{
	public static final MinerRank.RankEntry BEGINNER = new MinerRank.RankEntry("BEGINNER", 0, new ItemStack(Items.WOODEN_PICKAXE));

	private static final List<RankEntry> ENTRIES = Lists.newArrayList(BEGINNER);

	private MinerRank() {}

	public static boolean add(RankEntry entry)
	{
		if (ENTRIES.contains(entry))
		{
			return false;
		}

		if (ENTRIES.add(entry))
		{
			Collections.sort(ENTRIES);

			int i = ENTRIES.indexOf(entry);

			if (i < ENTRIES.size() - 1)
			{
				entry.nextEntry = ENTRIES.get(++i);
			}
			else
			{
				entry.nextEntry = entry;
			}

			return true;
		}

		return false;
	}

	public static void addAll(Collection<RankEntry> entries)
	{
		Iterator<MinerRank.RankEntry> iterator = entries.iterator();

		while (iterator.hasNext())
		{
			MinerRank.RankEntry entry = iterator.next();

			if (ENTRIES.contains(entry) || !ENTRIES.add(entry))
			{
				iterator.remove();
			}
		}

		Collections.sort(ENTRIES);

		for (int i = 0, max = ENTRIES.size() - 1; i <= max; ++i)
		{
			RankEntry entry = ENTRIES.get(i);

			if (i < max)
			{
				entry.nextEntry = ENTRIES.get(i + 1);
			}
			else
			{
				entry.nextEntry = entry;
			}
		}
	}

	public static RankEntry get(String name)
	{
		for (RankEntry entry : ENTRIES)
		{
			if (entry.getName().equalsIgnoreCase(name))
			{
				return entry;
			}
		}

		return BEGINNER;
	}

	public static RankEntry getOrCreate(CompoundNBT nbt)
	{
		RankEntry entry = get(nbt.getString("Name"));

		if (entry == null)
		{
			entry = new RankEntry(nbt);

			if (!add(entry))
			{
				return BEGINNER;
			}
		}

		return entry;
	}

	public static ImmutableList<RankEntry> getEntries()
	{
		return ImmutableList.copyOf(ENTRIES);
	}

	public static class RankEntry implements Comparable<RankEntry>, INBTSerializable<CompoundNBT>
	{
		private String name;
		private String translationKey;
		private int phase;
		private ItemStack iconItem;

		private RankEntry nextEntry;

		public RankEntry(String name, String key, int phase, ItemStack iconItem)
		{
			this.name = name.toUpperCase();
			this.translationKey = key;
			this.phase = phase;
			this.iconItem = iconItem;
		}

		public RankEntry(String name, int phase, ItemStack iconItem)
		{
			this(name, "cavern.miner." + name.toLowerCase(), phase, iconItem);
		}

		public RankEntry(CompoundNBT nbt)
		{
			this.deserializeNBT(nbt);
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

		public int getPhase()
		{
			return phase;
		}

		public ItemStack getIconItem()
		{
			return iconItem;
		}

		public RankEntry getNextEntry()
		{
			return nextEntry == null ? this : nextEntry;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null || !(obj instanceof RankEntry))
			{
				return false;
			}

			RankEntry o = (RankEntry)obj;

			return getName().equals(o.getName()) || getPhase() == o.getPhase();
		}

		@Override
		public int hashCode()
		{
			return Integer.hashCode(getPhase());
		}

		@Override
		public int compareTo(RankEntry o)
		{
			return Integer.compare(getPhase(), o.getPhase());
		}

		@Override
		public CompoundNBT serializeNBT()
		{
			CompoundNBT nbt = new CompoundNBT();

			nbt.putString("Name", getName());
			nbt.putString("TranslationKey", getTranslationKey());
			nbt.putInt("Phase", getPhase());
			nbt.put("IconItem", getIconItem().write(new CompoundNBT()));

			return nbt;
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt)
		{
			this.name = nbt.getString("Name");
			this.translationKey = nbt.getString("TranslationKey");
			this.phase = nbt.getInt("Phase");
			this.iconItem = ItemStack.read(nbt.getCompound("IconItem"));
		}
	}
}