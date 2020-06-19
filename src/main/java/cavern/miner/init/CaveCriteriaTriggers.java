package cavern.miner.init;

import cavern.miner.advancement.MinerRankTrigger;
import cavern.miner.advancement.MiningComboTrigger;
import net.minecraft.advancements.CriteriaTriggers;

public class CaveCriteriaTriggers
{
	public static final MiningComboTrigger MINING_COMBO = new MiningComboTrigger();
	public static final MinerRankTrigger MINER_RANK = new MinerRankTrigger();

	public static void registerTriggers()
	{
		CriteriaTriggers.register(MINING_COMBO);
		CriteriaTriggers.register(MINER_RANK);
	}
}