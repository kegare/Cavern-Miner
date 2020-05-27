package cavern.miner.handler.api;

import cavern.miner.api.DataWrapper;
import cavern.miner.api.data.IMinerAccess;
import cavern.miner.data.Miner;
import net.minecraft.entity.player.EntityPlayer;

public class DataHandler implements DataWrapper
{
	@Override
	public IMinerAccess getMiner(EntityPlayer player)
	{
		return Miner.get(player);
	}
}