package cavern.miner.item;

import cavern.miner.block.BlockCave.EnumType;
import cavern.miner.core.CavernMod;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockCave extends ItemBlock
{
	public ItemBlockCave(Block block)
	{
		super(block);
		this.setRegistryName(block.getRegistryName());
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage)
	{
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return "tile." + EnumType.byItemStack(stack).getTranslationKey();
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		String name = super.getItemStackDisplayName(stack);

		if (EnumType.byItemStack(stack) == EnumType.FISSURED_STONE)
		{
			return ("" + CavernMod.proxy.translateFormat("tile.fissured.name", name)).trim();
		}

		return name;
	}
}