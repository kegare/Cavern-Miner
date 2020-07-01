package cavern.miner.init;

import cavern.miner.world.gen.carver.CavernCanyonWorldCarver;
import cavern.miner.world.gen.carver.CavernWorldCarver;
import cavern.miner.world.gen.carver.ExtremeCanyonWorldCarver;
import cavern.miner.world.gen.carver.ExtremeCaveWorldCarver;
import cavern.miner.world.gen.carver.HugeCaveWorldCarver;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class CaveWorldCarvers
{
	public static final DeferredRegister<WorldCarver<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.WORLD_CARVERS, "cavern");

	public static final RegistryObject<WorldCarver<ProbabilityConfig>> CAVERN = REGISTRY.register("cavern", () -> new CavernWorldCarver(ProbabilityConfig::deserialize));
	public static final RegistryObject<WorldCarver<ProbabilityConfig>> CAVERN_CANYON = REGISTRY.register("cavern_canyon", () -> new CavernCanyonWorldCarver(ProbabilityConfig::deserialize));
	public static final RegistryObject<WorldCarver<ProbabilityConfig>> EXTREME_CAVE = REGISTRY.register("extreme_cave", () -> new ExtremeCaveWorldCarver(ProbabilityConfig::deserialize));
	public static final RegistryObject<WorldCarver<ProbabilityConfig>> EXTREME_CANYON = REGISTRY.register("extreme_canyon", () -> new ExtremeCanyonWorldCarver(ProbabilityConfig::deserialize));

	public static final RegistryObject<WorldCarver<ProbabilityConfig>> HUGE_CAVE = REGISTRY.register("huge_cave", () -> new HugeCaveWorldCarver(ProbabilityConfig::deserialize));
}