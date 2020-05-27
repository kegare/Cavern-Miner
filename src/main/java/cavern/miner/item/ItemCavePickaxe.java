package cavern.miner.item;

import cavern.miner.core.CavernMod;
import net.minecraft.item.ItemPickaxe;

public class ItemCavePickaxe extends ItemPickaxe
{
	public ItemCavePickaxe(ToolMaterial material, String name)
	{
		super(material);
		this.setUnlocalizedName(name);
		this.setCreativeTab(CavernMod.TAB_CAVERN);
	}
}