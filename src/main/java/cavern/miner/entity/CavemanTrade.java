package cavern.miner.entity;

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

		private ItemStack iconItem;

		public TradeEntry(int cost)
		{
			this.cost = cost;
		}

		public int getCost()
		{
			return cost;
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

		public abstract ItemStack createTradeItem();

		public abstract CompoundNBT serializeNBT();
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

		public ItemStackEntry(ItemStack stack, int cost)
		{
			super(cost);
			this.stack = stack;
		}

		public ItemStackEntry(CompoundNBT nbt)
		{
			this(ItemStack.read(nbt), nbt.getInt("Cost"));
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
			CompoundNBT nbt = stack.serializeNBT();

			nbt.putInt("Cost", getCost());

			return nbt;
		}
	}

	public static class EnchantedBookEntry extends TradeEntry
	{
		private final EnchantmentData data;

		public EnchantedBookEntry(EnchantmentData data, int cost)
		{
			super(cost);
			this.data = data;
		}

		public EnchantedBookEntry(CompoundNBT nbt)
		{
			this(new EnchantmentData(ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.tryCreate(nbt.getString("Name"))), nbt.getInt("Level")), nbt.getInt("Cost"));
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
			CompoundNBT nbt = new CompoundNBT();

			nbt.putString("Name", data.enchantment.getRegistryName().toString());
			nbt.putInt("Level", data.enchantmentLevel);
			nbt.putInt("Cost", getCost());

			return nbt;
		}
	}

	public static class EffectEntry extends TradeEntry
	{
		private final EffectInstance effect;

		public EffectEntry(EffectInstance effect, int cost)
		{
			super(cost);
			this.effect = effect;
		}

		public EffectEntry(CompoundNBT nbt)
		{
			this(EffectInstance.read(nbt), nbt.getInt("Cost"));
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
			CompoundNBT nbt = effect.write(new CompoundNBT());

			nbt.putInt("Cost", getCost());

			return nbt;
		}
	}
}