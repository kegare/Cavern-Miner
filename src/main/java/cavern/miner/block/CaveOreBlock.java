package cavern.miner.block;

import java.util.Random;
import java.util.function.Function;

import org.apache.commons.lang3.ObjectUtils;

import net.minecraft.block.Block;
import net.minecraft.block.OreBlock;

public class CaveOreBlock extends OreBlock
{
	private final Function<Random, Integer> exp;

	public CaveOreBlock(Block.Properties properties, Function<Random, Integer> exp)
	{
		super(properties);
		this.exp = exp;
	}

	@Override
	protected int getExperience(Random rand)
	{
		return ObjectUtils.defaultIfNull(exp.apply(rand), 0);
	}
}