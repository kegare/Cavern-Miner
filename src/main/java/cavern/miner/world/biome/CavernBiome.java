package cavern.miner.world.biome;

import cavern.miner.config.dimension.CavernConfig;
import cavern.miner.init.CaveFeatures;
import cavern.miner.init.CavePlacements;
import cavern.miner.init.CaveWorldCarvers;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.ProbabilityConfig;
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
		super(new Biome.Builder().surfaceBuilder(SurfaceBuilder.NOPE, SurfaceBuilder.AIR_CONFIG).precipitation(Biome.RainType.NONE)
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
			addCarver(GenerationStage.Carving.AIR, new ConfiguredCarver<>(CaveWorldCarvers.CAVERN.orElse(WorldCarver.CAVE), new ProbabilityConfig(probability)));
		}

		probability = CavernConfig.INSTANCE.canyon.get().floatValue();

		if (probability > 0.0F)
		{
			addCarver(GenerationStage.Carving.AIR, new ConfiguredCarver<>(CaveWorldCarvers.CAVERN_CANYON.orElse(WorldCarver.CANYON), new ProbabilityConfig(probability)));
		}

		probability = CavernConfig.INSTANCE.extremeCave.get().floatValue();

		if (probability > 0.0F)
		{
			addCarver(GenerationStage.Carving.AIR, new ConfiguredCarver<>(CaveWorldCarvers.EXTREME_CAVE.orElse(WorldCarver.CAVE), new ProbabilityConfig(probability)));
		}

		probability = CavernConfig.INSTANCE.extremeCanyon.get().floatValue();

		if (probability > 0.0F)
		{
			addCarver(GenerationStage.Carving.AIR, new ConfiguredCarver<>(CaveWorldCarvers.EXTREME_CANYON.orElse(WorldCarver.CANYON), new ProbabilityConfig(probability)));
		}
	}

	protected void addCaveFeatures()
	{
		CaveFeatures.VEIN.ifPresent(o -> addFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
			o.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG).withPlacement(Placement.NOPE.configure(IPlacementConfig.NO_PLACEMENT_CONFIG))));

		float chance = CavernConfig.INSTANCE.towerDungeon.get().floatValue();

		if (chance > 0.0F)
		{
			CaveFeatures.TOWER_DUNGEON.ifPresent(o -> addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
				o.withConfiguration(CavernConfig.TOWER_DUNGEON_MOBS.getConfig())
				.withPlacement(CavePlacements.CENTER_CHANCE_RANGE.orElse(Placement.CHANCE_RANGE).configure(new ChanceRangeConfig(chance, 5, 0, 30)))));
		}

		Placement<FrequencyConfig> place = CavePlacements.CENTER_NO_HEIGHT.orElse(Placement.COUNT_HEIGHT_64);

		CaveFeatures.GROUND_PATCH.ifPresent(o ->
		{
			addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
				o.withConfiguration(DefaultBiomeFeatures.GRASS_CONFIG).withPlacement(place.configure(new FrequencyConfig(20))));
			addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
				o.withConfiguration(DefaultBiomeFeatures.DEFAULT_FLOWER_CONFIG).withPlacement(place.configure(new FrequencyConfig(1))));
			addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
				o.withConfiguration(DefaultBiomeFeatures.BROWN_MUSHROOM_CONFIG).withPlacement(place.configure(new FrequencyConfig(1))));
			addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
				o.withConfiguration(DefaultBiomeFeatures.RED_MUSHROOM_CONFIG).withPlacement(place.configure(new FrequencyConfig(1))));
		});

		CaveFeatures.GROUND_TREE.ifPresent(o -> addFeature(GenerationStage.Decoration.VEGETAL_DECORATION,
			o.withConfiguration(new CountConfig(32)).withPlacement(place.configure(new FrequencyConfig(10)))));
	}

	protected void addFeatures()
	{
		DefaultBiomeFeatures.addLakes(this);
		DefaultBiomeFeatures.addMonsterRooms(this);
	}
}