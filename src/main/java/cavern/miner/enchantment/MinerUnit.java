package cavern.miner.enchantment;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

public class MinerUnit
{
	private final PlayerEntity player;

	private boolean breaking, captureDrops, captureExperiences;

	private Map<BlockPos, NonNullList<ItemStack>> capturedDrops;
	private Map<BlockPos, Integer> capturedExperiences;

	public MinerUnit(PlayerEntity player)
	{
		this.player = player;
	}

	@Nullable
	public MinerSnapshot getSnapshot(EnchantmentMiner miner, BlockPos pos, BlockState state)
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
			capturedDrops = new HashMap<>();

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
			capturedExperiences = new HashMap<>();

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
}