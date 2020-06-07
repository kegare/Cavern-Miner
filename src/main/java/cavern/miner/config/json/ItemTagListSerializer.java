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
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemTagListSerializer implements JsonSerializer<EntryTagList<Item>>, JsonDeserializer<EntryTagList<Item>>
{
	public static final ItemTagListSerializer INSTANCE = new ItemTagListSerializer();

	@Override
	public JsonElement serialize(EntryTagList<Item> src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();

		JsonArray array = new JsonArray();

		for (Item item : src.getEntryList())
		{
			array.add(item.getRegistryName().toString());
		}

		object.add("items", array);

		array = new JsonArray();

		for (Tag<Item> tag : src.getTagList())
		{
			array.add(tag.getId().toString());
		}

		object.add("tags", array);

		return object;
	}

	@Override
	public EntryTagList<Item> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();
		EntryTagList<Item> list = EntryTagList.create();

		JsonArray array = object.get("items").getAsJsonArray();

		for (JsonElement e : array)
		{
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(e.getAsString()));

			if (item != null)
			{
				list.add(item);
			}
		}

		array = object.get("tags").getAsJsonArray();

		for (JsonElement e : array)
		{
			list.add(new ItemTags.Wrapper(new ResourceLocation(e.getAsString())));
		}

		return list;
	}
}