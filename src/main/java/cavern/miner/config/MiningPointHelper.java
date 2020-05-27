package cavern.miner.config;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import cavern.miner.api.block.CaveOre;
import cavern.miner.handler.CaveEventHooks;
import cavern.miner.util.BlockMeta;
import cavern.miner.util.CaveLog;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

public final class MiningPointHelper
{
	private static final Map<BlockMeta, Integer> POINTS = Maps.newHashMap();

	public static ImmutableMap<BlockMeta, Integer> getEntries()
	{
		return ImmutableMap.copyOf(POINTS);
	}

	@SuppressWarnings("deprecation")
	public static void setupPoints()
	{
		Set<String> oreNames = Arrays.stream(OreDictionary.getOreNames())
			.filter(name -> name.startsWith("ore") && name.length() > 3 && Character.isUpperCase(name.charAt(3)))
			.sorted().collect(Collectors.toSet());

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
					int harvestLevel = block.getHarvestLevel(state);

					if (harvestLevel < 0 || !block.getHarvestTool(state).equals("pickaxe"))
					{
						continue;
					}

					int amount = harvestLevel + 1;

					if (harvestLevel > 0)
					{
						Item dropItem = block.getItemDropped(state, CaveEventHooks.RANDOM, 0);

						if (dropItem instanceof ItemBlock && ((ItemBlock)dropItem).getBlock() == block)
						{
							amount = 1;
						}
						else
						{
							String variant = name.substring(3).toLowerCase();
							Item pickaxe = ForgeRegistries.ITEMS.getValue(new ResourceLocation(block.getRegistryName().getResourceDomain(), variant + "_pickaxe"));
							double toolRarity = 1.0D;

							if (pickaxe != null)
							{
								ItemStack dummy = new ItemStack(pickaxe);
								int max = pickaxe.getMaxDamage(dummy);
								float destroy = pickaxe.getDestroySpeed(dummy, Blocks.IRON_ORE.getDefaultState());
								int enchant = pickaxe.getItemEnchantability(dummy);
								int harvest = pickaxe.getHarvestLevel(dummy, "pickaxe", null, null);

								toolRarity = max * 0.01D + destroy * 0.001D + enchant * 0.01D + harvest * 1.0D;
							}

							if (toolRarity >= 5.0D)
							{
								amount += MathHelper.ceil(toolRarity * 0.1D);
							}
						}
					}

					POINTS.put(new BlockMeta(state), amount);
				}
				catch (Exception e)
				{
					CaveLog.log(Level.ERROR, "An error occurred while setup. Skip: {%s} %s", name, stack.toString());
				}
			}
		}

		for (BlockMeta blockMeta : EntryListHelper.getBlockEntiries())
		{
			Block block = blockMeta.getBlock();

			if (block instanceof BlockOre || block instanceof BlockRedstoneOre)
			{
				POINTS.putIfAbsent(blockMeta, 1);
			}

			if (block instanceof CaveOre)
			{
				int point = ((CaveOre)block).getMiningPoint(blockMeta.getBlockState());

				if (point > 0)
				{
					POINTS.put(blockMeta, point);
				}
			}
		}

		POINTS.put(new BlockMeta(Blocks.REDSTONE_ORE, 0), 2);
		POINTS.put(new BlockMeta(Blocks.LIT_REDSTONE_ORE, 0), 2);
	}

	public static int getPoint(IBlockState state)
	{
		return POINTS.getOrDefault(new BlockMeta(state), 0);
	}
}