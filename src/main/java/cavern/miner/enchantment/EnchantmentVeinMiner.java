package cavern.miner.enchantment;

import cavern.miner.config.MiningConfig;
import cavern.miner.config.property.ConfigBlocks;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnchantmentVeinMiner extends EnchantmentMiner
{
	protected EnchantmentVeinMiner()
	{
		super("veinMiner", Enchantment.Rarity.UNCOMMON);
	}

	@Override
	public int getMaxLevel()
	{
		return 4;
	}

	@Override
	public ConfigBlocks getTargetBlocks()
	{
		return MiningConfig.veinTargetBlocks;
	}

	@Override
	public boolean isEffectiveTarget(ItemStack stack, IBlockState state)
	{
		ConfigBlocks targets = getTargetBlocks();

		if (targets != null && !targets.isEmpty())
		{
			return targets.hasBlockState(state);
		}

		return state.getBlock() instanceof BlockOre || state.getBlock() instanceof BlockRedstoneOre || MiningConfig.miningPoints.getPoint(state) > 0;
	}

	@Override
	public MiningSnapshot createSnapshot(World world, BlockPos pos, IBlockState state, EntityLivingBase entity)
	{
		return new VeinMiningSnapshot(this, world, pos, state, entity);
	}
}