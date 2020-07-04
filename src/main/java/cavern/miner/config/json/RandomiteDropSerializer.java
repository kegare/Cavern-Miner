package cavern.miner.config.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cavern.miner.block.RandomiteDrop;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public enum RandomiteDropSerializer implements JsonSerializer<RandomiteDrop.DropEntry>, JsonDeserializer<RandomiteDrop.DropEntry>
{
	INSTANCE;

	@Override
	public JsonElement serialize(RandomiteDrop.DropEntry src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();

		if (src instanceof RandomiteDrop.ItemEntry)
		{
			object.add("item", JsonHelper.serializeRegistryEntry(((RandomiteDrop.ItemEntry)src).getItem()));
		}
		else if (src instanceof RandomiteDrop.ItemStackEntry)
		{
			object.add("item", JsonHelper.serializeItemStack(((RandomiteDrop.ItemStackEntry)src).getItemStack()));
		}
		else if (src instanceof RandomiteDrop.TagEntry)
		{
			object.addProperty("tag", ((RandomiteDrop.TagEntry)src).getTag().getId().toString());
		}
		else
		{
			return JsonNull.INSTANCE;
		}

		object.addProperty("weight", src.itemWeight);

		if (src.getMinCount() == src.getMaxCount())
		{
			object.addProperty("count", src.getMinCount());
		}
		else
		{
			JsonObject o = new JsonObject();

			o.addProperty("min", src.getMinCount());
			o.addProperty("max", src.getMaxCount());

			object.add("count", o);
		}

		return object;
	}

	@Override
	public RandomiteDrop.DropEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();

		int weight = 0;

		if (object.has("weight"))
		{
			weight = object.get("weight").getAsInt();
		}

		if (weight <= 0)
		{
			return RandomiteDrop.EMPTY;
		}

		int min = 0;
		int max = 0;

		if (object.has("count"))
		{
			JsonElement e = object.get("count");

			if (e.isJsonPrimitive())
			{
				min = max = e.getAsInt();
			}
			else if (e.isJsonObject())
			{
				JsonObject o = e.getAsJsonObject();

				if (o.has("min"))
				{
					min = o.get("min").getAsInt();
				}

				if (o.has("max"))
				{
					min = o.get("max").getAsInt();
				}
			}
		}

		if (min <= 0 && max <= 0)
		{
			return RandomiteDrop.EMPTY;
		}

		if (object.has("item"))
		{
			JsonObject o = object.get("item").getAsJsonObject();

			if (o.has("nbt"))
			{
				ItemStack stack = JsonHelper.deserializeItemStack(o);

				if (stack.isEmpty())
				{
					return RandomiteDrop.EMPTY;
				}

				stack.setCount(1);

				return new RandomiteDrop.ItemStackEntry(stack, weight, min, max);
			}

			Item item = JsonHelper.deserializeItem(o);

			if (item == Items.AIR)
			{
				return RandomiteDrop.EMPTY;
			}

			return new RandomiteDrop.ItemEntry(item, weight, min, max);
		}

		if (object.has("tag"))
		{
			return new RandomiteDrop.TagEntry(JsonHelper.deserializeItemTag(object.get("tag")), weight, min, max);
		}

		return RandomiteDrop.EMPTY;
	}
}