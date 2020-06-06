package cavern.miner.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import cavern.miner.CavernMod;
import cavern.miner.vein.Vein;
import net.minecraft.block.Blocks;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;

public class VeinConfig
{
	private final NonNullList<Vein> veins = NonNullList.create();

	private final File file;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Vein.class, VeinSerializer.INSTANCE).create();

	public VeinConfig(String name)
	{
		this.file = new File(CavernModConfig.getConfigDir(), name + "_veins.json");
	}

	public boolean setVeins(Collection<Vein> entries)
	{
		veins.clear();

		return veins.addAll(entries);
	}

	public NonNullList<Vein> getVeins()
	{
		return veins;
	}

	public File getFile()
	{
		return file;
	}

	public void loadFromFile()
	{
		try
		{
			if (file.getParentFile() != null)
			{
				file.getParentFile().mkdirs();
			}

			if (!file.exists() && !file.createNewFile())
			{
				return;
			}

			if (file.canRead())
			{
				FileInputStream fis = new FileInputStream(file);
				BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));

				fromJson(buffer);

				buffer.close();
				fis.close();
			}
		}
		catch (IOException e)
		{
			CavernMod.LOG.error("Failed to load veins", e);
		}
	}

	public void saveToFile()
	{
		try
		{
			if (file.getParentFile() != null)
			{
				file.getParentFile().mkdirs();
			}

			if (!file.exists() && !file.createNewFile())
			{
				return;
			}

			if (file.canWrite())
			{
				FileOutputStream fos = new FileOutputStream(file);
				BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos));

				buffer.write(Strings.nullToEmpty(toJson()));

				buffer.close();
				fos.close();
			}
		}
		catch (IOException e)
		{
			CavernMod.LOG.error("Failed to save veins", e);
		}
	}

	@Nullable
	public String toJson()
	{
		if (veins.isEmpty())
		{
			return null;
		}

		return gson.toJson(veins);
	}

	public boolean fromJson(Reader json)
	{
		try
		{
			Collection<Vein> entries = gson.fromJson(json, new TypeToken<Collection<Vein>>(){}.getType());

			if (entries == null || entries.isEmpty())
			{
				return false;
			}

			return setVeins(entries);
		}
		catch (Exception e)
		{
			CavernMod.LOG.error("Failed to read from json", e);

			return false;
		}
	}

	public static void createExampleConfig()
	{
		VeinConfig config = new VeinConfig("example");

		config.getVeins().add(new Vein(Blocks.COAL_ORE.getDefaultState(), new Vein.Properties().count(20).size(10)));
		config.getVeins().add(new Vein(Blocks.SAND.getDefaultState(), new Vein.Properties().target(Blocks.DIRT.getDefaultState()).count(30).size(15).min(30)));
		config.getVeins().add(new Vein(Blocks.ACACIA_LOG.getDefaultState().with(RotatedPillarBlock.AXIS, Direction.Axis.Z), new Vein.Properties().target(Blocks.ACACIA_PLANKS.getDefaultState())));

		config.saveToFile();
	}
}