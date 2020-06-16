package cavern.miner.config;

import java.io.File;

import cavern.miner.config.client.ClientConfig;
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
		File[] configDirs = {getConfigDir(), CavernConfig.getConfigDir(), HugeCavernConfig.getConfigDir()};

		for (File dir : configDirs)
		{
			if (!dir.exists()) dir.mkdirs();
		}

		context.registerConfig(ModConfig.Type.COMMON, GeneralConfig.SPEC, "cavern_miner/general.toml");
		context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "cavern_miner/client.toml");

		context.registerConfig(ModConfig.Type.COMMON, CavernConfig.SPEC, "cavern_miner/cavern/dimension.toml");
		context.registerConfig(ModConfig.Type.COMMON, HugeCavernConfig.SPEC, "cavern_miner/huge_cavern/dimension.toml");
	}
}