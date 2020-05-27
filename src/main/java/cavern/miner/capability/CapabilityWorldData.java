package cavern.miner.capability;

import cavern.miner.data.WorldData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilityWorldData implements ICapabilitySerializable<NBTTagCompound>
{
	private final WorldData worldData;

	public CapabilityWorldData()
	{
		this.worldData = new WorldData();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == CaveCapabilities.WORLD_DATA;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (capability == CaveCapabilities.WORLD_DATA)
		{
			return CaveCapabilities.WORLD_DATA.cast(worldData);
		}

		return null;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		if (CaveCapabilities.WORLD_DATA != null)
		{
			return (NBTTagCompound)CaveCapabilities.WORLD_DATA.getStorage().writeNBT(CaveCapabilities.WORLD_DATA, worldData, null);
		}

		return new NBTTagCompound();
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		if (CaveCapabilities.WORLD_DATA != null)
		{
			CaveCapabilities.WORLD_DATA.getStorage().readNBT(CaveCapabilities.WORLD_DATA, worldData, null, nbt);
		}
	}

	public static void register()
	{
		CapabilityManager.INSTANCE.register(WorldData.class,
			new Capability.IStorage<WorldData>()
			{
				@Override
				public NBTBase writeNBT(Capability<WorldData> capability, WorldData instance, EnumFacing side)
				{
					NBTTagCompound nbt = new NBTTagCompound();

					instance.writeToNBT(nbt);

					return nbt;
				}

				@Override
				public void readNBT(Capability<WorldData> capability, WorldData instance, EnumFacing side, NBTBase nbt)
				{
					instance.readFromNBT((NBTTagCompound)nbt);
				}
			},
			WorldData::new
		);
	}
}