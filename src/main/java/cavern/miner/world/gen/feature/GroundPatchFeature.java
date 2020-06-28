package cavern.miner.world.gen.feature;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.block.BlockState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
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
		int max = generator.getMaxHeight() - 1;
		int ground = generator.getGroundHeight();
		int ySpread = max - ground;
		BlockState state = config.stateProvider.getBlockState(rand, pos);
		BlockPos originPos;

		if (config.field_227298_k_)
		{
			originPos = new BlockPos(pos.getX(), ground, pos.getZ());
		}
		else
		{
			originPos = pos;
		}

		int i = 0;
		BlockPos.Mutable posHere = new BlockPos.Mutable();
		BlockPos.Mutable posBelow = new BlockPos.Mutable();

		for (int count = 0; count < config.tryCount; ++count)
		{
			posHere.setPos(originPos).move(rand.nextInt(config.xSpread + 1) - rand.nextInt(config.xSpread + 1), rand.nextInt(ySpread + 1), rand.nextInt(config.zSpread + 1) - rand.nextInt(config.zSpread + 1));

			if (posHere.getY() >= max)
			{
				continue;
			}

			BlockState stateBelow = world.getBlockState(posBelow.setPos(posHere).move(Direction.DOWN));

			if ((world.isAirBlock(posHere) || config.isReplaceable && world.getBlockState(posHere).getMaterial().isReplaceable()) && state.isValidPosition(world, posHere) &&
				(config.whitelist.isEmpty() || config.whitelist.contains(stateBelow.getBlock())) && !config.blacklist.contains(stateBelow) &&
				(!config.requiresWater || world.getFluidState(posBelow.west()).isTagged(FluidTags.WATER) || world.getFluidState(posBelow.east()).isTagged(FluidTags.WATER) ||
				world.getFluidState(posBelow.north()).isTagged(FluidTags.WATER) || world.getFluidState(posBelow.south()).isTagged(FluidTags.WATER)))
			{
				config.blockPlacer.func_225567_a_(world, posHere, state, rand);

				++i;
			}
		}

		return i > 0;
	}
}