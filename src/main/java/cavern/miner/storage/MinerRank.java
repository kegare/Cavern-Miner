package cavern.miner.storage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;

public class MinerRank
{
	public static final MinerRank.RankEntry BEGINNER = new MinerRank.RankEntry("BEGINNER", 0, new ItemStack(Items.WOODEN_PICKAXE));

	private static final List<RankEntry> RANK_ENTRIES = Lists.newArrayList(BEGINNER);
	private static final Map<RankEntry, RankEntry> NEXT_CACHE = Maps.newHashMap();

	public static boolean add(RankEntry entry)
	{
		if (RANK_ENTRIES.contains(entry))
		{
			return false;
		}

		if (RANK_ENTRIES.add(entry))
		{
			Collections.sort(RANK_ENTRIES);

			NEXT_CACHE.clear();

			return true;
		}

		return false;
	}

	public static RankEntry get(String name)
	{
		for (RankEntry entry : RANK_ENTRIES)
		{
			if (entry.getName().equals(name))
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

	public static RankEntry next(@Nullable RankEntry current)
	{
		if (current == null)
		{
			return BEGINNER;
		}

		RankEntry next = NEXT_CACHE.get(current);

		if (next != null)
		{
			return next;
		}

		int max = RANK_ENTRIES.size() - 1;
		int index = RANK_ENTRIES.indexOf(current);

		if (index < 0)
		{
			return BEGINNER;
		}

		if (index < max)
		{
			next = RANK_ENTRIES.get(index + 1);
		}
		else
		{
			next = current;
		}

		NEXT_CACHE.put(current, next);

		return next;
	}

	public static ImmutableList<RankEntry> getEntries()
	{
		return ImmutableList.copyOf(RANK_ENTRIES);
	}

	public static class RankEntry implements Comparable<RankEntry>, INBTSerializable<CompoundNBT>
	{
		private String name;
		private String translationKey;
		private int phase;
		private ItemStack iconItem;

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