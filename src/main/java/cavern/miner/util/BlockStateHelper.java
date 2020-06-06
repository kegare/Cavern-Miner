package cavern.miner.util;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.block.BlockState;

public class BlockStateHelper
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(BlockState.class, BlockStateSerializer.INSTANCE).create();

	public static String toJson(BlockState state)
	{
		return GSON.toJson(state);
	}

	public static JsonElement toJsonTree(BlockState state)
	{
		return GSON.toJsonTree(state);
	}

	public static BlockState fromJson(String value)
	{
		return GSON.fromJson(value, BlockState.class);
	}

	public static BlockState fromJson(JsonElement value)
	{
		return GSON.fromJson(value, BlockState.class);
	}

	public static boolean equals(@Nullable BlockState o1, @Nullable BlockState o2)
	{
		if (o1 == null || o2 == null)
		{
			return false;
		}

		if (o1.getBlock() != o2.getBlock())
		{
			return false;
		}

		return o1.getValues().equals(o2.getValues());
	}
}