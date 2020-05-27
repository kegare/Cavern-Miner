package cavern.miner.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface CaveOre
{
	int getMiningPoint(IBlockState state);

	default int getMiningPoint(World world, BlockPos pos, IBlockState state, EntityPlayer player, int fortune)
	{
		return getMiningPoint(state);
	}
}