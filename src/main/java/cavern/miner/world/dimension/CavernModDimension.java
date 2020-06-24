package cavern.miner.world.dimension;

import java.util.function.BiFunction;

import net.minecraft.world.World;
import net.minecraft.world.biome.FuzzedBiomeMagnifier;
import net.minecraft.world.biome.IBiomeMagnifier;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ModDimension;

public class CavernModDimension extends ModDimension
{
	@Override
	public IBiomeMagnifier getMagnifier()
	{
		return FuzzedBiomeMagnifier.INSTANCE;
	}

	@Override
	public BiFunction<World, DimensionType, ? extends Dimension> getFactory()
	{
		return CavernDimension::new;
	}
}