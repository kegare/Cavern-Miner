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
import com.google.gson.JsonParseException;

import cavern.miner.CavernMod;
import cavern.miner.block.RandomiteDrop;
import cavern.miner.config.json.RandomiteDropSerializer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.Tags;

public class RandomiteDropConfig
{
	private final NonNullList<RandomiteDrop.DropEntry> entries = NonNullList.create();

	private final File file = new File(CavernModConfig.getConfigDir(), "randomite_drops.json");
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public boolean setEntries(Collection<RandomiteDrop.DropEntry> collection)
	{
		entries.clear();

		return entries.addAll(collection);
	}

	public NonNullList<RandomiteDrop.DropEntry> getEntries()
	{
		return entries;
	}

	public File getFile()
	{
		return file;
	}

	public boolean loadFromFile()
	{
		try
		{
			if (file.getParentFile() != null)
			{
				file.getParentFile().mkdirs();
			}

			if (!file.exists() && !file.createNewFile())
			{
				return false;
			}

			if (file.canRead() && file.length() > 0L)
			{
				FileInputStream fis = new FileInputStream(file);
				BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));

				fromJson(buffer);

				buffer.close();
				fis.close();

				return true;
			}
		}
		catch (IOException e)
		{
			CavernMod.LOG.error("Failed to load {}", file.getName(), e);
		}

		return false;
	}

	public boolean saveToFile()
	{
		try
		{
			if (file.getParentFile() != null)
			{
				file.getParentFile().mkdirs();
			}

			if (!file.exists() && !file.createNewFile())
			{
				return false;
			}

			if (file.canWrite())
			{
				FileOutputStream fos = new FileOutputStream(file);
				BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos));

				buffer.write(Strings.nullToEmpty(toJson()));

				buffer.close();
				fos.close();

				return true;
			}
		}
		catch (IOException e)
		{
			CavernMod.LOG.error("Failed to save {}", file.getName(), e);
		}

		return false;
	}

	@Nullable
	public String toJson()
	{
		if (entries.isEmpty())
		{
			return null;
		}

		JsonArray array = new JsonArray();

		for (RandomiteDrop.DropEntry entry : entries)
		{
			JsonElement e = RandomiteDropSerializer.INSTANCE.serialize(entry, entry.getClass(), null);

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

			entries.clear();

			for (JsonElement e : array)
			{
				if (e.isJsonNull() || !e.isJsonObject() || e.toString().isEmpty())
				{
					continue;
				}

				RandomiteDrop.DropEntry entry = RandomiteDropSerializer.INSTANCE.deserialize(e, e.getClass(), null);

				if (entry != RandomiteDrop.EMPTY)
				{
					entries.add(entry);
				}
			}
		}
		catch (JsonParseException e)
		{
			CavernMod.LOG.error("Failed to read from json", e);
		}
	}

	public void registerEntries()
	{
		RandomiteDrop.set(entries);
	}

	public void setDefault()
	{
		entries.clear();
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.INGOTS, 20, 1, 3));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.NUGGETS, 20, 1, 3));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.GEMS, 10, 1, 2));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.DUSTS, 15, 3, 5));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.RODS, 15, 1, 3));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.ENDER_PEARLS, 15, 1, 3));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.BONES, 20, 2, 3));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.GUNPOWDER, 20, 2, 5));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.STRING, 20, 2, 5));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.SEEDS, 20, 3, 5));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.CROPS, 20, 3, 5));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.DYES, 10, 2, 5));
	}
}