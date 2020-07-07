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

import cavern.miner.util.ItemStackTagList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public enum ItemStackTagListSerializer implements JsonSerializer<ItemStackTagList>, JsonDeserializer<ItemStackTagList>
{
	INSTANCE;

	@Override
	public JsonElement serialize(ItemStackTagList src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();

		JsonArray array = new JsonArray();

		for (ItemStack stack : src.getEntryList())
		{
			array.add(JsonHelper.serializeItemStack(stack));
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
	public ItemStackTagList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();
		ItemStackTagList list = new ItemStackTagList();

		JsonArray array = object.get("items").getAsJsonArray();

		for (JsonElement e : array)
		{
			if (e.isJsonObject())
			{
				ItemStack stack = JsonHelper.deserializeItemStack(e.getAsJsonObject());

				if (!stack.isEmpty())
				{
					list.add(stack);
				}
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