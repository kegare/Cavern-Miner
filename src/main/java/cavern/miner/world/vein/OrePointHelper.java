package cavern.miner.world.vein;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;

public class OrePointHelper
{
	private static final Map<OreRegistry.OreEntry, Integer> POINT_CACHE = new HashMap<>();

	public static int getPoint(OreRegistry.OreEntry entry)
	{
		if (entry == OreRegistry.OreEntry.EMPTY)
		{
			return 0;
		}

		return entry.getPoint().orElseGet(() -> POINT_CACHE.computeIfAbsent(entry, OrePointHelper::calcPoint));
	}

	private static int calcPoint(OreRegistry.OreEntry entry)
	{
		if (entry instanceof OreRegistry.BlockEntry)
		{
			return calcBlock(((OreRegistry.BlockEntry)entry).getBlock());
		}

		if (entry instanceof OreRegistry.BlockStateEntry)
		{
			return calcBlockState(((OreRegistry.BlockStateEntry)entry).getBlockState());
		}

		if (entry instanceof OreRegistry.TagEntry)
		{
			return calcBlockTag(((OreRegistry.TagEntry)entry).getTag());
		}

		return 0;
	}

	private static int calcBlock(Block block)
	{
		return calcBlockState(block.getDefaultState());
	}

	private static int calcBlockState(BlockState state)
	{
		Block block = state.getBlock();

		if (block instanceof AirBlock)
		{
			return 0;
		}

		int level = block.getHarvestLevel(state);

		if (level < 0 || block.getHarvestTool(state) != ToolType.PICKAXE)
		{
			return 0;
		}

		int point = level + 1;

		if (level > 0)
		{
			String name = block.getRegistryName().getPath();

			if (name.contains("_"))
			{
				name = name.substring(0, name.lastIndexOf('_'));
			}

			Item pickaxe = ForgeRegistries.ITEMS.getValue(new ResourceLocation(block.getRegistryName().getNamespace(), name + "_pickaxe"));
			double toolRarity = 1.0D;

			if (pickaxe != null)
			{
				ItemStack dummy = new ItemStack(pickaxe);
				int max = pickaxe.getMaxDamage(dummy);
				float destroy = pickaxe.getDestroySpeed(dummy, Blocks.IRON_ORE.getDefaultState());
				int enchant = pickaxe.getItemEnchantability(dummy);
				int harvest = pickaxe.getHarvestLevel(dummy, ToolType.PICKAXE, null, null);

				toolRarity = max * 0.01D + destroy * 0.001D + enchant * 0.01D + harvest * 1.0D;
			}

			if (toolRarity >= 5.0D)
			{
				point += MathHelper.ceil(toolRarity * 0.1D);
			}
		}

		return point;
	}

	private static int calcBlockTag(Tag<Block> tag)
	{
		int point = 0;

		for (Block block : tag.getAllElements())
		{
			int i = calcBlock(block);

			if (point < i)
			{
				point = i;
			}
		}

		return point;
	}
}