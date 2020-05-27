package cavern.miner.item;

import cavern.miner.core.CavernMod;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class ItemCaveArmor extends ItemArmor
{
	private final String renderName;

	public ItemCaveArmor(ArmorMaterial material, String name, String renderName, EntityEquipmentSlot slot)
	{
		super(material, 2, slot);
		this.setUnlocalizedName(name);
		this.setCreativeTab(CavernMod.TAB_CAVERN);
		this.renderName = renderName;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type)
	{
		return String.format("cavern:textures/models/armor/%s_layer_%d.png", renderName, slot == EntityEquipmentSlot.LEGS ? 2 : 1);
	}
}