package cavern.miner.command;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import cavern.miner.world.biome.CavernModDimension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ModDimension;

public class DimensionRegeneration
{
	private static final Random RANDOM = new Random();

	public static boolean regenerate(MinecraftServer server, DimensionType dim)
	{
		Path base = Paths.get(".").toAbsolutePath().normalize();
		Path directory = dim.getDirectory(base.toFile()).toPath().toAbsolutePath().normalize();

		if (!directory.startsWith(base))
		{
			return false;
		}

		String folderName = base.relativize(directory).toString();
		ServerWorld world = DimensionManager.getWorld(server, dim, false, false);

		if (world != null && !world.getPlayers().isEmpty())
		{
			return false;
		}

		ServerWorld overworld = DimensionManager.getWorld(server, DimensionType.OVERWORLD, false, false);

		if (overworld == null)
		{
			return false;
		}

		File folder = new File(overworld.getSaveHandler().getWorldDirectory(), folderName);

		if (!folder.exists())
		{
			return false;
		}

		DimensionManager.unloadWorld(world);
		DimensionManager.unloadWorlds(server, false);

		try
		{
			FileUtils.deleteDirectory(folder);
		}
		catch (Exception e)
		{
			return false;
		}

		ModDimension modDim = dim.getModType();

		if (modDim != null && modDim instanceof CavernModDimension)
		{
			((CavernModDimension)modDim).setSeed(RANDOM.nextLong());
		}

		DimensionManager.initWorld(server, dim);

		return true;
	}
}