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

import cavern.miner.util.EntryTagList;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockTagListSerializer implements JsonSerializer<EntryTagList<Block>>, JsonDeserializer<EntryTagList<Block>>
{
	public static final BlockTagListSerializer INSTANCE = new BlockTagListSerializer();

	@Override
	public JsonElement serialize(EntryTagList<Block> src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();

		JsonArray array = new JsonArray();

		for (Block block : src.getEntryList())
		{
			array.add(block.getRegistryName().toString());
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
	public EntryTagList<Block> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();
		EntryTagList<Block> list = EntryTagList.create();

		JsonArray array = object.get("blocks").getAsJsonArray();

		for (JsonElement e : array)
		{
			Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(e.getAsString()));

			if (block != null && !(block instanceof AirBlock))
			{
				list.add(block);
			}
		}

		array = object.get("tags").getAsJsonArray();

		for (JsonElement e : array)
		{
			list.add(JsonHelper.deserializeBlockTag(e.getAsJsonObject()));
		}

		return list;
	}
}