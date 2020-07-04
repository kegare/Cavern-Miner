package cavern.miner.block;

import java.util.Random;

import com.google.common.base.Objects;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;

public final class RandomiteDrop
{
	private static final Random RANDOM = new Random();

	public static final DropEntry EMPTY = new EmptyEntry();

	private RandomiteDrop() {}

	public static abstract class DropEntry extends WeightedRandom.Item
	{
		public DropEntry(int weight)
		{
			super(weight);
		}

		public abstract int getMinCount();

		public abstract int getMaxCount();

		public abstract ItemStack getDropItem();
	}

	private static class EmptyEntry extends DropEntry
	{
		private EmptyEntry()
		{
			super(0);
		}

		@Override
		public int getMinCount()
		{
			return 0;
		}

		@Override
		public int getMaxCount()
		{
			return 0;
		}

		@Override
		public ItemStack getDropItem()
		{
			return ItemStack.EMPTY;
		}
	}

	public static class ItemEntry extends DropEntry
	{
		private final Item item;
		private final int min;
		private final int max;

		public ItemEntry(Item item, int weight, int min, int max)
		{
			super(weight);
			this.item = item;
			this.min = min;
			this.max = max;
		}

		public ItemEntry(Item item, int weight, int count)
		{
			this(item, weight, count, count);
		}

		public Item getItem()
		{
			return item;
		}

		@Override
		public int getMinCount()
		{
			return min;
		}

		@Override
		public int getMaxCount()
		{
			return max;
		}

		@Override
		public ItemStack getDropItem()
		{
			return new ItemStack(item, MathHelper.nextInt(RANDOM, min, max));
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null || !(obj instanceof ItemEntry))
			{
				return false;
			}

			return item.getRegistryName().equals(((ItemEntry)obj).item.getRegistryName());
		}

		@Override
		public int hashCode()
		{
			return item.getRegistryName().hashCode();
		}
	}

	public static class ItemStackEntry extends DropEntry
	{
		private final ItemStack stack;
		private final int min;
		private final int max;

		public ItemStackEntry(ItemStack stack, int weight, int min, int max)
		{
			super(weight);
			this.stack = stack;
			this.min = min;
			this.max = max;
		}

		public ItemStackEntry(ItemStack stack, int weight, int count)
		{
			this(stack, weight, count, count);
		}

		public ItemStackEntry(ItemStack stack, int weight)
		{
			this(stack, weight, stack.getCount());
		}

		public ItemStack getItemStack()
		{
			return stack;
		}

		@Override
		public int getMinCount()
		{
			return min;
		}

		@Override
		public int getMaxCount()
		{
			return max;
		}

		@Override
		public ItemStack getDropItem()
		{
			ItemStack result = stack.copy();

			result.setCount(MathHelper.nextInt(RANDOM, min, max));

			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null || !(obj instanceof ItemStackEntry))
			{
				return false;
			}

			return ItemStack.areItemStacksEqual(stack, ((ItemStackEntry)obj).stack);
		}

		@Override
		public int hashCode()
		{
			return Objects.hashCode(stack.getItem().getRegistryName(), stack.getTag());
		}
	}

	public static class TagEntry extends DropEntry
	{
		private final Tag<Item> tag;
		private final int min;
		private final int max;

		public TagEntry(Tag<Item> tag, int weight, int min, int max)
		{
			super(weight);
			this.tag = tag;
			this.min = min;
			this.max = max;
		}

		public TagEntry(Tag<Item> tag, int weight, int count)
		{
			this(tag, weight, count, count);
		}

		public Tag<Item> getTag()
		{
			return tag;
		}

		@Override
		public int getMinCount()
		{
			return min;
		}

		@Override
		public int getMaxCount()
		{
			return max;
		}

		@Override
		public ItemStack getDropItem()
		{
			if (tag.getAllElements().isEmpty())
			{
				return ItemStack.EMPTY;
			}

			return new ItemStack(tag.getRandomElement(RANDOM), MathHelper.nextInt(RANDOM, min, max));
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null || !(obj instanceof TagEntry))
			{
				return false;
			}

			return tag.getId().equals(((TagEntry)obj).tag.getId());
		}

		@Override
		public int hashCode()
		{
			return tag.getId().hashCode();
		}
	}
}