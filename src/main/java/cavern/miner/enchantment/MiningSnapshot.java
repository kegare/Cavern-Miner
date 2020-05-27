package cavern.miner.enchantment;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import cavern.miner.util.CaveUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class MiningSnapshot implements Comparator<BlockPos>
{
	protected final EnchantmentMiner enchMiner;
	protected final World world;
	protected final BlockPos originPos;
	protected final IBlockState originState;

	protected EntityLivingBase miner;

	protected Set<BlockPos> miningTargets;

	public MiningSnapshot(EnchantmentMiner ench, World world, BlockPos pos, IBlockState state, EntityLivingBase entity)
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

	public IBlockState getOriginState()
	{
		return originState;
	}

	@Nullable
	public EntityLivingBase getMiner()
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

	public boolean isChecked()
	{
		return miningTargets != null;
	}

	public boolean isEmpty()
	{
		return miningTargets == null || miningTargets.isEmpty();
	}

	public boolean equals(World worldIn, BlockPos pos)
	{
		if (worldIn == null || pos == null)
		{
			return false;
		}

		return world.provider.getDimensionType() == worldIn.provider.getDimensionType() && originPos.equals(pos);
	}

	public int getTargetCount()
	{
		return isEmpty() ? 0 : miningTargets.size();
	}

	public Set<BlockPos> getTargets()
	{
		return ObjectUtils.defaultIfNull(miningTargets, Collections.emptySet());
	}

	public abstract MiningSnapshot checkForMining();

	public boolean offer(BlockPos pos)
	{
		if (miningTargets == null)
		{
			return false;
		}

		if (validTarget(pos) && !miningTargets.contains(pos))
		{
			return miningTargets.add(pos);
		}

		return false;
	}

	public boolean validTarget(BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);

		if (state.getBlock().isAir(state, world, pos) || state.getBlockHardness(world, pos) < 0.0F)
		{
			return false;
		}

		return enchMiner.isEffectiveTarget(getHeldItem(), state) || CaveUtils.isBlockEqual(state, originState);
	}

	@Override
	public int compare(BlockPos o1, BlockPos o2)
	{
		int i = CaveUtils.compareWithNull(o1, o2);

		if (i == 0 && o1 != null && o2 != null)
		{
			i = Double.compare(o1.distanceSq(originPos), o2.distanceSq(originPos));

			if (i == 0)
			{
				i = o1.compareTo(o2);
			}
		}

		return i;
	}
}