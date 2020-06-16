package cavern.miner.enchantment;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnchantmentAreaMiner extends EnchantmentMiner
{
	public EnchantmentAreaMiner()
	{
		super(Enchantment.Rarity.UNCOMMON);
	}

	@Override
	public int getMaxLevel()
	{
		return 2;
	}

	@Override
	public MinerSnapshot createSnapshot(World world, BlockPos pos, BlockState state, LivingEntity entity)
	{
		return new AreaMinerSnapshot(this, world, pos, state, entity);
	}
}