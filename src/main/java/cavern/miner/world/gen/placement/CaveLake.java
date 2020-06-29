package cavern.miner.world.gen.placement;

import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

import com.mojang.datafixers.Dynamic;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.Placement;

public class CaveLake extends Placement<ChanceConfig>
{
	public CaveLake(Function<Dynamic<?>, ? extends ChanceConfig> factory)
	{
		super(factory);
	}

	@Override
	public Stream<BlockPos> getPositions(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, ChanceConfig config, BlockPos pos)
	{
		if (rand.nextInt(config.chance) == 0)
		{
			return Stream.of(new BlockPos(rand.nextInt(16) + pos.getX(), rand.nextInt(generator.getMaxHeight() - 15) + 5, rand.nextInt(16) + pos.getZ()));
		}

		return Stream.empty();
	}
}