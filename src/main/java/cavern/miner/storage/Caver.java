package cavern.miner.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public class Caver implements INBTSerializable<CompoundNBT>
{
	private long sleepTime;

	public long getSleepTime()
	{
		return sleepTime;
	}

	public void setSleepTime(long time)
	{
		sleepTime = time;
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		CompoundNBT nbt = new CompoundNBT();

		nbt.putLong("SleepTime", sleepTime);

		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		sleepTime = nbt.getLong("SleepTime");
	}
}