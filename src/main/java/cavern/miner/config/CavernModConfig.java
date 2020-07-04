package cavern.miner.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Strings;

import cavern.miner.CavernMod;
import cavern.miner.config.client.ClientConfig;
import cavern.miner.config.dimension.CavernConfig;
import cavern.miner.config.dimension.HugeCavernConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

public class CavernModConfig
{
	private static final String INTERNAL_VERSION = "2";

	public static File getConfigDir()
	{
		return new File(FMLPaths.CONFIGDIR.get().toFile(), "cavern_miner");
	}

	private static void makeDirs()
	{
		final File[] configDirs = {getConfigDir(), CavernConfig.getConfigDir(), HugeCavernConfig.getConfigDir()};

		for (File dir : configDirs)
		{
			if (dir.getParentFile() != null)
			{
				dir.getParentFile().mkdirs();
			}

			if (!dir.exists())
			{
				dir.mkdirs();
			}
		}
	}

	public static void check()
	{
		makeDirs();

		try
		{
			final File dir = getConfigDir();
			final File file = new File(dir, ".version");

			String line = null;

			if (file.exists() && file.canRead() && file.length() > 0L)
			{
				try (FileInputStream fis = new FileInputStream(file); BufferedReader buffer = new BufferedReader(new InputStreamReader(fis)))
				{
					while (line == null)
					{
						line = buffer.readLine();
					}
				}
			}

			if (INTERNAL_VERSION.equals(Strings.nullToEmpty(line)))
			{
				return;
			}

			if (line != null && line.length() > 0)
			{
				File bakDir = new File(dir.getParent(), dir.getName() + "_bak");

				if (bakDir.exists())
				{
					FileUtils.deleteDirectory(bakDir);
				}

				FileUtils.moveDirectory(dir, bakDir);

				makeDirs();
			}

			if (!file.exists() && !file.createNewFile())
			{
				return;
			}

			if (file.canWrite())
			{
				try (FileOutputStream fos = new FileOutputStream(file); BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos)))
				{
					buffer.write(INTERNAL_VERSION);
				}
			}
		}
		catch (IOException e)
		{
			CavernMod.LOG.error("An error occurred while checking config internal version", e);
		}
	}

	public static void register(final ModLoadingContext context)
	{
		context.registerConfig(ModConfig.Type.COMMON, GeneralConfig.SPEC, "cavern_miner/general.toml");
		context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "cavern_miner/client.toml");

		context.registerConfig(ModConfig.Type.COMMON, CavernConfig.SPEC, "cavern_miner/cavern/dimension.toml");
		context.registerConfig(ModConfig.Type.COMMON, HugeCavernConfig.SPEC, "cavern_miner/huge_cavern/dimension.toml");
	}
}