package cavern.miner.config.json;

import java.lang.reflect.Type;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cavern.miner.world.vein.Vein;
import net.minecraft.block.BlockState;

public enum VeinSerializer implements JsonSerializer<Vein>, JsonDeserializer<Vein>
{
	INSTANCE;

	@Override
	public JsonElement serialize(Vein src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();

		object.add("block", JsonHelper.serializeBlockState(src.getBlockState()));

		JsonArray array = new JsonArray();
		src.getTargetBlocks().stream().map(JsonHelper::serializeBlockState).forEach(array::add);
		object.add("target_blocks", array);

		object.addProperty("count", src.getCount());
		object.addProperty("size", src.getSize());

		JsonObject sub = new JsonObject();
		sub.addProperty("min", src.getMinHeight());
		sub.addProperty("max", src.getMaxHeight());
		object.add("height", sub);

		return object;
	}

	@Override
	public Vein deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();

		BlockState state = JsonHelper.deserializeBlockState(object.get("block").getAsJsonObject());
		Vein.Properties properties = new Vein.Properties();

		if (object.has("target_blocks"))
		{
			JsonArray array = object.get("target_blocks").getAsJsonArray();
			Stream.Builder<BlockState> states = Stream.builder();

			array.forEach(o -> states.add(JsonHelper.deserializeBlockState((JsonObject)o)));

			properties.target(states.build().toArray(BlockState[]::new));
		}

		if (object.has("count"))
		{
			properties.count(object.get("count").getAsInt());
		}

		if (object.has("size"))
		{
			properties.size(object.get("size").getAsInt());
		}

		if (object.has("height"))
		{
			JsonObject sub = object.get("height").getAsJsonObject();

			if (sub.has("min"))
			{
				properties.min(sub.get("min").getAsInt());
			}

			if (sub.has("max"))
			{
				properties.max(sub.get("max").getAsInt());
			}
		}

		return new Vein(state, properties);
	}
}