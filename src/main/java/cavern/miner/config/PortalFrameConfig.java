package cavern.miner.config;

import java.io.File;

import net.minecraft.block.Blocks;

public class PortalFrameConfig extends BlockStateTagListConfig
{
	public PortalFrameConfig(File dir)
	{
		super(dir, "portal_frames");
	}

	@Override
	public void setDefault()
	{
		list.clear();
		list.add(Blocks.MOSSY_COBBLESTONE).add(Blocks.MOSSY_STONE_BRICKS);
	}
}