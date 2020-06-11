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

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import cavern.miner.CavernMod;
import cavern.miner.config.json.BlockStateTagListSerializer;
import cavern.miner.config.json.ItemStackTagListSerializer;
import cavern.miner.util.BlockStateTagList;
import cavern.miner.util.ItemStackTagList;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

public class PortalConfig
{
	private final ItemStackTagList triggerItems = ItemStackTagList.create();
	private final BlockStateTagList frameBlocks = BlockStateTagList.create();

	private final File file;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public PortalConfig(File dir)
	{
		this.file = new File(dir, "portal.json");
	}

	public boolean setTriggerItems(ItemStackTagList entries)
	{
		triggerItems.clear();

		return triggerItems.addEntries(entries.getEntryList()) && triggerItems.addTags(entries.getTagList());
	}

	public ItemStackTagList getTriggerItems()
	{
		return triggerItems;
	}

	public boolean setFrameBlocks(BlockStateTagList entries)
	{
		frameBlocks.clear();

		return frameBlocks.addEntries(entries.getEntryList()) && frameBlocks.addTags(entries.getTagList());
	}

	public BlockStateTagList getFrameBlocks()
	{
		return frameBlocks;
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

	public String toJson()
	{
		JsonObject object = new JsonObject();

		object.add("trigger_items", ItemStackTagListSerializer.INSTANCE.serialize(getTriggerItems(), ItemStack.class, null));
		object.add("frame_blocks", BlockStateTagListSerializer.INSTANCE.serialize(getFrameBlocks(), BlockState.class, null));

		return gson.toJson(object);
	}

	public void fromJson(Reader json)
	{
		try
		{
			JsonObject object = gson.fromJson(json, JsonObject.class);

			setTriggerItems(ItemStackTagListSerializer.INSTANCE.deserialize(object.get("trigger_items"), ItemStack.class, null));
			setFrameBlocks(BlockStateTagListSerializer.INSTANCE.deserialize(object.get("frame_blocks"), BlockState.class, null));
		}
		catch (JsonParseException e)
		{
			CavernMod.LOG.error("Failed to read from json", e);
		}
	}
}