package cavern.miner.init;

import cavern.miner.world.gen.placement.CenterChanceRange;
import cavern.miner.world.gen.placement.CenterNoHeight;
import net.minecraft.world.gen.placement.ChanceRangeConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CavePlacements
{
	public static final DeferredRegister<Placement<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.DECORATORS, "cavern");

	public static final RegistryObject<Placement<FrequencyConfig>> CENTER_NO_HEIGHT = REGISTRY.register("center_no_height", () -> new CenterNoHeight(FrequencyConfig::deserialize));
	public static final RegistryObject<Placement<ChanceRangeConfig>> CENTER_CHANCE_RANGE = REGISTRY.register("center_chance_range", () -> new CenterChanceRange(ChanceRangeConfig::deserialize));
}