package cavern.miner.world.gen.feature;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.trees.BirchTree;
import net.minecraft.block.trees.OakTree;
import net.minecraft.block.trees.Tree;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.CountConfig;

public class GroundTreeFeature extends Feature<CountConfig>
{
	private static final BlockState OAK_SAPLING = Blocks.OAK_SAPLING.getDefaultState();
	private static final Tree OAK_TREE = new OakTree();

	private static final BlockState BIRCH_SAPLING = Blocks.BIRCH_SAPLING.getDefaultState();
	private static final Tree BIRCH_TREE = new BirchTree();

	public GroundTreeFeature(Function<Dynamic<?>, ? extends CountConfig> factory)
	{
		super(factory);
	}

	@Override
	public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, CountConfig config)
	{
		BlockState state = OAK_SAPLING;
		Tree tree = OAK_TREE;

		if (rand.nextInt(5) == 0)
		{
			state = BIRCH_SAPLING;
			tree = BIRCH_TREE;
		}

		int max = generator.getMaxHeight() - 1;
		int ground = generator.getGroundHeight();
		int ySpread = max - ground - 10;
		BlockPos blockPos = new BlockPos(pos.getX(), ground, pos.getZ());

		int i = 0;
		BlockPos.Mutable posCache = new BlockPos.Mutable();

		outside: for (int count = 0; count < config.count; ++count)
		{
			posCache.setPos(blockPos).move(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(ySpread + 1), rand.nextInt(8) - rand.nextInt(8));

			if (posCache.getY() >= max)
			{
				continue;
			}

			if (world.isAirBlock(posCache) && state.isValidPosition(world, posCache))
			{
				int y = posCache.getY();

				for (int j = 0; j < 3; ++j)
				{
					if (!world.isAirBlock(posCache.move(Direction.UP)))
					{
						continue outside;
					}
				}

				tree.place(world, generator, posCache.setPos(posCache.getX(), y, posCache.getZ()), state, rand);

				++i;
			}
		}

		return i > 0;
	}
}