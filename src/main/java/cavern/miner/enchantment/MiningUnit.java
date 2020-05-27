package cavern.miner.enchantment;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.collect.Maps;

import cavern.miner.capability.CaveCapabilities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

public class MiningUnit
{
	private final EntityPlayer player;

	private boolean breaking, captureDrops, captureExperiences;

	private Map<BlockPos, NonNullList<ItemStack>> capturedDrops;
	private Map<BlockPos, Integer> capturedExperiences;

	public MiningUnit(EntityPlayer player)
	{
		this.player = player;
	}

	@Nullable
	public MiningSnapshot getSnapshot(EnchantmentMiner miner, BlockPos pos, IBlockState state)
	{
		return miner.createSnapshot(player.world, pos, state, player).checkForMining();
	}

	public boolean isBreaking()
	{
		return breaking;
	}

	public void setBreaking(boolean state)
	{
		breaking = state;
	}

	public boolean getCaptureDrops()
	{
		return captureDrops;
	}

	@Nullable
	public Map<BlockPos, NonNullList<ItemStack>> captureDrops(boolean value)
	{
		captureDrops = value;

		if (value)
		{
			capturedDrops = Maps.newHashMap();

			return null;
		}

		return capturedDrops;
	}

	public boolean addDrops(BlockPos pos, NonNullList<ItemStack> drops)
	{
		if (!captureDrops || capturedDrops == null || pos == null || drops == null || drops.isEmpty())
		{
			return false;
		}

		capturedDrops.put(pos, drops);

		return true;
	}

	public boolean getCaptureExperiences()
	{
		return captureExperiences;
	}

	@Nullable
	public Map<BlockPos, Integer> captureExperiences(boolean value)
	{
		captureExperiences = value;

		if (value)
		{
			capturedExperiences = Maps.newHashMap();

			return null;
		}

		return capturedExperiences;
	}

	public boolean addExperience(BlockPos pos, int experience)
	{
		if (!captureExperiences || capturedExperiences == null || pos == null || experience <= 0)
		{
			return false;
		}

		capturedExperiences.put(pos, experience);

		return true;
	}

	public static MiningUnit get(EntityPlayer player)
	{
		return ObjectUtils.defaultIfNull(CaveCapabilities.getCapability(player, CaveCapabilities.MINING_UNIT), new MiningUnit(player));
	}
}