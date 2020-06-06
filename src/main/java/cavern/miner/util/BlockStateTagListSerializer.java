package cavern.miner.util;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class BlockStateTagListSerializer implements JsonSerializer<BlockStateTagList>, JsonDeserializer<BlockStateTagList>
{
	public static final BlockStateTagListSerializer INSTANCE = new BlockStateTagListSerializer();

	@Override
	public JsonElement serialize(BlockStateTagList src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();

		JsonArray array = new JsonArray();

		for (BlockState state : src.getEntryList())
		{
			array.add(BlockStateHelper.toJsonTree(state));
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
		BlockStateTagList list = BlockStateTagList.create();

		JsonArray array = object.get("blocks").getAsJsonArray();

		for (JsonElement e : array)
		{
			BlockState state = BlockStateHelper.fromJson(e);

			if (state.getBlock() != Blocks.AIR)
			{
				list.add(state);
			}
		}

		array = object.get("tags").getAsJsonArray();

		for (JsonElement e : array)
		{
			list.add(new BlockTags.Wrapper(new ResourceLocation(e.getAsString())));
		}

		return list;
	}
}