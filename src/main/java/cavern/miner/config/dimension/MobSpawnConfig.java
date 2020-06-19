package cavern.miner.config.dimension;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import cavern.miner.config.AbstractEntryConfig;
import cavern.miner.config.json.SpawnListEntrySerializer;
import cavern.miner.init.CaveEntities;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.Biome;

public class MobSpawnConfig extends AbstractEntryConfig
{
	private final Map<EntityClassification, List<Biome.SpawnListEntry>> spawns = new HashMap<>();

	public MobSpawnConfig(File dir)
	{
		super(new File(dir, "mob_spawns.json"));
	}

	public void setEntries(EntityClassification type, Collection<Biome.SpawnListEntry> entries)
	{
		getEntries(type).addAll(entries);
	}

	public List<Biome.SpawnListEntry> getEntries(EntityClassification type)
	{
		return spawns.computeIfAbsent(type, o -> new ArrayList<>());
	}

	@Override
	public String toJson() throws JsonParseException
	{
		if (spawns.isEmpty())
		{
			return null;
		}

		JsonObject object = new JsonObject();

		for (Map.Entry<EntityClassification, List<Biome.SpawnListEntry>> entry : spawns.entrySet())
		{
			JsonArray array = new JsonArray();

			for (Biome.SpawnListEntry spawnEntry : entry.getValue())
			{
				JsonElement e = SpawnListEntrySerializer.INSTANCE.serialize(spawnEntry, spawnEntry.getClass(), null);

				if (e.isJsonNull() || e.toString().isEmpty())
				{
					continue;
				}

				array.add(e);
			}

			object.add(entry.getKey().name(), array);
		}

		return getGson().toJson(object);
	}

	@Override
	public void fromJson(Reader json) throws JsonParseException
	{
		JsonObject object = getGson().fromJson(json, JsonObject.class);

		if (object.size() == 0)
		{
			return;
		}

		for (Map.Entry<String, JsonElement> entry : object.entrySet())
		{
			EntityClassification type = EntityClassification.valueOf(entry.getKey());

			if (type == null)
			{
				continue;
			}

			JsonArray array = entry.getValue().getAsJsonArray();
			List<Biome.SpawnListEntry> entries = new ArrayList<>();

			for (JsonElement e : array)
			{
				Biome.SpawnListEntry spawnEntry = SpawnListEntrySerializer.INSTANCE.deserialize(e, e.getClass(), null);

				if (spawnEntry != null && spawnEntry.itemWeight > 0)
				{
					entries.add(spawnEntry);
				}
			}

			spawns.put(type, entries);
		}
	}

	public void registerSpawns(Biome biome)
	{
		for (Map.Entry<EntityClassification, List<Biome.SpawnListEntry>> entry : spawns.entrySet())
		{
			List<Biome.SpawnListEntry> list = biome.getSpawns(entry.getKey());

			list.clear();
			list.addAll(entry.getValue());
		}
	}

	public void setDefault()
	{
		spawns.clear();

		List<Biome.SpawnListEntry> entries = new ArrayList<>();

		entries.add(new Biome.SpawnListEntry(EntityType.SPIDER, 100, 4, 4));
		entries.add(new Biome.SpawnListEntry(EntityType.ZOMBIE, 95, 4, 4));
		entries.add(new Biome.SpawnListEntry(EntityType.ZOMBIE_VILLAGER, 15, 1, 1));
		entries.add(new Biome.SpawnListEntry(EntityType.SKELETON, 100, 4, 4));
		entries.add(new Biome.SpawnListEntry(EntityType.CREEPER, 100, 4, 4));
		entries.add(new Biome.SpawnListEntry(EntityType.SLIME, 50, 4, 4));
		entries.add(new Biome.SpawnListEntry(EntityType.ENDERMAN, 10, 1, 4));
		entries.add(new Biome.SpawnListEntry(EntityType.WITCH, 5, 1, 1));

		CaveEntities.CAVENIC_SKELETON.ifPresent(o -> entries.add(new Biome.SpawnListEntry(o, 5, 1, 1)));

		spawns.put(EntityClassification.MONSTER, entries);
		spawns.put(EntityClassification.AMBIENT, Arrays.asList(new Biome.SpawnListEntry(EntityType.BAT, 20, 8, 8)));
	}
}