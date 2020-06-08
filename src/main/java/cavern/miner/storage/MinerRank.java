package cavern.miner.storage;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class MinerRank
{
	public static final RankEntry BEGINNER = new RankEntry("BEGINNER", 0, new ItemStack(Items.WOODEN_PICKAXE));

	private static final List<RankEntry> ENTRIES = Lists.newArrayList(BEGINNER);

	private MinerRank() {}

	public static void set(Collection<RankEntry> entries)
	{
		Iterator<RankEntry> iterator = entries.iterator();

		while (iterator.hasNext())
		{
			RankEntry entry = iterator.next();

			if (ENTRIES.contains(entry) || !ENTRIES.add(entry))
			{
				iterator.remove();
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

	public static RankEntry getNextEntry(RankEntry current)
	{
		int i = ENTRIES.indexOf(current);

		if (i < 0)
		{
			return BEGINNER;
		}

		if (i < ENTRIES.size() - 1)
		{
			return ENTRIES.get(++i);
		}

		return current;
	}

	public static ImmutableList<RankEntry> getEntries()
	{
		return ImmutableList.copyOf(ENTRIES);
	}

	public static class RankEntry
	{
		private final String name;
		private final String translationKey;
		private final int phase;
		private final ItemStack iconItem;

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
	}

	public static class DisplayEntry
	{
		private String name;
		private String translationKey;
		private ItemStack iconItem;
		private int nextPhase;

		private RankEntry parent;

		public DisplayEntry(RankEntry entry)
		{
			this.name = entry.getName();
			this.translationKey = entry.getTranslationKey();
			this.iconItem = entry.getIconItem();

			RankEntry next = getNextEntry(entry);

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
			this.read(buf);
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

		public void read(PacketBuffer buf)
		{
			name = buf.readString();
			translationKey = buf.readString();
			iconItem = buf.readItemStack();
			nextPhase = buf.readInt();
		}
	}
}