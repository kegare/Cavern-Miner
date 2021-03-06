package cavern.miner.config.json;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cavern.miner.util.BlockStateTagList;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.Tag;

public enum BlockStateTagListSerializer implements JsonSerializer<BlockStateTagList>, JsonDeserializer<BlockStateTagList>
{
	INSTANCE;

	@Override
	public JsonElement serialize(BlockStateTagList src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();

		JsonArray array = new JsonArray();

		for (BlockState state : src.getEntryList())
		{
			array.add(JsonHelper.serializeBlockState(state));
		}

		object.add("blocks", array);

		array = new JsonArray();

		for (Tag<Block> tag : src.getTagList())
		{
			array.add(tag.getId().toString());
		}

		object.add("tags", array);

		return object;
	}

	@Override
	public BlockStateTagList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();
		BlockStateTagList list = new BlockStateTagList();

		JsonArray array = object.get("blocks").getAsJsonArray();

		for (JsonElement e : array)
		{
			if (e.isJsonObject())
			{
				BlockState state = JsonHelper.deserializeBlockState(e.getAsJsonObject());

				if (!(state.getBlock() instanceof AirBlock))
				{
					list.add(state);
				}
			}
		}

		array = object.get("tags").getAsJsonArray();

		for (JsonElement e : array)
		{
			list.add(JsonHelper.deserializeBlockTag(e));
		}

		return list;
	}
}