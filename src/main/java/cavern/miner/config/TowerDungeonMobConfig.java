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
import cavern.miner.config.json.DungeonMobSerializer;
import cavern.miner.world.gen.feature.TowerDungeonSpawner;
import net.minecraft.entity.EntityType;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.DungeonHooks;

public class TowerDungeonMobConfig
{
	private final NonNullList<DungeonHooks.DungeonMob> entries = NonNullList.create();

	private final File file = new File(CavernModConfig.getConfigDir(), "tower_dungeon_mobs.json");
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public boolean setEntries(Collection<DungeonHooks.DungeonMob> collection)
	{
		entries.clear();

		return entries.addAll(collection);
	}

	public NonNullList<DungeonHooks.DungeonMob> getEntries()
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

		for (DungeonHooks.DungeonMob entry : entries)
		{
			JsonElement e = DungeonMobSerializer.INSTANCE.serialize(entry, entry.getClass(), null);

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

				DungeonHooks.DungeonMob entry = DungeonMobSerializer.INSTANCE.deserialize(e, e.getClass(), null);

				if (entry != null && entry.itemWeight > 0)
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
		TowerDungeonSpawner.set(entries);
	}

	public void setDefault()
	{
		entries.clear();
		entries.add(new DungeonHooks.DungeonMob(100, EntityType.ZOMBIE));
		entries.add(new DungeonHooks.DungeonMob(100, EntityType.SKELETON));
		entries.add(new DungeonHooks.DungeonMob(100, EntityType.SPIDER));
		entries.add(new DungeonHooks.DungeonMob(50, EntityType.CAVE_SPIDER));
		entries.add(new DungeonHooks.DungeonMob(20, EntityType.CREEPER));
		entries.add(new DungeonHooks.DungeonMob(20, EntityType.ENDERMAN));
	}
}