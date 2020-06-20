package cavern.miner.init;

import cavern.miner.world.biome.CavernBiome;
import cavern.miner.world.biome.HugeCavernBiome;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class CaveBiomes
{
	public static final DeferredRegister<Biome> REGISTRY = new DeferredRegister<>(ForgeRegistries.BIOMES, "cavern");

	public static final RegistryObject<CavernBiome> CAVERN = REGISTRY.register("cavern", CavernBiome::new);
	public static final RegistryObject<CavernBiome> HUGE_CAVERN = REGISTRY.register("huge_cavern", HugeCavernBiome::new);

	public static void init()
	{
		CAVERN.get().init();
		HUGE_CAVERN.get().init();
	}
}