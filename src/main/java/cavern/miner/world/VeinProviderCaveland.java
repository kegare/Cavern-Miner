package cavern.miner.world;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;

import cavern.miner.config.CavelandConfig;
import cavern.miner.config.manager.CaveVein;
import cavern.miner.config.manager.CaveVeinManager;
import cavern.miner.config.property.ConfigBlocks;
import cavern.miner.util.BlockMeta;
import cavern.miner.util.CaveLog;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.oredict.OreDictionary;

public class VeinProviderCaveland extends VeinProviderCavern
{
	public VeinProviderCaveland(World world, Supplier<CaveVeinManager> manager)
	{
		super(world, manager);
	}

	@Override
	public ConfigBlocks getExemptBlocks()
	{
		return CavelandConfig.autoVeinBlacklist;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void getSubVeins(List<CaveVein> list)
	{
		List<String> oreNames = Arrays.stream(OreDictionary.getOreNames())
			.filter(name -> name.startsWith("stone") && name.length() > 5).sorted().collect(Collectors.toList());

		oreNames.add("dirt");
		oreNames.add("gravel");
		oreNames.add("sand");

		int max = world.provider.getAverageGroundLevel() - 4;

		for (String name : oreNames)
		{
			for (ItemStack stack : OreDictionary.getOres(name, false))
			{
				try
				{
					if (stack.isEmpty() || stack.getItem() == Items.AIR || !(stack.getItem() instanceof ItemBlock))
					{
						continue;
					}

					Block block = ((ItemBlock)stack.getItem()).getBlock();

					if (block == null || block instanceof BlockAir)
					{
						continue;
					}

					IBlockState state = block.getStateFromMeta(stack.getItemDamage());

					if (getExemptBlocks() != null && getExemptBlocks().hasBlockState(state))
					{
						continue;
					}

					int weight = MathHelper.getInt(rand, 3, 8);
					int size = MathHelper.getInt(rand, 5, 10);

					list.add(new CaveVein(new BlockMeta(state), weight, size, 1, max));
				}
				catch (Exception e)
				{
					CaveLog.log(Level.ERROR, "An error occurred while setup. Skip: {%s} %s", name, stack.toString());
				}
			}
		}
	}

	@Override
	protected CaveVein createVein(String name, BlockMeta blockMeta, Rarity rarity)
	{
		int weight = 5;
		int size = 5;
		Object[] biome = null;

		switch (rarity)
		{
			case COMMON:
				weight = MathHelper.getInt(rand, 4, 7);
				size = MathHelper.getInt(rand, 3, 5);
				break;
			case UNCOMMON:
				weight = MathHelper.getInt(rand, 1, 3);
				size = MathHelper.getInt(rand, 3, 5);
				break;
			case RARE:
				weight = MathHelper.getInt(rand, 0, 2);
				size = MathHelper.getInt(rand, 0, 3);
				break;
			case EPIC:
				weight = MathHelper.getInt(rand, 0, 1);
				size = MathHelper.getInt(rand, 1, 2);
				break;
			case EMERALD:
				weight = MathHelper.getInt(rand, 0, 3);
				size = MathHelper.getInt(rand, 1, 3);
				biome = ArrayUtils.toArray(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS);
				break;
			case DIAMOND:
				weight = MathHelper.getInt(rand, 0, 1);
				size = MathHelper.getInt(rand, 1, 2);
				break;
			case AQUA:
				weight = MathHelper.getInt(rand, 1, 2);
				size = MathHelper.getInt(rand, 2, 5);
				biome = ArrayUtils.toArray(BiomeDictionary.Type.COLD, BiomeDictionary.Type.WET, BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.RIVER);
				break;
			case RANDOMITE:
				weight = MathHelper.getInt(rand, 0, 1);
				size = MathHelper.getInt(rand, 2, 5);
				break;
		}

		if (weight <= 0 || size <= 0)
		{
			return null;
		}

		int max = world.provider.getAverageGroundLevel() - 4;

		if (biome == null)
		{
			return new CaveVein(blockMeta, weight, size, 1, max);
		}

		return new CaveVein(blockMeta, weight, size, 1, max, biome);
	}
}