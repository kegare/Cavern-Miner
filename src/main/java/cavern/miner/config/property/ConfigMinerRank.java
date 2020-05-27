package cavern.miner.config.property;

import cavern.miner.data.MinerRank;

public class ConfigMinerRank
{
	private int value;

	public int getValue()
	{
		return value;
	}

	public void setValue(int rank)
	{
		value = rank;
	}

	public MinerRank getRank()
	{
		return MinerRank.get(getValue());
	}
}