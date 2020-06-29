package cavern.miner.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

public class NBTSerializableCapability<E> implements ICapabilitySerializable<INBT>
{
	private final Capability<E> capability;
	private final LazyOptional<E> instance;

	public NBTSerializableCapability(Capability<E> capability, NonNullSupplier<E> instance)
	{
		this.capability = capability;
		this.instance = LazyOptional.of(instance);
	}

	@Override
	public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
	{
		return capability.orEmpty(cap, instance);
	}

	@Override
	public INBT serializeNBT()
	{
		return instance.map(o -> capability.writeNBT(o, null)).orElse(new CompoundNBT());
	}

	@Override
	public void deserializeNBT(INBT nbt)
	{
		instance.ifPresent(o -> capability.readNBT(o, null, nbt));
	}

	public static <T extends INBT, E extends INBTSerializable<T>> Storage<T, E> createStorage()
	{
		return new Storage<>();
	}

	private static class Storage<T extends INBT, E extends INBTSerializable<T>> implements Capability.IStorage<E>
	{
		@Override
		public INBT writeNBT(Capability<E> capability, E instance, Direction side)
		{
			return instance.serializeNBT();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void readNBT(Capability<E> capability, E instance, Direction side, INBT nbt)
		{
			instance.deserializeNBT((T)nbt);
		}
	}
}