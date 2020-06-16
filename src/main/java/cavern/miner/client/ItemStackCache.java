package cavern.miner.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemStackCache
{
	private static final Map<Item, ItemStack> ITEM_CACHE = new HashMap<>();

	public static ItemStack get(IItemProvider provider)
	{
		return ITEM_CACHE.computeIfAbsent(provider.asItem(), Item::getDefaultInstance);
	}
}