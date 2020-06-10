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
import cavern.miner.config.json.ItemStackTagListSerializer;
import cavern.miner.util.ItemStackTagList;
import net.minecraft.item.ItemStack;

public class ItemStackTagListConfig
{
	protected final ItemStackTagList list = ItemStackTagList.create();

	protected final File file;
	protected final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public ItemStackTagListConfig(File dir, String name)
	{
		this.file = new File(dir, name + ".json");
	}

	public boolean setEntries(ItemStackTagList entries)
	{
		list.clear();

		return list.addEntries(entries.getEntryList()) && list.addTags(entries.getTagList());
	}

	public ItemStackTagList getEntries()
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

		return gson.toJson(ItemStackTagListSerializer.INSTANCE.serialize(list, ItemStack.class, null));
	}

	public boolean fromJson(Reader json)
	{
		try
		{
			ItemStackTagList entries = ItemStackTagListSerializer.INSTANCE.deserialize(gson.fromJson(json, JsonObject.class), ItemStack.class, null);

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