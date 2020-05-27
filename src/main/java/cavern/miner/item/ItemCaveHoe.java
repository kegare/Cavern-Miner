package cavern.miner.item;

import cavern.miner.core.CavernMod;
import net.minecraft.item.ItemHoe;

public class ItemCaveHoe extends ItemHoe
{
	public ItemCaveHoe(ToolMaterial material, String name)
	{
		super(material);
		this.setUnlocalizedName(name);
		this.setCreativeTab(CavernMod.TAB_CAVERN);
	}
}