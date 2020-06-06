package cavern.miner.item;

import java.util.Comparator;

import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CavernItemGroup extends ItemGroup implements Comparator<ItemStack>
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
		items.add(new ItemStack(Blocks.MOSSY_COBBLESTONE));
		items.add(new ItemStack(Blocks.MOSSY_STONE_BRICKS));
		items.add(new ItemStack(Items.EMERALD));

		NonNullList<ItemStack> list = NonNullList.create();

		super.fill(list);

		list.sort(this);

		items.addAll(list);
	}

	@Override
	public int compare(ItemStack o1, ItemStack o2)
	{
		return -Boolean.compare(o1.getItem() instanceof BlockItem, o2.getItem() instanceof BlockItem);
	}
}