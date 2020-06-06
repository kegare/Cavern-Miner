package cavern.miner.block;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.Tags;

public class RandomiteOreBlock extends OreBlock
{
	private static final List<Tag<Item>> DROP_ITEM_TAGS = Lists.newArrayList();

	static
	{
		DROP_ITEM_TAGS.add(Tags.Items.INGOTS);
		DROP_ITEM_TAGS.add(Tags.Items.NUGGETS);
		DROP_ITEM_TAGS.add(Tags.Items.GEMS);
		DROP_ITEM_TAGS.add(Tags.Items.DUSTS);
		DROP_ITEM_TAGS.add(Tags.Items.RODS);
		DROP_ITEM_TAGS.add(Tags.Items.ENDER_PEARLS);
		DROP_ITEM_TAGS.add(Tags.Items.BONES);
		DROP_ITEM_TAGS.add(Tags.Items.ARROWS);
		DROP_ITEM_TAGS.add(Tags.Items.GUNPOWDER);
		DROP_ITEM_TAGS.add(Tags.Items.STRING);
		DROP_ITEM_TAGS.add(Tags.Items.SEEDS);
		DROP_ITEM_TAGS.add(Tags.Items.CROPS);
		DROP_ITEM_TAGS.add(Tags.Items.DYES);
	}

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
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
	{
		List<ItemStack> drops = Lists.newArrayList();
		ItemStack stack = builder.get(LootParameters.TOOL);

		if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0)
		{
			drops.add(new ItemStack(this));

			return drops;
		}

		int fortune = stack == null ? 0 : EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);

		for (int i = 0; i <= fortune; ++i)
		{
			drops.add(new ItemStack(getRandomDropItem(RANDOM)));
		}

		return drops;
	}

	protected Item getRandomDropItem(Random rand)
	{
		return DROP_ITEM_TAGS.get(rand.nextInt(DROP_ITEM_TAGS.size())).getRandomElement(rand);
	}
}