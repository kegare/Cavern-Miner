package cavern.miner.init;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nullable;

import cavern.miner.CavernMod;
import cavern.miner.block.CavernPortalBlock;
import cavern.miner.world.dimension.CavernModDimension;
import cavern.miner.world.dimension.HugeCavernModDimension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
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
	public static final DeferredRegister<ModDimension> REGISTRY = DeferredRegister.create(ForgeRegistries.MOD_DIMENSIONS, "cavern");

	public static final RegistryObject<ModDimension> CAVERN = REGISTRY.register("cavern", () -> new CavernModDimension());
	public static final RegistryObject<ModDimension> HUGE_CAVERN = REGISTRY.register("huge_cavern", () -> new HugeCavernModDimension());

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
	public static CavernPortalBlock getPortalBlock(DimensionType dim)
	{
		for (CavernPortalBlock portal : CaveBlocks.CAVE_PORTALS.get())
		{
			if (portal.getDimension() == dim)
			{
				return portal;
			}
		}

		return null;
	}

	@Nullable
	public static File getSaveFolder(MinecraftServer server, DimensionType dim)
	{
		Path base = Paths.get(".").toAbsolutePath().normalize();
		Path directory = dim.getDirectory(base.toFile()).toPath().toAbsolutePath().normalize();

		if (!directory.startsWith(base))
		{
			return null;
		}

		String folderName = base.relativize(directory).toString();
		ServerWorld overworld = DimensionManager.getWorld(server, DimensionType.OVERWORLD, false, false);

		if (overworld == null)
		{
			return null;
		}

		File folder = new File(overworld.getSaveHandler().getWorldDirectory(), folderName);

		if (!folder.exists())
		{
			return null;
		}

		return folder;
	}
}