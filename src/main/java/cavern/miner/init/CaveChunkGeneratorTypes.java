package cavern.miner.init;

import cavern.miner.world.gen.CavernChunkGenerator;
import cavern.miner.world.gen.CavernGenSettings;
import cavern.miner.world.gen.HugeCavernGenSettings;
import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class CaveChunkGeneratorTypes
{
	public static final DeferredRegister<ChunkGeneratorType<?, ?>> REGISTRY = DeferredRegister.create(ForgeRegistries.CHUNK_GENERATOR_TYPES, "cavern");

	public static final RegistryObject<ChunkGeneratorType<CavernGenSettings, CavernChunkGenerator>> CAVERN = REGISTRY.register("cavern",
		() -> new ChunkGeneratorType<>(CavernChunkGenerator::new, false, CavernGenSettings::new));
	public static final RegistryObject<ChunkGeneratorType<CavernGenSettings, CavernChunkGenerator>> HUGE_CAVERN = REGISTRY.register("huge_cavern",
		() -> new ChunkGeneratorType<>(CavernChunkGenerator::new, false, HugeCavernGenSettings::new));
}