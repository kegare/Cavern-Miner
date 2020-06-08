package cavern.miner.util;

import com.google.common.base.Objects;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;

public class ItemStackEntry
{
	private final ItemStack stack;

	public ItemStackEntry(ItemStack stack)
	{
		this.stack = stack;
	}

	public ItemStackEntry(IItemProvider provider)
	{
		this(new ItemStack(provider));
	}

	public ItemStack getItemStack()
	{
		return stack;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}

		if (obj instanceof ItemStack)
		{
			ItemStack o = (ItemStack)obj;

			return ItemStack.areItemStacksEqual(getItemStack(), o);
		}

		if (obj instanceof ItemStackEntry)
		{
			ItemStackEntry o = (ItemStackEntry)obj;

			return ItemStack.areItemStacksEqual(getItemStack(), o.getItemStack());
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		ItemStack o = getItemStack();

		return Objects.hashCode(o.getItem().getRegistryName(), o.getCount(), o.getTag());
	}
}