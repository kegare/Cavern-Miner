package cavern.miner.world.biome;

import java.util.Optional;

import cavern.miner.config.CavernConfig;
import cavern.miner.init.CaveEntities;
import cavern.miner.init.CaveFeatures;
import cavern.miner.init.CavePlacements;
import cavern.miner.init.CaveWorldCarvers;
import cavern.miner.world.dimension.CavernDimension;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.world.IWorld;
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
		super(new Biome.Builder().surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.STONE_STONE_GRAVEL_CONFIG).precipitation(Biome.RainType.NONE)
			.depth(0.125F).scale(0.05F).temperature(0.5F).downfall(0.0F).waterColor(4159204).waterFogColor(329011).category(Biome.Category.NONE).parent(null));
		this.addFeatures();
		this.addSpawns();
	}

	public void init()
	{
		addCarvers();
		addCaveFeatures();
		addCaveSpawns();
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
			CaveFeatures.TOWER_DUNGEON.ifPresent(o -> addFeature(GenerationStage.Decoration.UNDERGROUND_STRUCTURES, o.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
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

	protected void addSpawns()
	{
		addSpawn(EntityClassification.AMBIENT, new Biome.SpawnListEntry(EntityType.BAT, 20, 8, 8));
		addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.SPIDER, 100, 4, 4));
		addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.ZOMBIE, 95, 4, 4));
		addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.ZOMBIE_VILLAGER, 15, 1, 1));
		addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.SKELETON, 100, 4, 4));
		addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.CREEPER, 100, 4, 4));
		addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.SLIME, 50, 4, 4));
		addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.ENDERMAN, 10, 1, 4));
		addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.WITCH, 5, 1, 1));
	}

	protected void addCaveSpawns()
	{
		CaveEntities.CAVENIC_SKELETON.ifPresent(o -> addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(o, 15, 1, 1)));
	}

	public static Optional<CavernBiome> get(IWorld world)
	{
		if (world.getDimension() instanceof CavernDimension)
		{
			CavernDimension cavern = (CavernDimension)world.getDimension();

			if (cavern.getBiome() instanceof CavernBiome)
			{
				return Optional.of((CavernBiome)cavern.getBiome());
			}
		}

		return Optional.empty();
	}
}