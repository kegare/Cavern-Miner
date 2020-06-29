package cavern.miner.enchantment;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nullable;

import cavern.miner.util.BlockStateHelper;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class MinerSnapshot implements Comparator<BlockPos>
{
	protected final EnchantmentMiner enchMiner;
	protected final World world;
	protected final BlockPos originPos;
	protected final BlockState originState;
	protected final LivingEntity miner;

	protected final Set<BlockPos> miningTargets = new TreeSet<>(this);

	public MinerSnapshot(EnchantmentMiner ench, World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity)
	{
		this.enchMiner = ench;
		this.world = world;
		this.originPos = pos;
		this.originState = state;
		this.miner = entity;
	}

	public EnchantmentMiner getEnchantmentMiner()
	{
		return enchMiner;
	}

	public World getWorld()
	{
		return world;
	}

	public BlockPos getOriginPos()
	{
		return originPos;
	}

	public BlockState getOriginState()
	{
		return originState;
	}

	@Nullable
	public LivingEntity getMiner()
	{
		return miner;
	}

	public ItemStack getHeldItem()
	{
		return miner == null ? ItemStack.EMPTY : miner.getHeldItemMainhand();
	}

	public int getLevel()
	{
		return Math.max(EnchantmentHelper.getEnchantmentLevel(enchMiner, getHeldItem()), 1);
	}

	public boolean isEmpty()
	{
		return miningTargets.isEmpty();
	}

	public boolean equals(World worldIn, BlockPos pos)
	{
		if (worldIn == null || pos == null)
		{
			return false;
		}

		return world.getDimension().getType() == worldIn.getDimension().getType() && originPos.equals(pos);
	}

	public int getTargetCount()
	{
		return isEmpty() ? 0 : miningTargets.size();
	}

	public Set<BlockPos> getTargets()
	{
		return miningTargets;
	}

	public abstract MinerSnapshot checkForMining();

	public boolean offer(BlockPos pos)
	{
		if (validTarget(pos) && !miningTargets.contains(pos))
		{
			return miningTargets.add(pos);
		}

		return false;
	}

	public boolean validTarget(BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);

		if (state.getBlock().isAir(state, world, pos) || state.getBlockHardness(world, pos) < 0.0F)
		{
			return false;
		}

		return enchMiner.isEffectiveTarget(getHeldItem(), state) || BlockStateHelper.equals(state, originState);
	}

	@Override
	public int compare(BlockPos o1, BlockPos o2)
	{
		int i = Double.compare(o1.distanceSq(originPos), o2.distanceSq(originPos));

		if (i == 0)
		{
			i = o1.compareTo(o2);
		}

		return i;
	}
}