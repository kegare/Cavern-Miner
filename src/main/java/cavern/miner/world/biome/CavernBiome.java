package cavern.miner.world.biome;

import java.util.Optional;
import java.util.Random;

import cavern.miner.config.CavernConfig;
import cavern.miner.init.CaveFeatures;
import cavern.miner.init.CaveWorldCarvers;
import cavern.miner.world.carver.CaveConfiguredCarver;
import cavern.miner.world.dimension.CavernDimension;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;

public class CavernBiome extends Biome
{
	public CavernBiome()
	{
		super(new Biome.Builder().surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.STONE_STONE_GRAVEL_CONFIG).precipitation(Biome.RainType.NONE)
			.depth(0.125F).scale(0.05F).temperature(1.0F).downfall(0.0F).waterColor(4159204).waterFogColor(329011).category(Biome.Category.NONE).parent(null));
		this.addCarvers();
		this.addFeatures();
		this.addSpawns();
	}

	protected void addCarvers()
	{
		float probability = CavernConfig.INSTANCE.cave.get().floatValue();

		if (probability > 0.0F)
		{
			addCarver(GenerationStage.Carving.AIR, new CaveConfiguredCarver<>(() -> CaveWorldCarvers.CAVERN.orElse(WorldCarver.CAVE), new ProbabilityConfig(probability)));
		}

		probability = CavernConfig.INSTANCE.extremeCave.get().floatValue();

		if (probability > 0.0F)
		{
			addCarver(GenerationStage.Carving.AIR, new CaveConfiguredCarver<>(() -> CaveWorldCarvers.EXTREME_CAVE.orElse(WorldCarver.CAVE), new ProbabilityConfig(probability)));
		}

		probability = CavernConfig.INSTANCE.extremeCanyon.get().floatValue();

		if (probability > 0.0F)
		{
			addCarver(GenerationStage.Carving.AIR, new CaveConfiguredCarver<>(() -> CaveWorldCarvers.EXTREME_CANYON.orElse(WorldCarver.CANYON), new ProbabilityConfig(probability)));
		}
	}

	protected void addFeatures()
	{
		DefaultBiomeFeatures.addLakes(this);
		DefaultBiomeFeatures.addMonsterRooms(this);
		DefaultBiomeFeatures.addSedimentDisks(this);
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

	public void placeCaveFeatures(WorldGenRegion region, ChunkGenerator<? extends GenerationSettings> generator, BlockPos pos, Random rand)
	{
		CaveFeatures.VEIN.ifPresent(o -> o.place(region, generator, rand, pos, IFeatureConfig.NO_FEATURE_CONFIG));

		if (rand.nextDouble() < CavernConfig.INSTANCE.towerDungeon.get())
		{
			CaveFeatures.TOWER_DUNGEON.ifPresent(o -> o.place(region, generator, rand, pos.add(8, rand.nextInt(30) + 5, 8), IFeatureConfig.NO_FEATURE_CONFIG));
		}
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