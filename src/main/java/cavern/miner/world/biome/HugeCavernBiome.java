package cavern.miner.world.biome;

import cavern.miner.config.HugeCavernConfig;
import cavern.miner.init.CaveFeatures;
import cavern.miner.init.CaveWorldCarvers;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;

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
			addCarver(GenerationStage.Carving.AIR, new ConfiguredCarver<>(CaveWorldCarvers.HUGE_CAVE.orElse(WorldCarver.CAVE), new ProbabilityConfig(probability)));
		}
	}

	@Override
	protected void addCaveFeatures()
	{
		CaveFeatures.VEIN.ifPresent(o -> addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
			o.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG))));
	}

	@Override
	protected void addFeatures()
	{
		DefaultBiomeFeatures.addLakes(this);
	}
}