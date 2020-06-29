package cavern.miner.config.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.entity.EntityType;
import net.minecraftforge.common.DungeonHooks;

public enum DungeonMobSerializer implements JsonSerializer<DungeonHooks.DungeonMob>, JsonDeserializer<DungeonHooks.DungeonMob>
{
	INSTANCE;

	@Override
	public JsonElement serialize(DungeonHooks.DungeonMob src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = JsonHelper.serializeRegistryEntry(src.type);

		object.addProperty("weight", src.itemWeight);

		return object;
	}

	@Override
	public DungeonHooks.DungeonMob deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();

		EntityType<?> type = JsonHelper.deserializeEntityType(object);

		if (type == null)
		{
			return null;
		}

		int weight = object.get("weight").getAsInt();

		return new DungeonHooks.DungeonMob(weight, type);
	}
}