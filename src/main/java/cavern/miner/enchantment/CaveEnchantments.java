package cavern.miner.enchantment;

import java.util.Map.Entry;

import javax.annotation.Nullable;

import cavern.miner.util.CaveUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;

public final class CaveEnchantments
{
	public static final EnchantmentMiner VEIN_MINER = new EnchantmentVeinMiner();
	public static final EnchantmentMiner AREA_MINER = new EnchantmentAreaMiner();

	public static void registerEnchantments(IForgeRegistry<Enchantment> registry)
	{
		registry.register(VEIN_MINER.setRegistryName(CaveUtils.getKey("vein_miner")));
		registry.register(AREA_MINER.setRegistryName(CaveUtils.getKey("area_miner")));
	}

	@Nullable
	public static EnchantmentMiner getMinerEnchantment(ItemStack stack)
	{
		for (Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet())
		{
			if (entry.getKey() instanceof EnchantmentMiner && entry.getValue() > 0)
			{
				return (EnchantmentMiner)entry.getKey();
			}
		}

		return null;
	}
}