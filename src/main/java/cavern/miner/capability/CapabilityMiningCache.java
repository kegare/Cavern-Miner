package cavern.miner.capability;

import cavern.miner.data.MiningCache;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CapabilityMiningCache implements ICapabilityProvider
{
	private MiningCache data;

	public CapabilityMiningCache(EntityPlayer player)
	{
		this.data = new MiningCache(player);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == CaveCapabilities.MINING_CACHE;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (capability == CaveCapabilities.MINING_CACHE)
		{
			return CaveCapabilities.MINING_CACHE.cast(data);
		}

		return null;
	}

	public static void register()
	{
		CapabilityManager.INSTANCE.register(MiningCache.class, new EmptyStorage<>(), () -> new MiningCache(null));
	}
}