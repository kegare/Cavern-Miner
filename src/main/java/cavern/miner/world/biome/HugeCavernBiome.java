package cavern.miner.world.biome;

import cavern.miner.config.dimension.HugeCavernConfig;
import cavern.miner.init.CaveFeatures;
import cavern.miner.init.CavePlacements;
import cavern.miner.init.CaveWorldCarvers;
import cavern.miner.world.gen.feature.VeinFeatureConfig;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraft.world.gen.placement.ChanceConfig;
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
			addCarver(GenerationStage.Carving.AIR, createCarver(CaveWorldCarvers.HUGE_CAVE.get(), new ProbabilityConfig(probability)));
		}
	}

	@Override
	protected void addFeatures()
	{
		addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS,
			Feature.LAKE.withConfiguration(new BlockStateFeatureConfig(Blocks.WATER.getDefaultState())).withPlacement(CavePlacements.CAVE_LAKE.get().configure(new ChanceConfig(3))));
		addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS,
			Feature.LAKE.withConfiguration(new BlockStateFeatureConfig(Blocks.LAVA.getDefaultState())).withPlacement(CavePlacements.CAVE_LAKE.get().configure(new ChanceConfig(6))));

		addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
			CaveFeatures.VEIN.get().withConfiguration(VeinFeatureConfig.ProviderType.HUGE_CAVERN.createConfig()).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));
	}
}