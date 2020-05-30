package cavern.miner.config;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import cavern.miner.core.CavernMod;
import cavern.miner.util.BlockMeta;
import cavern.miner.util.ItemMeta;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class EntryListHelper
{
	protected static final List<BlockMeta> BLOCKS = Lists.newArrayList();
	protected static final List<ItemMeta> ITEMS = Lists.newArrayList();

	@SuppressWarnings("deprecation")
	public static void setupBlocks()
	{
		NonNullList<ItemStack> subList = NonNullList.create();

		for (Block block : ForgeRegistries.BLOCKS.getValuesCollection())
		{
			try
			{
				if (block instanceof BlockAir || block instanceof ITileEntityProvider)
				{
					continue;
				}

				Item item = Item.getItemFromBlock(block);

				if (item == Items.AIR)
				{
					continue;
				}

				if (block != Block.getBlockFromItem(item))
				{
					continue;
				}

				subList.clear();

				block.getSubBlocks(CreativeTabs.SEARCH, subList);

				for (ItemStack stack : subList)
				{
					if (stack.isEmpty() || stack.getItem() != item)
					{
						continue;
					}

					int meta = stack.getItemDamage();
					IBlockState state = block.getStateFromMeta(meta);

					if (meta != block.getMetaFromState(state))
					{
						continue;
					}

					BlockMeta blockMeta = new BlockMeta(block, meta);

					if (!BLOCKS.contains(blockMeta))
					{
						BLOCKS.add(blockMeta);
					}
				}
			}
			catch (Exception e)
			{
				CavernMod.LOG.warn("An error occurred while setup. Skip: {}", block.toString(), e);
			}
		}
	}

	public static void setupItems()
	{
		NonNullList<ItemStack> subList = NonNullList.create();

		for (Item item : ForgeRegistries.ITEMS.getValuesCollection())
		{
			try
			{
				if (item == Items.AIR)
				{
					continue;
				}

				subList.clear();

				item.getSubItems(CreativeTabs.SEARCH, subList);

				for (ItemStack stack : subList)
				{
					ItemMeta itemMeta = new ItemMeta(stack);

					if (!ITEMS.contains(itemMeta))
					{
						ITEMS.add(itemMeta);
					}
				}
			}
			catch (Exception e)
			{
				CavernMod.LOG.warn("An error occurred while setup. Skip: {}", item.toString(), e);
			}
		}
	}

	public static ImmutableList<BlockMeta> getBlockEntries()
	{
		return ImmutableList.copyOf(BLOCKS);
	}

	public static ImmutableList<ItemMeta> getItemEntries()
	{
		return ImmutableList.copyOf(ITEMS);
	}
}