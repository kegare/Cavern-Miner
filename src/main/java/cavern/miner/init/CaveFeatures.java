package cavern.miner.init;

import cavern.miner.world.gen.feature.TowerDungeonFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CaveFeatures
{
	public static final DeferredRegister<Feature<?>> REGISTRY = new DeferredRegister<>(ForgeRegistries.FEATURES, "cavern");

	public static final RegistryObject<TowerDungeonFeature> TOWER_DUNGEON = REGISTRY.register("tower_dungeon", () -> new TowerDungeonFeature(NoFeatureConfig::deserialize));
}