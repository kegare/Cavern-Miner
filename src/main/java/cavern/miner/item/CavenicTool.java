package cavern.miner.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public interface CavenicTool
{
	public static int getUseCount(ItemStack stack)
	{
		return stack.hasTag() ? MathHelper.clamp(stack.getTag().getInt("UseCount"), 0, 900) : 0;
	}

	public static ItemStack setUseCount(ItemStack stack, int count)
	{
		stack.getOrCreateTag().putInt("UseCount", count);

		return stack;
	}

	public static ItemStack growUseCount(ItemStack stack)
	{
		return setUseCount(stack, getUseCount(stack) + 1);
	}
}