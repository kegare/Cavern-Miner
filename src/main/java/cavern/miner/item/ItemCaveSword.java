package cavern.miner.item;

import cavern.miner.core.CavernMod;
import net.minecraft.item.ItemSword;

public class ItemCaveSword extends ItemSword
{
	protected final ToolMaterial toolMaterial;

	public ItemCaveSword(ToolMaterial material, String name)
	{
		super(material);
		this.toolMaterial = material;
		this.setUnlocalizedName(name);
		this.setCreativeTab(CavernMod.TAB_CAVERN);
	}

	public ToolMaterial getToolMaterial()
	{
		return toolMaterial;
	}
}