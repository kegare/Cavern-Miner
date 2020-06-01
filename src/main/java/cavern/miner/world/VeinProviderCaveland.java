package cavern.miner.world;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import cavern.miner.config.CavelandConfig;
import cavern.miner.config.manager.CaveVein;
import cavern.miner.config.manager.CaveVeinManager;
import cavern.miner.core.CavernMod;
import cavern.miner.util.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.oredict.OreDictionary;

public class VeinProviderCaveland extends VeinProvider
{
	@Override
	public CaveVeinManager getVeinManager()
	{
		return CavelandConfig.autoVeins ? null : CavelandConfig.VEINS;
	}

	@Override
	public String[] getBlacklist()
	{
		return CavelandConfig.autoVeinBlacklist;
	}

	@SuppressWarnings("deprecation")
	@Override
	public NonNullList<Pair<String, BlockMeta>> getStoneBlocks()
	{
		if (stoneBlocks != null)
		{
			return stoneBlocks;
		}

		NonNullList<Pair<String, BlockMeta>> list = NonNullList.create();
		String[] others = {"dirt", "gravel", "sand"};

		Arrays.stream(OreDictionary.getOreNames())
			.filter(name -> getBlacklist() != null && !ArrayUtils.contains(getBlacklist(), name))
			.filter(name -> name.startsWith("stone") && name.length() > 5 && Character.isUpperCase(name.charAt(5)) || ArrayUtils.contains(others, name))
			.forEach(name ->
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

						list.add(Pair.of(name, new BlockMeta(state)));
					}
					catch (Exception e)
					{
						CavernMod.LOG.warn("An error occurred while setup. Skip: {} | {}", name, stack.toString(), e);
					}
				}
			}
		);

		stoneBlocks = list;

		return list;
	}

	@Override
	protected void getSubVeins(World world, int chunkX, int chunkZ, List<CaveVein> list)
	{
		NonNullList<Pair<String, BlockMeta>> stones = getStoneBlocks();
		int max = world.provider.getAverageGroundLevel() - 4;

		for (Pair<String, BlockMeta> stone : stones)
		{
			int weight = MathHelper.getInt(rand, 3, 8);
			int size = MathHelper.getInt(rand, 5, 10);

			list.add(new CaveVein(stone.getRight(), weight, size, 1, max));
		}
	}

	@Override
	protected CaveVein createVein(World world, BlockMeta blockMeta, Rarity rarity)
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

		return new CaveVein(blockMeta, weight, size, 1, world.provider.getAverageGroundLevel() - 4).setBiomes(biome);
	}
}