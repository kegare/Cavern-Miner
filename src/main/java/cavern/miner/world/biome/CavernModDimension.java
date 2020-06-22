package cavern.miner.world.biome;

import java.util.function.BiFunction;

import cavern.miner.world.dimension.CavernDimension;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ModDimension;

public class CavernModDimension extends ModDimension
{
	private long seed;

	@Override
	public void write(PacketBuffer buffer, boolean network)
	{
		buffer.writeLong(seed);
	}

	@Override
	public void read(PacketBuffer buffer, boolean network)
	{
		seed = buffer.readLong();
	}

	public long getSeed()
	{
		return seed;
	}

	public void setSeed(long value)
	{
		seed = value;
	}

	@Override
	public BiFunction<World, DimensionType, ? extends Dimension> getFactory()
	{
		return CavernDimension::new;
	}
}