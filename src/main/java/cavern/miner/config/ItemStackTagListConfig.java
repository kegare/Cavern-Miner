package cavern.miner.config;

import java.io.File;
import java.io.Reader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import cavern.miner.config.json.ItemStackTagListSerializer;
import cavern.miner.util.ItemStackTagList;

public class ItemStackTagListConfig extends AbstractEntryConfig
{
	protected final ItemStackTagList list = ItemStackTagList.create();

	public ItemStackTagListConfig(File dir, String name)
	{
		super(new File(dir, name + ".json"));
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

	@Override
	public String toJson() throws JsonParseException
	{
		if (list.isEmpty())
		{
			return null;
		}

		return getGson().toJson(ItemStackTagListSerializer.INSTANCE.serialize(list, list.getClass(), null));
	}

	@Override
	public void fromJson(Reader json) throws JsonParseException
	{
		JsonObject object = getGson().fromJson(json, JsonObject.class);

		if (object.size() == 0)
		{
			return;
		}

		ItemStackTagList entries = ItemStackTagListSerializer.INSTANCE.deserialize(object, object.getClass(), null);

		if (entries == null || entries.isEmpty())
		{
			return;
		}

		setEntries(entries);
	}

	public void setDefault()
	{
		list.clear();
	}
}