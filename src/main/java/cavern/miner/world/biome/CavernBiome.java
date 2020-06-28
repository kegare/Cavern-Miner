package cavern.miner.world.biome;

import cavern.miner.config.dimension.CavernConfig;
import cavern.miner.init.CaveFeatures;
import cavern.miner.init.CavePlacements;
import cavern.miner.init.CaveWorldCarvers;
import cavern.miner.world.gen.feature.VeinFeatureConfig;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.BlockStateFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.ChanceRangeConfig;
import net.minecraft.world.gen.placement.CountConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;

public class CavernBiome extends Biome
{
	public CavernBiome()
	{
		super(new Biome.Builder().surfaceBuilder(SurfaceBuilder.NOPE, SurfaceBuilder.STONE_STONE_GRAVEL_CONFIG).precipitation(Biome.RainType.NONE)
			.depth(-1.0F).scale(0.0F).temperature(0.5F).downfall(0.0F).waterColor(4159204).waterFogColor(329011).category(Biome.Category.NONE).parent(null));
		this.addFeatures();
	}

	public void init()
	{
		addCarvers();
		addCaveFeatures();
	}

	protected void addCarvers()
	{
		float probability = CavernConfig.INSTANCE.cave.get().floatValue();

		if (probability > 0.0F)
		{
			addCarver(GenerationStage.Carving.AIR, createCarver(CaveWorldCarvers.CAVERN.get(), new ProbabilityConfig(probability)));
		}

		probability = CavernConfig.INSTANCE.canyon.get().floatValue();

		if (probability > 0.0F)
		{
			addCarver(GenerationStage.Carving.AIR, createCarver(CaveWorldCarvers.CAVERN_CANYON.get(), new ProbabilityConfig(probability)));
		}

		probability = CavernConfig.INSTANCE.extremeCave.get().floatValue();

		if (probability > 0.0F)
		{
			addCarver(GenerationStage.Carving.AIR, createCarver(CaveWorldCarvers.EXTREME_CAVE.get(), new ProbabilityConfig(probability)));
		}

		probability = CavernConfig.INSTANCE.extremeCanyon.get().floatValue();

		if (probability > 0.0F)
		{
			addCarver(GenerationStage.Carving.AIR, createCarver(CaveWorldCarvers.EXTREME_CANYON.get(), new ProbabilityConfig(probability)));
		}
	}

	protected void addCaveFeatures()
	{
		addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS,
			Feature.LAKE.withConfiguration(new BlockStateFeatureConfig(Blocks.WATER.getDefaultState())).withPlacement(CavePlacements.CAVE_LAKE.get().configure(new ChanceConfig(4))));
		addFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS,
			Feature.LAKE.withConfiguration(new BlockStateFeatureConfig(Blocks.LAVA.getDefaultState())).withPlacement(CavePlacements.CAVE_LAKE.get().configure(new ChanceConfig(10))));

		addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
			CaveFeatures.VEIN.get().withConfiguration(VeinFeatureConfig.ProviderType.CAVERN.createConfig()).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG)));

		float chance = CavernConfig.INSTANCE.towerDungeon.get().floatValue();

		if (chance > 0.0F)
		{
			addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
				CaveFeatures.TOWER_DUNGEON.get().withConfiguration(CavernConfig.INSTANCE.towerDungeonMobs.getConfig())
				.withPlacement(CavePlacements.CENTER_CHANCE_RANGE.get().configure(new ChanceRangeConfig(chance, 5, 0, 30))));
		}

		Placement<FrequencyConfig> place = CavePlacements.CENTER_NO_HEIGHT.get();
		Feature<BlockClusterFeatureConfig> feature = CaveFeatures.GROUND_PATCH.get();

		addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
			feature.withConfiguration(DefaultBiomeFeatures.GRASS_CONFIG).withPlacement(place.configure(new FrequencyConfig(20))));
		addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
			feature.withConfiguration(DefaultBiomeFeatures.DEFAULT_FLOWER_CONFIG).withPlacement(place.configure(new FrequencyConfig(1))));
		addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
			feature.withConfiguration(DefaultBiomeFeatures.BROWN_MUSHROOM_CONFIG).withPlacement(place.configure(new FrequencyConfig(1))));
		addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
			feature.withConfiguration(DefaultBiomeFeatures.RED_MUSHROOM_CONFIG).withPlacement(place.configure(new FrequencyConfig(1))));

		addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
			CaveFeatures.GROUND_TREE.get().withConfiguration(new CountConfig(32)).withPlacement(place.configure(new FrequencyConfig(10))));
	}

	protected void addFeatures()
	{
		DefaultBiomeFeatures.addMonsterRooms(this);
	}
}