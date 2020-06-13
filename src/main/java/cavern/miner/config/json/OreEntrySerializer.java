package cavern.miner.config.json;

import java.lang.reflect.Type;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cavern.miner.world.vein.OreRegistry;
import cavern.miner.world.vein.VeinProvider;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class OreEntrySerializer implements JsonSerializer<OreRegistry.OreEntry>, JsonDeserializer<OreRegistry.OreEntry>
{
	public static final OreEntrySerializer INSTANCE = new OreEntrySerializer();

	@Override
	public JsonElement serialize(OreRegistry.OreEntry src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();
		JsonObject entry = serializeEntry(src);

		if (entry == null)
		{
			return JsonNull.INSTANCE;
		}

		object.add("entry", entry);

		entry = src.getParent().map(o -> serializeEntry(OreRegistry.getEntry(o, true))).orElse(null);

		if (entry != null)
		{
			object.add("parent", entry);

			return object;
		}

		src.getRarity().ifPresent(o -> object.addProperty("rarity", o.toString().toLowerCase()));

		src.getPoint().ifPresent(o -> object.addProperty("point", o));

		return object;
	}

	@Override
	public OreRegistry.OreEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();

		OreRegistry.OreEntry parent = null;
		VeinProvider.Rarity rarity = null;
		Integer point = null;

		if (object.has("parent"))
		{
			parent = deserializeEntry(object.get("parent").getAsJsonObject(), rarity, point);
		}

		if (parent == null)
		{
			if (object.has("rarity"))
			{
				rarity = VeinProvider.Rarity.valueOf(object.get("rarity").getAsString().toUpperCase());
			}

			if (object.has("point"))
			{
				point = object.get("point").getAsInt();
			}
		}

		return ObjectUtils.defaultIfNull(deserializeEntry(object.get("entry").getAsJsonObject(), rarity, point), OreRegistry.OreEntry.EMPTY);
	}

	@Nullable
	private JsonObject serializeEntry(OreRegistry.OreEntry entry)
	{
		if (entry == OreRegistry.OreEntry.EMPTY)
		{
			return null;
		}

		if (entry instanceof OreRegistry.BlockEntry)
		{
			return JsonHelper.serializeRegistryEntry(((OreRegistry.BlockEntry)entry).getBlock());
		}

		if (entry instanceof OreRegistry.BlockStateEntry)
		{
			return JsonHelper.serializeBlockState(((OreRegistry.BlockStateEntry)entry).getBlockState());
		}

		if (entry instanceof OreRegistry.TagEntry)
		{
			return JsonHelper.serializeTag(((OreRegistry.TagEntry)entry).getTag());
		}

		return null;
	}

	@Nullable
	private OreRegistry.OreEntry deserializeEntry(JsonObject object, VeinProvider.Rarity rarity, Integer point)
	{
		if (object.has("name"))
		{
			if (object.has("properties"))
			{
				BlockState state = JsonHelper.deserializeBlockState(object);

				if (state.getBlock() instanceof AirBlock)
				{
					return null;
				}

				return new OreRegistry.BlockStateEntry(state, rarity, point);
			}

			Block block = JsonHelper.deserializeBlock(object);

			if (block instanceof AirBlock)
			{
				return null;
			}

			return new OreRegistry.BlockEntry(block, rarity, point);
		}

		if (object.has("tag"))
		{
			return new OreRegistry.TagEntry(JsonHelper.deserializeBlockTag(object), rarity, point);
		}

		return null;
	}
}