package cavern.miner.init;

import cavern.miner.world.gen.feature.GroundPatchFeature;
import cavern.miner.world.gen.feature.GroundTreeFeature;
import cavern.miner.world.gen.feature.TowerDungeonFeature;
import cavern.miner.world.gen.feature.VeinFeature;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.CountConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CaveFeatures
{
	public static final DeferredRegister<Feature<?>> REGISTRY = new DeferredRegister<>(ForgeRegistries.FEATURES, "cavern");

	public static final RegistryObject<VeinFeature> VEIN = REGISTRY.register("vein", () -> new VeinFeature(NoFeatureConfig::deserialize));
	public static final RegistryObject<TowerDungeonFeature> TOWER_DUNGEON = REGISTRY.register("tower_dungeon", () -> new TowerDungeonFeature(NoFeatureConfig::deserialize));

	public static final RegistryObject<GroundPatchFeature> GROUND_PATCH = REGISTRY.register("ground_patch", () -> new GroundPatchFeature(BlockClusterFeatureConfig::deserialize));
	public static final RegistryObject<GroundTreeFeature> GROUND_TREE = REGISTRY.register("ground_tree", () -> new GroundTreeFeature(CountConfig::deserialize));
}