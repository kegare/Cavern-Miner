package cavern.miner.item;

import java.util.function.Supplier;

import cavern.miner.init.CaveTags;
import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyValue;

public class CaveItemTier implements IItemTier
{
	public static final IItemTier MAGNITE = new CaveItemTier(3, 10, 100.0F, 11.0F, 50, () -> Ingredient.fromTag(CaveTags.Items.INGOTS_MAGNITE));
	public static final IItemTier AQUAMARINE = new CaveItemTier(2, 200, 8.0F, 1.5F, 15, () -> Ingredient.fromTag(CaveTags.Items.GEMS_AQUAMARINE));

	private final int harvestLevel;
	private final int maxUses;
	private final float efficiency;
	private final float attackDamage;
	private final int enchantability;
	private final LazyValue<Ingredient> repairMaterial;

	private CaveItemTier(int harvestLevel, int maxUses, float efficiency, float attackDamage, int enchantability, Supplier<Ingredient> repairMaterial)
	{
		this.harvestLevel = harvestLevel;
		this.maxUses = maxUses;
		this.efficiency = efficiency;
		this.attackDamage = attackDamage;
		this.enchantability = enchantability;
		this.repairMaterial = new LazyValue<>(repairMaterial);
	}

	@Override
	public int getMaxUses()
	{
		return maxUses;
	}

	@Override
	public float getEfficiency()
	{
		return efficiency;
	}

	@Override
	public float getAttackDamage()
	{
		return attackDamage;
	}

	@Override
	public int getHarvestLevel()
	{
		return harvestLevel;
	}

	@Override
	public int getEnchantability()
	{
		return enchantability;
	}

	@Override
	public Ingredient getRepairMaterial()
	{
		return repairMaterial.getValue();
	}
}