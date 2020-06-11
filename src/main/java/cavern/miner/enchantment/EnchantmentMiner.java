package cavern.miner.enchantment;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public abstract class EnchantmentMiner extends Enchantment
{
	protected EnchantmentMiner(Enchantment.Rarity rarity)
	{
		super(rarity, EnchantmentType.DIGGER, new EquipmentSlotType[] {EquipmentSlotType.MAINHAND});
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
		return super.canApplyAtEnchantingTable(stack) && stack.getToolTypes().contains(ToolType.PICKAXE);
	}

	public boolean isEffectiveTarget(ItemStack stack, BlockState state)
	{
		return !stack.isEmpty() && stack.canHarvestBlock(state);
	}

	public abstract MinerSnapshot createSnapshot(World world, BlockPos pos, BlockState state, LivingEntity entity);
}