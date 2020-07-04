package cavern.miner.world.gen.carver;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.feature.ProbabilityConfig;

public class ExtremeCaveWorldCarver extends CavernWorldCarver
{
	public ExtremeCaveWorldCarver(Function<Dynamic<?>, ? extends ProbabilityConfig> factory)
	{
		super(factory);
	}

	@Override
	protected int func_222724_a()
	{
		return 20;
	}

	@Override
	protected float generateCaveRadius(Random rand)
	{
		float f = rand.nextFloat() * 20.0F + rand.nextFloat();

		if (rand.nextInt(10) == 0)
		{
			f *= rand.nextFloat() * rand.nextFloat() * 1.5F + 1.0F;
		}

		return f;
	}

	@Override
	protected double func_222725_b()
	{
		return 0.75D;
	}

	@Override
	protected int generateCaveStartY(Random rand)
	{
		return MathHelper.floor(maxHeight * 0.75D) + rand.nextInt(rand.nextInt(5) + 5) + 5;
	}
}