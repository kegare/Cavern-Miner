package cavern.miner.init;

import cavern.miner.world.biome.CavernBiome;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class CaveBiomes
{
	public static final DeferredRegister<Biome> REGISTRY = new DeferredRegister<>(ForgeRegistries.BIOMES, "cavern");

	public static final RegistryObject<Biome> CAVERN = REGISTRY.register("cavern", CavernBiome::new);
}