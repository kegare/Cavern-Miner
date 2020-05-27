package cavern.miner.capability;

import cavern.miner.enchantment.MiningUnit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CapabilityMiningUnit implements ICapabilityProvider
{
	private final MiningUnit assist;

	public CapabilityMiningUnit(EntityPlayer player)
	{
		this.assist = new MiningUnit(player);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == CaveCapabilities.MINING_UNIT;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (capability == CaveCapabilities.MINING_UNIT)
		{
			return CaveCapabilities.MINING_UNIT.cast(assist);
		}

		return null;
	}

	public static void register()
	{
		CapabilityManager.INSTANCE.register(MiningUnit.class, new EmptyStorage<>(), () -> new MiningUnit(null));
	}
}