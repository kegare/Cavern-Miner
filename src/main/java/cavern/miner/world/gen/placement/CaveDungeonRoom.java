package cavern.miner.world.gen.placement;

import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mojang.datafixers.Dynamic;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;

public class CaveDungeonRoom extends Placement<FrequencyConfig>
{
	public CaveDungeonRoom(Function<Dynamic<?>, ? extends FrequencyConfig> factory)
	{
		super(factory);
	}

	@Override
	public Stream<BlockPos> getPositions(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, FrequencyConfig config, BlockPos pos)
	{
		return IntStream.range(0, config.count).mapToObj(o ->
		{
			int ground = generator.getGroundHeight();
			int max = ground > 0 ? ground - 5 : generator.getMaxHeight() - 10;

			return new BlockPos(rand.nextInt(16) + pos.getX(), rand.nextInt(max), rand.nextInt(16) + pos.getZ());
		});
	}
}