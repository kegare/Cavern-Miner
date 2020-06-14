package cavern.miner.world.vein;

import com.google.common.collect.ImmutableList;

import cavern.miner.config.HugeCavernConfig;
import cavern.miner.util.BlockStateTagList;

public class HugeCavernVeinProvider extends VeinProvider
{
	@Override
	public ImmutableList<Vein> getVeins()
	{
		return ImmutableList.copyOf(HugeCavernConfig.VEINS.getVeins());
	}

	@Override
	public BlockStateTagList getWhitelist()
	{
		return HugeCavernConfig.VEINS.getWhitelist();
	}

	@Override
	public BlockStateTagList getBlacklist()
	{
		return HugeCavernConfig.VEINS.getBlacklist();
	}
}