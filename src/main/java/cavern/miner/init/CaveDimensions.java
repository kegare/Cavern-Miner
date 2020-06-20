package cavern.miner.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.annotation.Nullable;

import cavern.miner.CavernMod;
import cavern.miner.block.CavernPortalBlock;
import cavern.miner.world.dimension.CavernDimension;
import cavern.miner.world.dimension.HugeCavernDimension;
import net.minecraft.world.dimension.Dimension;
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

	public static final DimensionType CAVERN_TYPE = null;
	public static final DimensionType HUGE_CAVERN_TYPE = null;

	@SubscribeEvent
	public static void registerDimensions(final RegisterDimensionsEvent event)
	{
		attachDimensionType("cavern", DimensionManager.registerOrGetDimension(CAVERN.getId(), CAVERN.get(), null, false));
		attachDimensionType("huge_cavern", DimensionManager.registerOrGetDimension(HUGE_CAVERN.getId(), HUGE_CAVERN.get(), null, false));
	}

	private static void attachDimensionType(String name, DimensionType type)
	{
		if (type == null)
		{
			CavernMod.LOG.error("Unable to inject dimension type {} (Invalid Registry)", name);

			return;
		}

		final Class<?> targetClass = CaveDimensions.class;
		final String fieldName = (name + "_type").toUpperCase();

		try
		{
			Field field = targetClass.getDeclaredField(fieldName);

			if ((field.getModifiers() & Modifier.STATIC) != Modifier.STATIC)
			{
				CavernMod.LOG.error("Unable to inject dimension type {} at {}.{} (Non-Static)", name, targetClass.getName(), fieldName);

				return;
			}

			Field modifiersField = Field.class.getDeclaredField("modifiers");

			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

			field.set(null, type);
		}
		catch (Exception e)
		{
			CavernMod.LOG.error("Unable to inject dimension type {} at {}.{}", name, targetClass.getName(), fieldName, e);
		}
	}

	@Nullable
	public static CavernPortalBlock getPortalBlock(Dimension dimension)
	{
		for (RegistryObject<CavernPortalBlock> portal : CaveBlocks.CAVE_PORTALS)
		{
			if (portal.get().getDimension() == dimension.getType())
			{
				return portal.get();
			}
		}

		return null;
	}
}