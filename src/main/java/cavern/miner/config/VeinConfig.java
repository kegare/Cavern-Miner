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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import cavern.miner.CavernMod;
import cavern.miner.config.json.VeinSerializer;
import cavern.miner.vein.Vein;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;

public class VeinConfig
{
	private final NonNullList<Vein> veins = NonNullList.create();

	private final File file;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public VeinConfig(File dir, String name)
	{
		this.file = new File(dir, name + "_veins.json");
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

			if (file.canRead() && file.length() > 0L)
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

		JsonArray array = new JsonArray();

		for (Vein vein : veins)
		{
			JsonElement e = VeinSerializer.INSTANCE.serialize(vein, vein.getClass(), null);

			if (e.isJsonNull() || e.toString().isEmpty())
			{
				continue;
			}

			array.add(e);
		}

		return gson.toJson(array);
	}

	public void fromJson(Reader json)
	{
		try
		{
			JsonArray array = gson.fromJson(json, JsonArray.class);

			if (array.size() == 0)
			{
				return;
			}

			veins.clear();

			for (JsonElement e : array)
			{
				if (e.isJsonNull() || e.toString().isEmpty())
				{
					continue;
				}

				Vein vein = VeinSerializer.INSTANCE.deserialize(e, Vein.class, null);

				if (vein.getBlockState().getBlock() instanceof AirBlock)
				{
					continue;
				}

				if (vein.getCount() <= 0 || vein.getSize() <= 0)
				{
					continue;
				}

				veins.add(vein);
			}
		}
		catch (Exception e)
		{
			CavernMod.LOG.error("Failed to read from json", e);
		}
	}

	public static void createExampleConfig()
	{
		VeinConfig config = new VeinConfig(CavernModConfig.getConfigDir(), "example");

		config.getVeins().add(new Vein(Blocks.COAL_ORE.getDefaultState(), new Vein.Properties().count(20).size(10)));
		config.getVeins().add(new Vein(Blocks.SAND.getDefaultState(), new Vein.Properties().target(Blocks.DIRT.getDefaultState()).count(30).size(15).min(30)));
		config.getVeins().add(new Vein(Blocks.ACACIA_LOG.getDefaultState().with(RotatedPillarBlock.AXIS, Direction.Axis.Z), new Vein.Properties().target(Blocks.ACACIA_PLANKS.getDefaultState())));

		config.saveToFile();
	}
}