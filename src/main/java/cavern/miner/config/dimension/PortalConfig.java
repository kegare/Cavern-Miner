package cavern.miner.config.dimension;

import java.io.File;
import java.io.Reader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import cavern.miner.config.AbstractEntryConfig;
import cavern.miner.config.json.BlockStateTagListSerializer;
import cavern.miner.config.json.ItemStackTagListSerializer;
import cavern.miner.util.BlockStateTagList;
import cavern.miner.util.ItemStackTagList;

public class PortalConfig extends AbstractEntryConfig
{
	private final ItemStackTagList triggerItems = ItemStackTagList.create();
	private final BlockStateTagList frameBlocks = BlockStateTagList.create();

	public PortalConfig(File dir)
	{
		super(new File(dir, "portal.json"));
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

	@Override
	public String toJson() throws JsonParseException
	{
		JsonObject object = new JsonObject();

		object.add("trigger_items", ItemStackTagListSerializer.INSTANCE.serialize(triggerItems, triggerItems.getClass(), null));
		object.add("frame_blocks", BlockStateTagListSerializer.INSTANCE.serialize(frameBlocks, frameBlocks.getClass(), null));

		return getGson().toJson(object);
	}

	@Override
	public void fromJson(Reader json) throws JsonParseException
	{
		JsonObject object = getGson().fromJson(json, JsonObject.class);

		JsonElement e = object.get("trigger_items");

		triggerItems.clear();

		if (e != null && e.isJsonObject())
		{
			ItemStackTagList list = ItemStackTagListSerializer.INSTANCE.deserialize(e, e.getClass(), null);

			if (!list.getEntryList().isEmpty())
			{
				triggerItems.addEntries(list.getEntryList());
			}

			if (!list.getTagList().isEmpty())
			{
				triggerItems.addTags(list.getTagList());
			}
		}

		e = object.get("frame_blocks");

		frameBlocks.clear();

		if (e != null && e.isJsonObject())
		{
			BlockStateTagList list = BlockStateTagListSerializer.INSTANCE.deserialize(e, e.getClass(), null);

			if (!list.getEntryList().isEmpty())
			{
				frameBlocks.addEntries(list.getEntryList());
			}

			if (!list.getTagList().isEmpty())
			{
				frameBlocks.addTags(list.getTagList());
			}
		}
	}
}