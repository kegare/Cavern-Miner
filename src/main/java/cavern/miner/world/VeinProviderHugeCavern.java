package cavern.miner.world;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Lists;

import cavern.miner.config.HugeCavernConfig;
import cavern.miner.config.manager.CaveVein;
import cavern.miner.config.manager.CaveVeinManager;
import cavern.miner.config.property.ConfigBlocks;
import cavern.miner.util.BlockMeta;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

public class VeinProviderHugeCavern extends VeinProviderCavern
{
	public VeinProviderHugeCavern(World world, Supplier<CaveVeinManager> manager)
	{
		super(world, manager);
	}

	@Override
	public ConfigBlocks getExemptBlocks()
	{
		return HugeCavernConfig.autoVeinBlacklist;
	}

	@Override
	protected List<CaveVein> createVeins(String name, BlockMeta blockMeta, Rarity rarity)
	{
		List<CaveVein> list = Lists.newArrayList();
		int weight = 15;
		int size = 5;
		int min = 1;
		int max = world.getActualHeight() - 1;
		Object[] biome = null;

		switch (rarity)
		{
			case COMMON:
				weight = MathHelper.getInt(rand, 12, 15);
				size = MathHelper.getInt(rand, 15, 20);
				break;
			case UNCOMMON:
				weight = MathHelper.getInt(rand, 10, 12);
				size = MathHelper.getInt(rand, 8, 12);
				break;
			case RARE:
				weight = MathHelper.getInt(rand, 7, 10);
				size = MathHelper.getInt(rand, 5, 8);
				break;
			case EPIC:
				weight = MathHelper.getInt(rand, 1, 2);
				size = MathHelper.getInt(rand, 3, 5);
				max = 50;
				break;
			case EMERALD:
				weight = MathHelper.getInt(rand, 2, 3);
				size = MathHelper.getInt(rand, 3, 5);
				min = 50;
				biome = ArrayUtils.toArray(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS);
				break;
			case DIAMOND:
				weight = MathHelper.getInt(rand, 0, 2);
				size = MathHelper.getInt(rand, 2, 4);
				max = 30;
				break;
			case AQUA:
				weight = MathHelper.getInt(rand, 6, 8);
				size = MathHelper.getInt(rand, 3, 6);
				max = 70;
				biome = ArrayUtils.toArray(BiomeDictionary.Type.COLD, BiomeDictionary.Type.WET, BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.RIVER);
				break;
			case RANDOMITE:
				weight = MathHelper.getInt(rand, 4, 8);
				size = MathHelper.getInt(rand, 2, 4);
				min = 20;
				break;
		}

		if (weight <= 0 || size <= 0)
		{
			return null;
		}

		list.add(new CaveVein(blockMeta, weight, size, min, max, biome));

		if (rarity == Rarity.COMMON || rarity == Rarity.UNCOMMON || rarity == Rarity.RANDOMITE)
		{
			list.add(new CaveVein(blockMeta, weight, size / 2, max / 3, max, biome));
		}

		if (name.startsWith("ore") && rarity == Rarity.COMMON)
		{
			list.add(new CaveVein(blockMeta, weight / 2, size, min, 40, biome));
		}

		return list;
	}
}