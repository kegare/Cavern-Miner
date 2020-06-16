package cavern.miner.init;

import javax.annotation.Nullable;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.world.dimension.CavernDimension;
import cavern.miner.world.dimension.HugeCavernDimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "cavern")
public final class CaveDimensions
{
	public static final DeferredRegister<ModDimension> REGISTRY = new DeferredRegister<>(ForgeRegistries.MOD_DIMENSIONS, "cavern");

	public static final RegistryObject<ModDimension> CAVERN = REGISTRY.register("cavern", () -> ModDimension.withFactory(CavernDimension::new));
	public static final RegistryObject<ModDimension> HUGE_CAVERN = REGISTRY.register("huge_cavern", () -> ModDimension.withFactory(HugeCavernDimension::new));

	public static DimensionType CAVERN_TYPE;
	public static DimensionType HUGE_CAVERN_TYPE;

	@SubscribeEvent
	public static void registerDimensions(final RegisterDimensionsEvent event)
	{
		CAVERN_TYPE = CAVERN.map(o -> DimensionManager.registerOrGetDimension(o.getRegistryName(), o, null, false)).orElse(null);
		HUGE_CAVERN_TYPE = HUGE_CAVERN.map(o -> DimensionManager.registerOrGetDimension(o.getRegistryName(), o, null, false)).orElse(null);
	}

	@Nullable
	public static CavernPortalBlock getPortalBlock(@Nullable DimensionType type)
	{
		if (type == null)
		{
			return null;
		}

		for (RegistryObject<CavernPortalBlock> portal : CaveBlocks.CAVE_PORTALS)
		{
			if (portal.map(CavernPortalBlock::getDimension).orElse(null) == type)
			{
				return portal.get();
			}
		}

		return null;
	}
}