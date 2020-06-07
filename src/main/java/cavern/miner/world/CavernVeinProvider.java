package cavern.miner.world;

import cavern.miner.config.CavernConfig;
import cavern.miner.util.BlockStateTagList;
import cavern.miner.vein.Vein;
import net.minecraft.util.NonNullList;

public class CavernVeinProvider extends VeinProvider
{
	@Override
	public NonNullList<Vein> getVeins()
	{
		return CavernConfig.VEINS.getVeins();
	}

	@Override
	public BlockStateTagList getBlacklist()
	{
		return CavernConfig.VEINS_BLACKLIST.getEntries();
	}
}