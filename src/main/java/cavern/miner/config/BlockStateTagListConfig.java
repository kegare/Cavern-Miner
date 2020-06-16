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

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import cavern.miner.CavernMod;
import cavern.miner.config.json.BlockStateTagListSerializer;
import cavern.miner.util.BlockStateTagList;

public class BlockStateTagListConfig
{
	protected final BlockStateTagList list = BlockStateTagList.create();

	protected final File file;
	protected final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public BlockStateTagListConfig(File dir, String name)
	{
		this.file = new File(dir, name + ".json");
	}

	public boolean setEntries(BlockStateTagList entries)
	{
		list.clear();

		return list.addEntries(entries.getEntryList()) && list.addTags(entries.getTagList());
	}

	public BlockStateTagList getEntries()
	{
		return list;
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
		if (list.isEmpty())
		{
			return null;
		}

		return gson.toJson(BlockStateTagListSerializer.INSTANCE.serialize(list, list.getClass(), null));
	}

	public boolean fromJson(Reader json)
	{
		try
		{
			JsonObject object = gson.fromJson(json, JsonObject.class);

			if (object.size() == 0)
			{
				return false;
			}

			BlockStateTagList entries = BlockStateTagListSerializer.INSTANCE.deserialize(object, object.getClass(), null);

			if (entries == null || entries.isEmpty())
			{
				return false;
			}

			return setEntries(entries);
		}
		catch (JsonParseException e)
		{
			CavernMod.LOG.error("Failed to read from json", e);

			return false;
		}
	}

	public void setDefault()
	{
		list.clear();
	}
}