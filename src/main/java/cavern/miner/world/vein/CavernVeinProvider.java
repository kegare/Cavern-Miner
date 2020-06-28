package cavern.miner.world.vein;

import java.util.List;

import cavern.miner.config.dimension.CavernConfig;
import cavern.miner.util.BlockStateTagList;

public class CavernVeinProvider extends VeinProvider
{
	@Override
	public List<Vein> getVeins()
	{
		return CavernConfig.INSTANCE.veins.getVeins();
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

	@Override
	public List<String> getBlacklistMods()
	{
		return CavernConfig.INSTANCE.veins.getBlacklistMods();
	}
}