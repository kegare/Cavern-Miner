package cavern.miner.world.carver;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.world.gen.carver.CaveWorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;

public class CavernWorldCarver extends CaveWorldCarver
{
	public CavernWorldCarver(Function<Dynamic<?>, ? extends ProbabilityConfig> config, int maxHeight)
	{
		super(config, maxHeight);
	}

	@Override
	protected int func_222724_a()
	{
		return 25;
	}

	@Override
	protected float generateCaveRadius(Random rand)
	{
		float f = rand.nextFloat() * 2.0F + rand.nextFloat();

		if (rand.nextInt(6) == 0)
		{
			f *= rand.nextFloat() * rand.nextFloat() * 4.0F + 1.0F;
		}

		return f;
	}

	@Override
	protected double func_222725_b()
	{
		return 1.25D;
	}

	@Override
	protected int generateCaveStartY(Random random)
	{
		return random.nextInt(random.nextInt(120) + 5) + 5;
	}
}