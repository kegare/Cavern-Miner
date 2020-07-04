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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

public enum SpawnListEntrySerializer implements JsonSerializer<Biome.SpawnListEntry>, JsonDeserializer<Biome.SpawnListEntry>
{
	INSTANCE;

	@Override
	public JsonElement serialize(Biome.SpawnListEntry src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();

		object.addProperty("name", src.entityType.getRegistryName().toString());
		object.addProperty("weight", src.itemWeight);

		JsonObject group = new JsonObject();

		group.addProperty("min", src.minGroupCount);
		group.addProperty("max", src.maxGroupCount);
		object.add("group_count", group);

		return object;
	}

	@Override
	public Biome.SpawnListEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();

		EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(object.get("name").getAsString()));

		if (entityType == null)
		{
			return null;
		}

		int weight = object.get("weight").getAsInt();

		JsonObject group = object.get("group_count").getAsJsonObject();
		int min = group.get("min").getAsInt();
		int max = group.get("max").getAsInt();

		return new Biome.SpawnListEntry(entityType, weight, min, max);
	}
}