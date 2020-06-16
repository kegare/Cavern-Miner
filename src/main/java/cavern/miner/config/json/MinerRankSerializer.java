package cavern.miner.config.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cavern.miner.storage.MinerRank;
import cavern.miner.storage.MinerRank.RankEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class MinerRankSerializer implements JsonSerializer<MinerRank.RankEntry>, JsonDeserializer<MinerRank.RankEntry>
{
	public static final MinerRankSerializer INSTANCE = new MinerRankSerializer();

	@Override
	public JsonElement serialize(RankEntry src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();

		object.addProperty("name", src.getName());
		object.addProperty("translation_key", src.getTranslationKey());
		object.addProperty("phase", src.getPhase());
		object.add("icon_item", JsonHelper.serializeItemStack(src.getIconItem()));

		if (src.getAdvancementKey() != null)
		{
			object.addProperty("advancement", src.getAdvancementKey().toString());
		}

		return object;
	}

	@Override
	public RankEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();

		String name = object.get("name").getAsString();
		String key = object.get("translation_key").getAsString();
		int phase = object.get("phase").getAsInt();
		ItemStack iconItem = JsonHelper.deserializeItemStack(object.get("icon_item").getAsJsonObject());
		ResourceLocation advancement = null;

		if (object.has("advancement"))
		{
			advancement = new ResourceLocation(object.get("advancement").getAsString());
		}

		return new MinerRank.RankEntry(name, key, phase, iconItem, advancement);
	}
}