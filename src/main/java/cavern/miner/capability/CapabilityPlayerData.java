package cavern.miner.capability;

import cavern.miner.data.PlayerData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class CapabilityPlayerData implements ICapabilitySerializable<NBTTagCompound>
{
	private final PlayerData playerData;

	public CapabilityPlayerData()
	{
		this.playerData = new PlayerData();
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return capability == CaveCapabilities.PLAYER_DATA;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (capability == CaveCapabilities.PLAYER_DATA)
		{
			return CaveCapabilities.PLAYER_DATA.cast(playerData);
		}

		return null;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		if (CaveCapabilities.PLAYER_DATA != null)
		{
			return (NBTTagCompound)CaveCapabilities.PLAYER_DATA.getStorage().writeNBT(CaveCapabilities.PLAYER_DATA, playerData, null);
		}

		return new NBTTagCompound();
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		if (CaveCapabilities.PLAYER_DATA != null)
		{
			CaveCapabilities.PLAYER_DATA.getStorage().readNBT(CaveCapabilities.PLAYER_DATA, playerData, null, nbt);
		}
	}

	public static void register()
	{
		CapabilityManager.INSTANCE.register(PlayerData.class,
			new Capability.IStorage<PlayerData>()
			{
				@Override
				public NBTBase writeNBT(Capability<PlayerData> capability, PlayerData instance, EnumFacing side)
				{
					NBTTagCompound nbt = new NBTTagCompound();

					instance.writeToNBT(nbt);

					return nbt;
				}

				@Override
				public void readNBT(Capability<PlayerData> capability, PlayerData instance, EnumFacing side, NBTBase nbt)
				{
					instance.readFromNBT((NBTTagCompound)nbt);
				}
			},
			PlayerData::new
		);
	}
}