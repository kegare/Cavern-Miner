package cavern.miner.world.carver;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.world.gen.feature.ProbabilityConfig;

public class ExtremeCaveWorldCarver extends CavernWorldCarver
{
	public ExtremeCaveWorldCarver(Function<Dynamic<?>, ? extends ProbabilityConfig> config, int maxHeight)
	{
		super(config, maxHeight);
	}

	@Override
	protected int func_222724_a()
	{
		return 10;
	}

	@Override
	protected float generateCaveRadius(Random rand)
	{
		float f = rand.nextFloat() * 10.0F + rand.nextFloat();

		if (rand.nextInt(5) == 0)
		{
			f *= rand.nextFloat() * rand.nextFloat() * 2.0F + 1.0F;
		}

		return f;
	}

	@Override
	protected double func_222725_b()
	{
		return 1.2D;
	}

	@Override
	protected int generateCaveStartY(Random rand)
	{
		return 170 + rand.nextInt(5);
	}
}