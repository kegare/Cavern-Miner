package cavern.miner.world.gen.carver;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.world.gen.feature.ProbabilityConfig;

public class HugeCaveWorldCarver extends CavernWorldCarver
{
	public HugeCaveWorldCarver(Function<Dynamic<?>, ? extends ProbabilityConfig> factory)
	{
		super(factory);
	}

	@Override
	public int getRandomNumber()
	{
		return 1;
	}

	@Override
	protected int func_222724_a()
	{
		return 5;
	}

	@Override
	protected float generateCaveRadius(Random rand)
	{
		float f = rand.nextFloat() * 50.0F + rand.nextFloat();

		if (rand.nextInt(5) == 0)
		{
			f *= rand.nextFloat() * rand.nextFloat() + 1.0F;
		}

		return f;
	}

	@Override
	protected double func_222725_b()
	{
		return 2.0D;
	}

	@Override
	protected int generateCaveStartY(Random rand)
	{
		return 120 + rand.nextInt(rand.nextInt(15) + 5);
	}
}