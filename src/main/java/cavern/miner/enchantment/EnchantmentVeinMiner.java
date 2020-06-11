package cavern.miner.enchantment;

import cavern.miner.vein.OreRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

public class EnchantmentVeinMiner extends EnchantmentMiner
{
	public EnchantmentVeinMiner()
	{
		super(Enchantment.Rarity.UNCOMMON);
	}

	@Override
	public int getMaxLevel()
	{
		return 4;
	}

	@Override
	public boolean isEffectiveTarget(ItemStack stack, BlockState state)
	{
		if (!super.isEffectiveTarget(stack, state))
		{
			return false;
		}

		Block block = state.getBlock();

		if (block instanceof OreBlock || block instanceof RedstoneOreBlock || block.isIn(Tags.Blocks.ORES))
		{
			return true;
		}

		return OreRegistry.getEntry(state).getPoint() != null;
	}

	@Override
	public MinerSnapshot createSnapshot(World world, BlockPos pos, BlockState state, LivingEntity entity)
	{
		return new VeinMinerSnapshot(this, world, pos, state, entity);
	}
}