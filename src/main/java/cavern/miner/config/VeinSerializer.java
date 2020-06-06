package cavern.miner.config;

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

import cavern.miner.util.BlockStateHelper;
import cavern.miner.vein.Vein;
import net.minecraft.block.BlockState;

public class VeinSerializer implements JsonSerializer<Vein>, JsonDeserializer<Vein>
{
	public static final VeinSerializer INSTANCE = new VeinSerializer();

	@Override
	public JsonElement serialize(Vein src, Type typeOfSrc, JsonSerializationContext context)
	{
		final JsonObject object = new JsonObject();

		object.add("block", BlockStateHelper.toJsonTree(src.getBlockState()));

		JsonArray array = new JsonArray();
		src.getTargetBlocks().stream().map(BlockStateHelper::toJsonTree).forEach(array::add);
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
		final JsonObject object = json.getAsJsonObject();

		BlockState state = BlockStateHelper.fromJson(object.get("block").getAsJsonObject());
		Vein.Properties properties = new Vein.Properties();

		JsonArray array = object.get("target_blocks").getAsJsonArray();
		Stream.Builder<BlockState> states = Stream.builder();
		array.forEach(o -> states.add(BlockStateHelper.fromJson(o)));
		properties.target(states.build().toArray(BlockState[]::new));

		properties.count(object.get("count").getAsInt());
		properties.size(object.get("size").getAsInt());

		JsonObject sub = object.get("height").getAsJsonObject();
		properties.min(sub.get("min").getAsInt());
		properties.max(sub.get("max").getAsInt());

		return new Vein(state, properties);
	}
}