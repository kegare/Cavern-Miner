package cavern.miner.config.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;

public enum EffectInstanceSerializer implements JsonSerializer<EffectInstance>, JsonDeserializer<EffectInstance>
{
	INSTANCE;

	@Override
	public JsonElement serialize(EffectInstance src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = JsonHelper.serializeRegistryEntry(src.getPotion());

		object.addProperty("duration", src.getDuration());
		object.addProperty("amplifer", src.getAmplifier());

		return object;
	}

	@Override
	public EffectInstance deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();

		Effect effect = JsonHelper.deserializeEffect(object);

		if (effect == null)
		{
			return null;
		}

		int duration;

		if (object.has("duration"))
		{
			duration = object.get("duration").getAsInt();
		}
		else
		{
			duration = 200;
		}

		int amplifer;

		if (object.has("amplifer"))
		{
			amplifer = object.get("amplifer").getAsInt();
		}
		else
		{
			amplifer = 0;
		}

		return new EffectInstance(effect, duration, amplifer);
	}
}