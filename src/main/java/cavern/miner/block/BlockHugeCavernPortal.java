package cavern.miner.block;

import cavern.miner.config.HugeCavernConfig;
import cavern.miner.util.CaveUtils;
import cavern.miner.world.CaveDimensions;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.DimensionType;
import net.minecraftforge.oredict.OreDictionary;

public class BlockHugeCavernPortal extends BlockCavernPortal
{
	public BlockHugeCavernPortal()
	{
		super();
		this.setUnlocalizedName("portal.hugeCavern");
	}

	@Override
	public DimensionType getDimension()
	{
		return CaveDimensions.HUGE_CAVERN;
	}

	@Override
	public boolean isTriggerItem(ItemStack stack)
	{
		if (!HugeCavernConfig.triggerItems.isEmpty())
		{
			return HugeCavernConfig.triggerItems.hasItemStack(stack);
		}

		if (!stack.isEmpty() && stack.getItem() == Items.DIAMOND)
		{
			return true;
		}

		for (ItemStack dictStack : OreDictionary.getOres("gemDiamond", false))
		{
			if (CaveUtils.isItemEqual(stack, dictStack))
			{
				return true;
			}
		}

		return false;
	}
}