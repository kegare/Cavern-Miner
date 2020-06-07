package cavern.miner.config;

import java.io.File;

import net.minecraft.block.Blocks;
import net.minecraftforge.common.Tags;

public class VeinBlacklistConfig extends BlockStateTagListConfig
{
	public VeinBlacklistConfig(File dir, String name)
	{
		super(dir, name + "_veins_blacklist");
	}

	@Override
	public void setDefault()
	{
		list.clear();
		list.add(Blocks.STONE).add(Blocks.POLISHED_ANDESITE).add(Blocks.POLISHED_DIORITE).add(Blocks.POLISHED_GRANITE);
		list.add(Tags.Blocks.ORES_QUARTZ);
	}
}