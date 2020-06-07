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
import com.google.gson.JsonElement;

import cavern.miner.CavernMod;
import cavern.miner.config.json.ItemStackTagListSerializer;
import cavern.miner.util.ItemStackTagList;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Tags;

public class PortalTriggerConfig
{
	private final ItemStackTagList list = ItemStackTagList.create();

	private final File file;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public PortalTriggerConfig(File dir, String name)
	{
		this.file = new File(dir, name + "_portal_triggers.json");
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
			CavernMod.LOG.error("Failed to load portal trigger", e);
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
			CavernMod.LOG.error("Failed to save portal trigger", e);
		}
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
			ItemStackTagList entries = ItemStackTagListSerializer.INSTANCE.deserialize(gson.fromJson(json, JsonElement.class), ItemStack.class, null);

			if (entries == null || entries.isEmpty())
			{
				return false;
			}

			return setEntries(entries);
		}
		catch (Exception e)
		{
			CavernMod.LOG.error("Failed to read from json", e);

			return false;
		}
	}

	public void setDefault()
	{
		list.clear();
		list.add(Tags.Items.GEMS_EMERALD);
	}
}