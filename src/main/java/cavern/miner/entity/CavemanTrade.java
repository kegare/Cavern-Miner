package cavern.miner.entity;

import javax.annotation.Nullable;

import com.google.common.base.Strings;

import cavern.miner.storage.MinerRank;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.ForgeRegistries;

public final class CavemanTrade
{
	public static final TradeEntry EMPTY = new EmptyEntry();

	private CavemanTrade() {}

	public static CompoundNBT write(TradeEntry entry)
	{
		CompoundNBT nbt = entry.serializeNBT();

		if (entry instanceof ItemStackEntry)
		{
			nbt.putString("EntryType", "Item");
		}
		else if (entry instanceof EnchantedBookEntry)
		{
			nbt.putString("EntryType", "EnchantedBook");
		}
		else if (entry instanceof EffectEntry)
		{
			nbt.putString("EntryType", "Effect");
		}

		return nbt;
	}

	public static TradeEntry read(CompoundNBT nbt)
	{
		String type = nbt.getString("EntryType");

		if (type.equals("Item"))
		{
			return new ItemStackEntry(nbt);
		}
		else if (type.equals("EnchantedBook"))
		{
			return new EnchantedBookEntry(nbt);
		}
		else if (type.equals("Effect"))
		{
			return new EffectEntry(nbt);
		}

		return EMPTY;
	}

	public static abstract class TradeEntry
	{
		private final int cost;
		private final String rank;

		private ItemStack iconItem;
		private ItemStack rankIconItem;

		public TradeEntry(int cost, @Nullable String rank)
		{
			this.cost = cost;
			this.rank = Strings.isNullOrEmpty(rank) ? MinerRank.BEGINNER.getName() : rank;
		}

		public TradeEntry(CompoundNBT nbt)
		{
			this(nbt.getInt("Cost"), nbt.getString("Rank"));
			this.iconItem = ItemStack.read(nbt.getCompound("Icon"));
			this.rankIconItem = ItemStack.read(nbt.getCompound("RankIcon"));
		}

		public TradeEntry(int cost)
		{
			this(cost, null);
		}

		public int getCost()
		{
			return cost;
		}

		public String getRankName()
		{
			return rank;
		}

		public MinerRank.RankEntry getRank()
		{
			return MinerRank.byName(getRankName()).orElse(MinerRank.BEGINNER);
		}

		public abstract ITextComponent getDisplayName();

		public ItemStack createIconItem()
		{
			return createTradeItem();
		}

		public ItemStack getIconItem()
		{
			if (iconItem == null)
			{
				iconItem = createIconItem();
			}

			return iconItem;
		}

		public ItemStack createRankIconItem()
		{
			return getRank().getIconItem().copy();
		}

		public ItemStack getRankIconItem()
		{
			if (rankIconItem == null)
			{
				rankIconItem = createRankIconItem();
			}

			return rankIconItem;
		}

		public abstract ItemStack createTradeItem();

		public CompoundNBT serializeNBT()
		{
			CompoundNBT nbt = new CompoundNBT();

			nbt.putInt("Cost", getCost());
			nbt.putString("Rank", getRankName());
			nbt.put("Icon", getIconItem().write(new CompoundNBT()));
			nbt.put("RankIcon", getRankIconItem().write(new CompoundNBT()));

			return nbt;
		}
	}

	private static class EmptyEntry extends TradeEntry
	{
		private EmptyEntry()
		{
			super(0);
		}

		@Override
		public ITextComponent getDisplayName()
		{
			return ItemStack.EMPTY.getDisplayName();
		}

		@Override
		public ItemStack createTradeItem()
		{
			return ItemStack.EMPTY;
		}

		@Override
		public CompoundNBT serializeNBT()
		{
			return new CompoundNBT();
		}
	}

	public static class ItemStackEntry extends TradeEntry
	{
		private final ItemStack stack;

		public ItemStackEntry(ItemStack stack, int cost, @Nullable String rank)
		{
			super(cost, rank);
			this.stack = stack;
		}

		public ItemStackEntry(CompoundNBT nbt)
		{
			super(nbt);
			this.stack = ItemStack.read(nbt);
		}

		public ItemStack getItemStack()
		{
			return stack;
		}

		@Override
		public ITextComponent getDisplayName()
		{
			return stack.getDisplayName();
		}

		@Override
		public ItemStack createTradeItem()
		{
			return stack.copy();
		}

		@Override
		public CompoundNBT serializeNBT()
		{
			return stack.write(super.serializeNBT());
		}
	}

	public static class EnchantedBookEntry extends TradeEntry
	{
		private final EnchantmentData data;

		public EnchantedBookEntry(EnchantmentData data, int cost, @Nullable String rank)
		{
			super(cost, rank);
			this.data = data;
		}

		public EnchantedBookEntry(CompoundNBT nbt)
		{
			super(nbt);
			this.data = new EnchantmentData(ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryCreate(nbt.getString("Name"))), nbt.getInt("Level"));
		}

		public EnchantmentData getData()
		{
			return data;
		}

		@Override
		public ITextComponent getDisplayName()
		{
			return data.enchantment.getDisplayName(data.enchantmentLevel);
		}

		@Override
		public ItemStack createTradeItem()
		{
			return EnchantedBookItem.getEnchantedItemStack(data);
		}

		@Override
		public CompoundNBT serializeNBT()
		{
			CompoundNBT nbt = super.serializeNBT();

			nbt.putString("Name", data.enchantment.getRegistryName().toString());
			nbt.putInt("Level", data.enchantmentLevel);

			return nbt;
		}
	}

	public static class EffectEntry extends TradeEntry
	{
		private final EffectInstance effect;

		public EffectEntry(EffectInstance effect, int cost, @Nullable String rank)
		{
			super(cost, rank);
			this.effect = effect;
		}

		public EffectEntry(CompoundNBT nbt)
		{
			super(nbt);
			this.effect = EffectInstance.read(nbt);
		}

		public EffectInstance getEffect()
		{
			return effect;
		}

		@Override
		public ITextComponent getDisplayName()
		{
			return effect.getPotion().getDisplayName();
		}

		@Override
		public ItemStack createTradeItem()
		{
			return PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potion.getPotionTypeForName(effect.getPotion().getRegistryName().toString()));
		}

		@Override
		public CompoundNBT serializeNBT()
		{
			return effect.write(super.serializeNBT());
		}
	}
}