package cavern.miner.item;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.init.CaveBlocks;
import cavern.miner.init.CaveEnchantments;
import cavern.miner.util.ItemStackEntry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;

public class CavernItemGroup extends ItemGroup
{
	public static final CavernItemGroup INSTANCE = new CavernItemGroup();

	public CavernItemGroup()
	{
		super("cavern");
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public ItemStack createIcon()
	{
		return new ItemStack(Blocks.MOSSY_COBBLESTONE);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void fill(NonNullList<ItemStack> items)
	{
		NonNullList<ItemStackEntry> entries = NonNullList.create();

		for (CavernPortalBlock portal : CaveBlocks.CAVE_PORTALS.get())
		{
			for (BlockState state : portal.getFrameBlocks())
			{
				Item item = state.getBlock().asItem();

				if (item != null && item != Items.AIR)
				{
					ItemStackEntry entry = new ItemStackEntry(item);

					if (!entries.contains(entry))
					{
						entries.add(entry);
					}
				}
			}

			for (ItemStack stack : portal.getTriggerItems())
			{
				if (!stack.isEmpty())
				{
					ItemStackEntry entry = new ItemStackEntry(stack);

					if (!entries.contains(entry))
					{
						entries.add(entry);
					}
				}
			}
		}

		entries.forEach(o -> items.add(o.getItemStack()));

		NonNullList<ItemStack> list = NonNullList.create();

		super.fill(list);

		items.addAll(list);

		for (RegistryObject<Enchantment> entry : CaveEnchantments.REGISTRY.getEntries())
		{
			entry.ifPresent(ench ->
			{
				if (ench.type != null)
				{
					for (int i = ench.getMinLevel(); i <= ench.getMaxLevel(); ++i)
					{
						items.add(EnchantedBookItem.getEnchantedItemStack(new EnchantmentData(ench, i)));
					}
				}
			});
		}
	}
}