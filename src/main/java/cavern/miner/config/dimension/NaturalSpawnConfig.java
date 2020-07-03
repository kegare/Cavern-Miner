package cavern.miner.config.dimension;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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

public class NaturalSpawnConfig extends AbstractEntryConfig
{
	private final Map<EntityClassification, List<Biome.SpawnListEntry>> spawns = new HashMap<>();
	private final Supplier<List<Biome>> biomes;

	public NaturalSpawnConfig(File dir, Supplier<List<Biome>> biomes)
	{
		super(new File(dir, "natural_spawns.json"));
		this.biomes = biomes;
	}

	public List<Biome.SpawnListEntry> getEntries(EntityClassification type)
	{
		return spawns.computeIfAbsent(type, o -> new ArrayList<>());
	}

	@Override
	public boolean isEmpty()
	{
		return spawns.isEmpty();
	}

	@Override
	public boolean isAllowEmpty()
	{
		return true;
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

	@Override
	public void setToDefault()
	{
		spawns.clear();

		List<Biome.SpawnListEntry> monsters = new ArrayList<>();

		monsters.add(new Biome.SpawnListEntry(EntityType.SPIDER, 100, 4, 4));
		monsters.add(new Biome.SpawnListEntry(EntityType.ZOMBIE, 95, 4, 4));
		monsters.add(new Biome.SpawnListEntry(EntityType.ZOMBIE_VILLAGER, 15, 1, 1));
		monsters.add(new Biome.SpawnListEntry(EntityType.SKELETON, 100, 4, 4));
		monsters.add(new Biome.SpawnListEntry(EntityType.CREEPER, 100, 4, 4));
		monsters.add(new Biome.SpawnListEntry(EntityType.SLIME, 50, 4, 4));
		monsters.add(new Biome.SpawnListEntry(EntityType.ENDERMAN, 10, 1, 4));
		monsters.add(new Biome.SpawnListEntry(EntityType.WITCH, 5, 1, 1));
		monsters.add(new Biome.SpawnListEntry(CaveEntities.CAVENIC_SKELETON.get(), 5, 1, 1));

		spawns.put(EntityClassification.MONSTER, monsters);

		List<Biome.SpawnListEntry> ambients = new ArrayList<>();

		ambients.add(new Biome.SpawnListEntry(EntityType.BAT, 20, 8, 8));
		ambients.add(new Biome.SpawnListEntry(CaveEntities.CAVEMAN.get(), 1, 1, 1));

		spawns.put(EntityClassification.AMBIENT, ambients);
	}

	@Override
	public void load()
	{
		super.load();

		for (Map.Entry<EntityClassification, List<Biome.SpawnListEntry>> entry : spawns.entrySet())
		{
			for (Biome biome : biomes.get())
			{
				List<Biome.SpawnListEntry> list = biome.getSpawns(entry.getKey());

				list.clear();
				list.addAll(entry.getValue());
			}
		}
	}
}