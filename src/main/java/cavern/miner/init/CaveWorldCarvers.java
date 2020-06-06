package cavern.miner.init;

import cavern.miner.world.carver.CavernWorldCarver;
import cavern.miner.world.carver.ExtremeCaveWorldCarver;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class CaveWorldCarvers
{
	public static final DeferredRegister<WorldCarver<?>> REGISTRY = new DeferredRegister<>(ForgeRegistries.WORLD_CARVERS, "cavern");

	public static final RegistryObject<WorldCarver<ProbabilityConfig>> CAVERN = REGISTRY.register("cavern", () -> new CavernWorldCarver(ProbabilityConfig::deserialize, 256));
	public static final RegistryObject<WorldCarver<ProbabilityConfig>> EXTREME_CAVE = REGISTRY.register("extreme_cave", () -> new ExtremeCaveWorldCarver(ProbabilityConfig::deserialize, 256));
}