package cavern.miner.world.gen.feature;

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

public class TowerDungeonPlacement extends Placement<ChanceRangeConfig>
{
	public TowerDungeonPlacement(Function<Dynamic<?>, ? extends ChanceRangeConfig> factory)
	{
		super(factory);
	}

	@Override
	public Stream<BlockPos> getPositions(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random random, ChanceRangeConfig config, BlockPos pos)
	{
		if (random.nextDouble() < config.chance)
		{
			int x = pos.getX() + 8;
			int z = pos.getZ() + 8;
			int y = random.nextInt(config.top - config.topOffset) + config.bottomOffset;

			return Stream.of(new BlockPos(x, y, z));
		}

		return Stream.empty();
	}
}