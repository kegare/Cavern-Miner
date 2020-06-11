package cavern.miner.world.biome;

import cavern.miner.init.CaveWorldCarvers;
import cavern.miner.world.carver.CaveConfiguredCarver;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;

public class CavernBiome extends Biome
{
	public CavernBiome()
	{
		super(new Biome.Builder().surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.STONE_STONE_GRAVEL_CONFIG).precipitation(Biome.RainType.NONE)
			.depth(0.125F).scale(0.05F).temperature(1.0F).downfall(0.0F).waterColor(4159204).waterFogColor(329011).category(Biome.Category.NONE).parent(null));
		this.addCarver(GenerationStage.Carving.AIR, new CaveConfiguredCarver<>(() -> CaveWorldCarvers.CAVERN.orElse(WorldCarver.CAVE), new ProbabilityConfig(0.2F)));
		this.addCarver(GenerationStage.Carving.AIR, new CaveConfiguredCarver<>(() -> CaveWorldCarvers.EXTREME_CAVE.orElse(WorldCarver.CAVE), new ProbabilityConfig(0.1F)));
		this.addCarver(GenerationStage.Carving.AIR, new CaveConfiguredCarver<>(() -> CaveWorldCarvers.EXTREME_CANYON.orElse(WorldCarver.CANYON), new ProbabilityConfig(0.003F)));
		DefaultBiomeFeatures.addLakes(this);
		DefaultBiomeFeatures.addMonsterRooms(this);
		DefaultBiomeFeatures.addSedimentDisks(this);
		this.addSpawn(EntityClassification.AMBIENT, new Biome.SpawnListEntry(EntityType.BAT, 10, 8, 8));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.SPIDER, 100, 4, 4));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.ZOMBIE, 95, 4, 4));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.SKELETON, 100, 4, 4));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.CREEPER, 100, 4, 4));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.SLIME, 100, 4, 4));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.ENDERMAN, 10, 1, 4));
		this.addSpawn(EntityClassification.MONSTER, new Biome.SpawnListEntry(EntityType.WITCH, 5, 1, 1));
	}
}