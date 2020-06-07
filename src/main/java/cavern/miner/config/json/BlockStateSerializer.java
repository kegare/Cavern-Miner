package cavern.miner.config.json;

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.IProperty;
import net.minecraft.state.IStateHolder;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockStateSerializer implements JsonSerializer<BlockState>, JsonDeserializer<BlockState>
{
	public static final BlockStateSerializer INSTANCE = new BlockStateSerializer();

	@Override
	public JsonElement serialize(BlockState src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();

		object.addProperty("name", src.getBlock().getRegistryName().toString());

		JsonObject propsObject = new JsonObject();

		for (Map.Entry<IProperty<?>, Comparable<?>> entry : src.getValues().entrySet())
		{
			Pair<String, String> value = getPropertyString(entry);

			propsObject.addProperty(value.getLeft(), value.getRight());
		}

		if (propsObject.size() > 0)
		{
			object.add("properties", propsObject);
		}

		return object;
	}

	@Override
	public BlockState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();

		String name = object.get("name").getAsString();
		Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));

		if (block == null || block instanceof AirBlock)
		{
			return Blocks.AIR.getDefaultState();
		}

		BlockState state = block.getDefaultState();

		if (object.has("properties"))
		{
			StateContainer<Block, BlockState> stateContainer = block.getStateContainer();
			JsonObject propsObject = object.get("properties").getAsJsonObject();

			for (Map.Entry<String, JsonElement> entry : propsObject.entrySet())
			{
				String key = entry.getKey();
				IProperty<?> prop = stateContainer.getProperty(key);

				if (prop != null)
				{
					String value = entry.getValue().getAsString();

					state = IStateHolder.withString(state, prop, key, value, value);
				}
			}
		}

		return state;
	}

	private Pair<String, String> getPropertyString(Map.Entry<IProperty<?>, Comparable<?>> entry)
	{
		IProperty<?> key = entry.getKey();
		Comparable<?> value = entry.getValue();

		return Pair.of(key.getName(), Util.getValueName(key, value));
	}
}