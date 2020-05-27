package cavern.miner.item;

import cavern.miner.core.CavernMod;
import net.minecraft.item.ItemSpade;

public class ItemCaveShovel extends ItemSpade
{
	public ItemCaveShovel(ToolMaterial material, String name)
	{
		super(material);
		this.setUnlocalizedName(name);
		this.setCreativeTab(CavernMod.TAB_CAVERN);
	}
}