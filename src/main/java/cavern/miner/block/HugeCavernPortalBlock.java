package cavern.miner.block;

import cavern.miner.config.dimension.HugeCavernConfig;
import cavern.miner.init.CaveDimensions;
import cavern.miner.util.BlockStateTagList;
import cavern.miner.util.ItemStackTagList;
import net.minecraft.world.dimension.DimensionType;

public class HugeCavernPortalBlock extends CavernPortalBlock
{
	public HugeCavernPortalBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	public DimensionType getDimension()
	{
		return CaveDimensions.HUGE_CAVERN_TYPE;
	}

	@Override
	public ItemStackTagList getTriggerItems()
	{
		return HugeCavernConfig.PORTAL.getTriggerItems();
	}

	@Override
	public BlockStateTagList getFrameBlocks()
	{
		return HugeCavernConfig.PORTAL.getFrameBlocks();
	}
}