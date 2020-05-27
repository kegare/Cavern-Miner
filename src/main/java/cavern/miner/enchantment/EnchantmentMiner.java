package cavern.miner.enchantment;

import javax.annotation.Nullable;

import cavern.miner.config.property.ConfigBlocks;
import cavern.miner.util.CaveUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class EnchantmentMiner extends Enchantment
{
	protected EnchantmentMiner(String name, Enchantment.Rarity rarity)
	{
		super(rarity, EnumEnchantmentType.DIGGER, new EntityEquipmentSlot[] {EntityEquipmentSlot.MAINHAND});
		this.setName(name);
	}

	@Override
	public int getMinEnchantability(int enchantmentLevel)
	{
		return 15;
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel)
	{
		return super.getMinEnchantability(enchantmentLevel) + 50;
	}

	@Override
	public int getMaxLevel()
	{
		return 3;
	}

	@Override
	public boolean canApplyTogether(Enchantment ench)
	{
		return super.canApplyTogether(ench) && !(ench instanceof EnchantmentMiner);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack)
	{
		return super.canApplyAtEnchantingTable(stack) && CaveUtils.isPickaxe(stack);
	}

	@Nullable
	public ConfigBlocks getTargetBlocks()
	{
		return null;
	}

	public boolean isEffectiveTarget(ItemStack stack, IBlockState state)
	{
		ConfigBlocks targets = getTargetBlocks();

		if (targets != null && !targets.isEmpty())
		{
			return targets.hasBlockState(state);
		}

		return !stack.isEmpty() && stack.canHarvestBlock(state);
	}

	public abstract MiningSnapshot createSnapshot(World world, BlockPos pos, IBlockState state, EntityLivingBase entity);
}