package cavern.miner.block;

import java.util.Random;
import java.util.function.ToIntFunction;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class CaveOreBlock extends OreBlock
{
	private final ToIntFunction<Random> exp;

	public CaveOreBlock(Block.Properties properties)
	{
		this(properties, null);
	}

	public CaveOreBlock(Block.Properties properties, @Nullable ToIntFunction<Random> exp)
	{
		super(properties);
		this.exp = exp;
	}

	@Override
	protected int getExperience(Random rand)
	{
		return exp == null ? 0 : exp.applyAsInt(rand);
	}

	public int getPoint(BlockState state, IWorldReader reader, BlockPos pos, int point)
	{
		return point;
	}
}