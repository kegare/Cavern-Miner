package cavern.miner.world.biome;

import java.util.Random;

import cavern.miner.config.HugeCavernConfig;
import cavern.miner.init.CaveFeatures;
import cavern.miner.init.CaveWorldCarvers;
import cavern.miner.world.carver.CaveConfiguredCarver;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.ProbabilityConfig;

public class HugeCavernBiome extends CavernBiome
{
	public HugeCavernBiome()
	{
		super();
	}

	@Override
	protected void addCarvers()
	{
		float probability = HugeCavernConfig.INSTANCE.cave.get().floatValue();

		if (probability > 0.0F)
		{
			addCarver(GenerationStage.Carving.AIR, new CaveConfiguredCarver<>(() -> CaveWorldCarvers.HUGE_CAVE.orElse(WorldCarver.CAVE), new ProbabilityConfig(probability)));
		}
	}

	@Override
	public void placeCaveFeatures(WorldGenRegion region, ChunkGenerator<? extends GenerationSettings> generator, BlockPos pos, Random rand)
	{
		CaveFeatures.VEIN.ifPresent(o -> o.place(region, generator, rand, pos, IFeatureConfig.NO_FEATURE_CONFIG));
	}
}