package cavern.miner.config;

import java.io.File;
import java.io.Reader;
import java.util.Collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import cavern.miner.config.json.DungeonMobSerializer;
import cavern.miner.init.CaveEntities;
import cavern.miner.world.gen.feature.TowerDungeonSpawner;
import net.minecraft.entity.EntityType;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.DungeonHooks;

public class TowerDungeonMobConfig extends AbstractEntryConfig
{
	private final NonNullList<DungeonHooks.DungeonMob> entries = NonNullList.create();

	public TowerDungeonMobConfig()
	{
		super(new File(CavernModConfig.getConfigDir(), "tower_dungeon_mobs.json"));
	}

	public boolean setEntries(Collection<DungeonHooks.DungeonMob> collection)
	{
		entries.clear();

		return entries.addAll(collection);
	}

	public NonNullList<DungeonHooks.DungeonMob> getEntries()
	{
		return entries;
	}

	@Override
	public String toJson() throws JsonParseException
	{
		if (entries.isEmpty())
		{
			return null;
		}

		JsonArray array = new JsonArray();

		for (DungeonHooks.DungeonMob entry : entries)
		{
			JsonElement e = DungeonMobSerializer.INSTANCE.serialize(entry, entry.getClass(), null);

			if (e.isJsonNull() || e.toString().isEmpty())
			{
				continue;
			}

			array.add(e);
		}

		return getGson().toJson(array);
	}

	@Override
	public void fromJson(Reader json) throws JsonParseException
	{
		JsonArray array = getGson().fromJson(json, JsonArray.class);

		if (array.size() == 0)
		{
			return;
		}

		entries.clear();

		for (JsonElement e : array)
		{
			if (e.isJsonNull() || !e.isJsonObject() || e.toString().isEmpty())
			{
				continue;
			}

			DungeonHooks.DungeonMob entry = DungeonMobSerializer.INSTANCE.deserialize(e, e.getClass(), null);

			if (entry != null && entry.itemWeight > 0)
			{
				entries.add(entry);
			}
		}
	}

	public void registerEntries()
	{
		TowerDungeonSpawner.set(entries);
	}

	public void setDefault()
	{
		entries.clear();
		entries.add(new DungeonHooks.DungeonMob(100, EntityType.ZOMBIE));
		entries.add(new DungeonHooks.DungeonMob(100, EntityType.SKELETON));
		entries.add(new DungeonHooks.DungeonMob(100, EntityType.SPIDER));
		entries.add(new DungeonHooks.DungeonMob(50, EntityType.CAVE_SPIDER));
		entries.add(new DungeonHooks.DungeonMob(20, EntityType.CREEPER));
		entries.add(new DungeonHooks.DungeonMob(20, EntityType.ENDERMAN));

		CaveEntities.CAVENIC_SKELETON.ifPresent(o -> entries.add(new DungeonHooks.DungeonMob(20, o)));
	}
}