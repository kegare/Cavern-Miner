package cavern.miner.init;

import cavern.miner.world.gen.feature.TowerDungeonPlacement;
import net.minecraft.world.gen.placement.ChanceRangeConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CavePlacements
{
	public static final DeferredRegister<Placement<?>> REGISTRY = new DeferredRegister<>(ForgeRegistries.DECORATORS, "cavern");

	public static final RegistryObject<Placement<ChanceRangeConfig>> TOWER_DUNGEON = REGISTRY.register("tower_dungeon", () -> new TowerDungeonPlacement(ChanceRangeConfig::deserialize));
}