package cavern.miner.block;

import cavern.miner.config.CavelandConfig;
import cavern.miner.world.CaveDimensions;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.DimensionType;

public class BlockCavelandPortal extends BlockCavernPortal
{
	public BlockCavelandPortal()
	{
		super();
		this.setUnlocalizedName("portal.caveland");
	}

	@Override
	public DimensionType getDimension()
	{
		return CaveDimensions.CAVELAND;
	}

	@Override
	public boolean isTriggerItem(ItemStack stack)
	{
		if (!CavelandConfig.triggerItems.isEmpty())
		{
			return CavelandConfig.triggerItems.hasItemStack(stack);
		}

		return !stack.isEmpty() && stack.getItem() == Items.GOLDEN_APPLE;
	}
}