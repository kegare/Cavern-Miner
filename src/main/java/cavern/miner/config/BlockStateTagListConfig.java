package cavern.miner.config;

import java.io.File;
import java.io.Reader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import cavern.miner.config.json.BlockStateTagListSerializer;
import cavern.miner.util.BlockStateTagList;

public class BlockStateTagListConfig extends AbstractEntryConfig
{
	protected final BlockStateTagList list = BlockStateTagList.create();

	public BlockStateTagListConfig(File dir, String name)
	{
		super(new File(dir, name + ".json"));
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

	@Override
	public String toJson() throws JsonParseException
	{
		if (list.isEmpty())
		{
			return null;
		}

		return getGson().toJson(BlockStateTagListSerializer.INSTANCE.serialize(list, list.getClass(), null));
	}

	@Override
	public void fromJson(Reader json) throws JsonParseException
	{
		JsonObject object = getGson().fromJson(json, JsonObject.class);

		if (object.size() == 0)
		{
			return;
		}

		BlockStateTagList entries = BlockStateTagListSerializer.INSTANCE.deserialize(object, object.getClass(), null);

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