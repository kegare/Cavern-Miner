package cavern.miner.core;

import cavern.miner.block.CaveBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeTabCavern extends CreativeTabs
{
	public CreativeTabCavern()
	{
		super(CavernMod.MODID);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ItemStack getTabIconItem()
	{
		return new ItemStack(CaveBlocks.CAVERN_PORTAL);
	}
}