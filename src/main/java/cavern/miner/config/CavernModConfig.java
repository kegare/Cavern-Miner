package cavern.miner.config;

import java.io.File;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

public class CavernModConfig
{
	public static File getConfigDir()
	{
		return new File(FMLPaths.CONFIGDIR.get().toFile(), "cavern_miner");
	}

	public static void register(ModLoadingContext context)
	{
		File dir = getConfigDir();

		if (!dir.exists())
		{
			dir.mkdirs();
		}

		context.registerConfig(ModConfig.Type.COMMON, MiningConfig.SPEC, "cavern_miner/mining.toml");
	}
}