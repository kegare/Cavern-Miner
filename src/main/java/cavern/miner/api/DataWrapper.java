package cavern.miner.api;

import cavern.miner.api.data.IMinerAccess;
import net.minecraft.entity.player.EntityPlayer;

public interface DataWrapper
{
	IMinerAccess getMiner(EntityPlayer player);
}