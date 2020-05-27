package cavern.miner.item;

import cavern.miner.core.CavernMod;
import net.minecraft.item.ItemAxe;

public class ItemCaveAxe extends ItemAxe
{
	public ItemCaveAxe(ToolMaterial material, float damage, float speed, String name)
	{
		super(material, damage, speed);
		this.setUnlocalizedName(name);
		this.setCreativeTab(CavernMod.TAB_CAVERN);
	}
}