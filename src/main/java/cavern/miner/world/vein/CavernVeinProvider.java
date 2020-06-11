package cavern.miner.world.vein;

import com.google.common.collect.ImmutableList;

import cavern.miner.config.CavernConfig;
import cavern.miner.util.BlockStateTagList;
import cavern.miner.vein.Vein;

public class CavernVeinProvider extends VeinProvider
{
	@Override
	public ImmutableList<Vein> getVeins()
	{
		return ImmutableList.copyOf(CavernConfig.VEINS.getVeins());
	}

	@Override
	public BlockStateTagList getWhitelist()
	{
		return CavernConfig.VEINS.getWhitelist();
	}

	@Override
	public BlockStateTagList getBlacklist()
	{
		return CavernConfig.VEINS.getBlacklist();
	}
}