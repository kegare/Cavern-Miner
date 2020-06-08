package cavern.miner.init;

import java.util.Map.Entry;

import javax.annotation.Nullable;

import cavern.miner.enchantment.EnchantmentAreaMiner;
import cavern.miner.enchantment.EnchantmentMiner;
import cavern.miner.enchantment.EnchantmentVeinMiner;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class CaveEnchantments
{
	public static final DeferredRegister<Enchantment> REGISTRY = new DeferredRegister<>(ForgeRegistries.ENCHANTMENTS, "cavern");

	public static final RegistryObject<EnchantmentMiner> VEIN_MINER = REGISTRY.register("vein_miner", EnchantmentVeinMiner::new);
	public static final RegistryObject<EnchantmentMiner> AREA_MINER = REGISTRY.register("area_miner", EnchantmentAreaMiner::new);

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