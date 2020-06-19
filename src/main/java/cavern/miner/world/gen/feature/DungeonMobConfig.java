package cavern.miner.world.gen.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

import cavern.miner.CavernMod;
import cavern.miner.config.json.DungeonMobSerializer;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraftforge.common.DungeonHooks;

public class DungeonMobConfig implements IFeatureConfig
{
	private final List<DungeonHooks.DungeonMob> spawns = new ArrayList<>();
	private final Gson gson = new Gson();

	public List<DungeonHooks.DungeonMob> getSpawns()
	{
		return spawns;
	}

	private <T> Stream<T> toJsonStream(DynamicOps<T> ops)
	{
		return spawns.stream().map(o ->
		{
			try
			{
				JsonElement e = DungeonMobSerializer.INSTANCE.serialize(o, o.getClass(), null);

				if (e.isJsonNull() || e.toString().isEmpty())
				{
					return ops.empty();
				}

				return ops.createString(gson.toJson(e));
			}
			catch (JsonParseException e)
			{
				CavernMod.LOG.warn("Failed to serialize dungeon mob entry", e);

				return ops.empty();
			}
		});
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> ops)
	{
		return new Dynamic<>(ops, spawns.isEmpty() ? ops.emptyList() : ops.createList(toJsonStream(ops)));
	}

	public static <T> DungeonMobConfig deserialize(Dynamic<T> data)
	{
		DungeonMobConfig result = new DungeonMobConfig();
		List<String> entries = data.asList(o -> o.asString(""));

		for (String json : entries)
		{
			try
			{
				if (json.isEmpty())
				{
					continue;
				}

				JsonElement e = result.gson.fromJson(json, JsonElement.class);
				DungeonHooks.DungeonMob entry = DungeonMobSerializer.INSTANCE.deserialize(e, e.getClass(), null);

				if (entry != null)
				{
					result.spawns.add(entry);
				}
			}
			catch (JsonParseException e)
			{
				CavernMod.LOG.warn("Failed to deserialize dungeon mob entry", e);
			}
		}

		return result;
	}
}