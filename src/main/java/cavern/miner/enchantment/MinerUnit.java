package cavern.miner.enchantment;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class MinerUnit
{
	private final PlayerEntity player;

	private boolean breaking;

	public MinerUnit(PlayerEntity player)
	{
		this.player = player;
	}

	@Nullable
	public MinerSnapshot getSnapshot(EnchantmentMiner miner, BlockPos pos, BlockState state)
	{
		return player == null ? null : miner.createSnapshot(player.world, pos, state, player).checkForMining();
	}

	public boolean isBreaking()
	{
		return breaking;
	}

	public void setBreaking(boolean state)
	{
		breaking = state;
	}
}