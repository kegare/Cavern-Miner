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
import net.minecraft.world.IBlockReader;

public abstract class MinerSnapshot implements Comparator<BlockPos>
{
	protected final EnchantmentMiner enchMiner;
	protected final IBlockReader reader;
	protected final BlockPos originPos;
	protected final BlockState originState;
	protected final LivingEntity miner;

	protected final Set<BlockPos> miningTargets = new TreeSet<>(this);

	public MinerSnapshot(EnchantmentMiner ench, IBlockReader reader, BlockPos pos, BlockState state, @Nullable LivingEntity entity)
	{
		this.enchMiner = ench;
		this.reader = reader;
		this.originPos = pos;
		this.originState = state;
		this.miner = entity;
	}

	public EnchantmentMiner getEnchantmentMiner()
	{
		return enchMiner;
	}

	public IBlockReader getReader()
	{
		return reader;
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
		BlockState state = reader.getBlockState(pos);

		if (state.getBlock().isAir(state, reader, pos) || state.getBlockHardness(reader, pos) < 0.0F)
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