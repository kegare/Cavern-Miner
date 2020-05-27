package cavern.miner.capability;

import cavern.miner.data.Miner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilityMiner implements ICapabilitySerializable<NBTTagCompound>
{
	private final Miner miner;

	public CapabilityMiner(EntityPlayer player)
	{
		this.miner = new Miner(player);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return CaveCapabilities.MINER != null && capability == CaveCapabilities.MINER;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (CaveCapabilities.MINER != null && capability == CaveCapabilities.MINER)
		{
			return CaveCapabilities.MINER.cast(miner);
		}

		return null;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		if (CaveCapabilities.MINER != null)
		{
			return (NBTTagCompound)CaveCapabilities.MINER.getStorage().writeNBT(CaveCapabilities.MINER, miner, null);
		}

		return new NBTTagCompound();
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		if (CaveCapabilities.MINER != null)
		{
			CaveCapabilities.MINER.getStorage().readNBT(CaveCapabilities.MINER, miner, null, nbt);
		}
	}

	public static void register()
	{
		CapabilityManager.INSTANCE.register(Miner.class,
			new Capability.IStorage<Miner>()
			{
				@Override
				public NBTBase writeNBT(Capability<Miner> capability, Miner instance, EnumFacing side)
				{
					NBTTagCompound nbt = new NBTTagCompound();

					instance.writeToNBT(nbt);

					return nbt;
				}

				@Override
				public void readNBT(Capability<Miner> capability, Miner instance, EnumFacing side, NBTBase nbt)
				{
					instance.readFromNBT((NBTTagCompound)nbt);
				}
			},
			Miner::new
		);
	}
}