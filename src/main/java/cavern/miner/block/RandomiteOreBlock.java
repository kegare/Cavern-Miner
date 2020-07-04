package cavern.miner.block;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import cavern.miner.config.GeneralConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;

public class RandomiteOreBlock extends CaveOreBlock
{
	public RandomiteOreBlock(Block.Properties properties)
	{
		super(properties);
	}

	@Override
	protected int getExperience(Random rand)
	{
		return MathHelper.nextInt(rand, 1, 3);
	}

	@Override
	public int getPoint(BlockState state, IWorldReader reader, BlockPos pos, int point)
	{
		return MathHelper.nextInt(RANDOM, point - 1, point + 1);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
	{
		List<ItemStack> drops = Lists.newArrayList();
		ItemStack stack = builder.get(LootParameters.TOOL);

		if (stack != null && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0)
		{
			drops.add(new ItemStack(this));

			return drops;
		}

		int fortune = stack == null ? 0 : EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);

		for (int i = 0; i <= fortune; ++i)
		{
			ItemStack drop = GeneralConfig.INSTANCE.randomiteDrops.getRandomDrop(RANDOM).getDropItem();

			if (!drop.isEmpty())
			{
				drops.add(drop);
			}
		}

		return drops;
	}
}