package cavern.miner.enchantment;

import cavern.miner.config.MiningConfig;
import cavern.miner.config.property.ConfigBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnchantmentAreaMiner extends EnchantmentMiner
{
	protected EnchantmentAreaMiner()
	{
		super("areaMiner", Enchantment.Rarity.UNCOMMON);
	}

	@Override
	public int getMaxLevel()
	{
		return 2;
	}

	@Override
	public ConfigBlocks getTargetBlocks()
	{
		return MiningConfig.areaTargetBlocks;
	}

	@Override
	public MiningSnapshot createSnapshot(World world, BlockPos pos, IBlockState state, EntityLivingBase entity)
	{
		return new AreaMiningSnapshot(this, world, pos, state, entity);
	}
}