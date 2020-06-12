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
		File dir = getConfigDir();

		if (!dir.exists())
		{
			dir.mkdirs();
		}

		context.registerConfig(ModConfig.Type.COMMON, GeneralConfig.SPEC, "cavern_miner/general.toml");
		context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "cavern_miner/client.toml");

		dir = CavernConfig.getConfigDir();

		if (!dir.exists())
		{
			dir.mkdirs();
		}

		context.registerConfig(ModConfig.Type.COMMON, CavernConfig.SPEC, "cavern_miner/cavern/dimension.toml");
	}
}