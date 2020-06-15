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

	public static final RegistryObject<Biome> CAVERN = REGISTRY.register("cavern", CavernBiome::new);
	public static final RegistryObject<Biome> HUGE_CAVERN = REGISTRY.register("huge_cavern", HugeCavernBiome::new);

	public static void init()
	{
		for (RegistryObject<Biome> biome : REGISTRY.getEntries())
		{
			biome.filter(o -> o instanceof CavernBiome).map(o -> (CavernBiome)o).ifPresent(CavernBiome::init);
		}
	}
}