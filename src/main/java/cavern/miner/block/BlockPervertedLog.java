package cavern.miner.block;

import cavern.miner.core.CavernMod;
import net.minecraft.block.BlockOldLog;

public class BlockPervertedLog extends BlockOldLog
{
	public BlockPervertedLog()
	{
		super();
		this.setUnlocalizedName("pervertedLog");
		this.setHardness(1.2F);
		this.setCreativeTab(CavernMod.TAB_CAVERN);
	}
}