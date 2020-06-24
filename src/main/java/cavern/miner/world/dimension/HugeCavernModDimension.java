package cavern.miner.world.dimension;

import java.util.function.BiFunction;

import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;

public class HugeCavernModDimension extends CavernModDimension
{
	@Override
	public BiFunction<World, DimensionType, ? extends Dimension> getFactory()
	{
		return HugeCavernDimension::new;
	}
}