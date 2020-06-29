package cavern.miner.world.gen.placement;

import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

import com.mojang.datafixers.Dynamic;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.placement.ChanceRangeConfig;
import net.minecraft.world.gen.placement.Placement;

public class CenterChanceRange extends Placement<ChanceRangeConfig>
{
	public CenterChanceRange(Function<Dynamic<?>, ? extends ChanceRangeConfig> factory)
	{
		super(factory);
	}

	@Override
	public Stream<BlockPos> getPositions(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, ChanceRangeConfig config, BlockPos pos)
	{
		if (rand.nextFloat() < config.chance)
		{
			return Stream.of(new BlockPos(pos.getX() + 8, rand.nextInt(config.top - config.topOffset) + config.bottomOffset, pos.getZ() + 8));
		}

		return Stream.empty();
	}
}