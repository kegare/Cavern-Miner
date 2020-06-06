package cavern.miner.init;

import javax.annotation.Nullable;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.world.CavernDimension;
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

	public static DimensionType CAVERN_TYPE;

	@SubscribeEvent
	public static void registerDimensions(final RegisterDimensionsEvent event)
	{
		CAVERN_TYPE = DimensionManager.registerOrGetDimension(CAVERN.getId(), CAVERN.get(), null, false);
	}

	@Nullable
	public static CavernPortalBlock getPortalBlock(@Nullable DimensionType type)
	{
		if (type == null)
		{
			return null;
		}

		for (CavernPortalBlock portal : CaveBlocks.CAVE_PORTALS.get())
		{
			if (portal.getDimension() == type)
			{
				return portal;
			}
		}

		return null;
	}
}