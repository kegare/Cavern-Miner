package cavern.miner.world.gen.feature;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.block.BlockState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.RandomPatchFeature;

public class GroundPatchFeature extends RandomPatchFeature
{
	public GroundPatchFeature(Function<Dynamic<?>, ? extends BlockClusterFeatureConfig> factory)
	{
		super(factory);
	}

	@Override
	public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, BlockClusterFeatureConfig config)
	{
		int max = world.getMaxHeight() - 1;
		int ground = generator.getGroundHeight();
		int ySpread = max - ground;
		BlockState state = config.stateProvider.getBlockState(rand, pos);
		BlockPos blockPos;

		if (config.field_227298_k_)
		{
			blockPos = new BlockPos(pos.getX(), ground, pos.getZ());
		}
		else
		{
			blockPos = pos;
		}

		int i = 0;
		BlockPos.Mutable posCache = new BlockPos.Mutable();

		for (int count = 0; count < config.tryCount; ++count)
		{
			posCache.setPos(blockPos).move(rand.nextInt(config.xSpread + 1) - rand.nextInt(config.xSpread + 1), rand.nextInt(ySpread + 1), rand.nextInt(config.zSpread + 1) - rand.nextInt(config.zSpread + 1));

			if (posCache.getY() >= max)
			{
				continue;
			}

			BlockPos posBelow = posCache.down();
			BlockState stateBelow = world.getBlockState(posBelow);

			if ((world.isAirBlock(posCache) || config.isReplaceable && world.getBlockState(posCache).getMaterial().isReplaceable()) && state.isValidPosition(world, posCache) &&
				(config.whitelist.isEmpty() || config.whitelist.contains(stateBelow.getBlock())) && !config.blacklist.contains(stateBelow) &&
				(!config.requiresWater || world.getFluidState(posBelow.west()).isTagged(FluidTags.WATER) || world.getFluidState(posBelow.east()).isTagged(FluidTags.WATER) ||
				world.getFluidState(posBelow.north()).isTagged(FluidTags.WATER) || world.getFluidState(posBelow.south()).isTagged(FluidTags.WATER)))
			{
				config.blockPlacer.func_225567_a_(world, posCache, state, rand);

				++i;
			}
		}

		return i > 0;
	}
}