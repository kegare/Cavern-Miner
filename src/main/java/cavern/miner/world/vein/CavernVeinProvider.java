package cavern.miner.world.vein;

import com.google.common.collect.ImmutableList;

import cavern.miner.config.dimension.CavernConfig;
import cavern.miner.util.BlockStateTagList;

public class CavernVeinProvider extends VeinProvider
{
	@Override
	public ImmutableList<Vein> getVeins()
	{
		return ImmutableList.copyOf(CavernConfig.INSTANCE.veins.getVeins());
	}

	@Override
	public BlockStateTagList getWhitelist()
	{
		return CavernConfig.INSTANCE.veins.getWhitelist();
	}

	@Override
	public BlockStateTagList getBlacklist()
	{
		return CavernConfig.INSTANCE.veins.getBlacklist();
	}
}