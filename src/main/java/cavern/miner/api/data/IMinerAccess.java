package cavern.miner.api.data;

import com.google.common.collect.ImmutableMap;

import cavern.miner.util.BlockMeta;

public interface IMinerAccess
{
	int getPoint();

	void setPoint(int value);

	void addPoint(int amount);

	int getRank();

	void setRank(int value);

	ImmutableMap<BlockMeta, Integer> getMiningRecords();
}