package cavern.miner.world.gen;

import cavern.miner.config.dimension.CavernConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.GenerationSettings;

public class CavernGenSettings extends GenerationSettings
{
	protected BlockState groundTopBlock = Blocks.GRASS_BLOCK.getDefaultState();
	protected BlockState groundUnderBlock = Blocks.DIRT.getDefaultState();

	@Override
	public int getBedrockRoofHeight()
	{
		return 255;
	}

	@Override
	public int getBedrockFloorHeight()
	{
		return 0;
	}

	public int getGroundHeight()
	{
		return CavernConfig.INSTANCE.groundDecoration.get() ? 150 : 0;
	}

	public BlockState getGroundTopBlock()
	{
		return groundTopBlock;
	}

	public void setGroundTopBlock(BlockState state)
	{
		groundTopBlock = state;
	}

	public BlockState getGroundUnderBlock()
	{
		return groundUnderBlock;
	}

	public void setGroundUnderBlock(BlockState state)
	{
		groundUnderBlock = state;
	}
}