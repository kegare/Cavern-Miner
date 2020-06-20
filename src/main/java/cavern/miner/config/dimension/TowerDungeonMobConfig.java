package cavern.miner.config.dimension;

import java.io.File;
import java.io.Reader;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import cavern.miner.config.AbstractEntryConfig;
import cavern.miner.config.json.DungeonMobSerializer;
import cavern.miner.init.CaveEntities;
import cavern.miner.world.gen.feature.DungeonMobConfig;
import net.minecraft.entity.EntityType;
import net.minecraftforge.common.DungeonHooks;

public class TowerDungeonMobConfig extends AbstractEntryConfig
{
	private final DungeonMobConfig config = new DungeonMobConfig();

	public TowerDungeonMobConfig(File dir)
	{
		super(new File(dir, "tower_dungeon_mobs.json"));
	}

	public DungeonMobConfig getConfig()
	{
		return config;
	}

	@Override
	public String toJson() throws JsonParseException
	{
		if (config.getSpawns().isEmpty())
		{
			return null;
		}

		JsonArray array = new JsonArray();

		for (DungeonHooks.DungeonMob entry : config.getSpawns())
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

		config.getSpawns().clear();

		for (JsonElement e : array)
		{
			if (e.isJsonNull() || !e.isJsonObject() || e.toString().isEmpty())
			{
				continue;
			}

			DungeonHooks.DungeonMob entry = DungeonMobSerializer.INSTANCE.deserialize(e, e.getClass(), null);

			if (entry != null && entry.itemWeight > 0)
			{
				config.getSpawns().add(entry);
			}
		}
	}

	public void setDefault()
	{
		List<DungeonHooks.DungeonMob> list = config.getSpawns();

		list.clear();
		list.add(new DungeonHooks.DungeonMob(100, EntityType.ZOMBIE));
		list.add(new DungeonHooks.DungeonMob(100, EntityType.SKELETON));
		list.add(new DungeonHooks.DungeonMob(100, EntityType.SPIDER));
		list.add(new DungeonHooks.DungeonMob(50, EntityType.CAVE_SPIDER));
		list.add(new DungeonHooks.DungeonMob(20, EntityType.CREEPER));
		list.add(new DungeonHooks.DungeonMob(20, EntityType.ENDERMAN));
		list.add(new DungeonHooks.DungeonMob(20, CaveEntities.CAVENIC_SKELETON.get()));
	}
}