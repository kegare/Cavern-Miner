package cavern.miner.enchantment;

import cavern.miner.util.BlockStateTagList;
import cavern.miner.vein.OrePointHelper;
import cavern.miner.vein.OreRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
	public BlockStateTagList getTargetBlocks()
	{
		return null;
	}

	@Override
	public boolean isEffectiveTarget(ItemStack stack, BlockState state)
	{
		BlockStateTagList targets = getTargetBlocks();

		if (targets != null && !targets.isEmpty())
		{
			return targets.contains(state);
		}

		return state.getBlock() instanceof OreBlock || state.getBlock() instanceof RedstoneOreBlock || OrePointHelper.getPoint(OreRegistry.getEntry(state)) > 0;
	}

	@Override
	public MinerSnapshot createSnapshot(World world, BlockPos pos, BlockState state, LivingEntity entity)
	{
		return new VeinMinerSnapshot(this, world, pos, state, entity);
	}
}