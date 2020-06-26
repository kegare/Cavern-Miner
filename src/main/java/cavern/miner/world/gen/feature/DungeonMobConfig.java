package cavern.miner.world.gen.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.registries.ForgeRegistries;

public class DungeonMobConfig implements IFeatureConfig
{
	private final List<DungeonHooks.DungeonMob> spawns = new ArrayList<>();

	public List<DungeonHooks.DungeonMob> getSpawns()
	{
		return spawns;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> ops)
	{
		ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();

		for (DungeonHooks.DungeonMob entry : spawns)
		{
			builder.put(ops.createString(entry.type.getRegistryName().toString()), ops.createInt(entry.itemWeight));
		}

		return new Dynamic<>(ops, ops.createMap(builder.build()));
	}

	public static <T> DungeonMobConfig deserialize(Dynamic<T> data)
	{
		DungeonMobConfig config = new DungeonMobConfig();
		Map<String, Integer> map = data.asMap(o -> o.asString(""), o -> o.asInt(0));

		for (Map.Entry<String, Integer> entry : map.entrySet())
		{
			String key = entry.getKey();

			if (key.isEmpty())
			{
				continue;
			}

			EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(key));

			if (entityType == null)
			{
				continue;
			}

			int weight = entry.getValue();

			if (weight <= 0)
			{
				continue;
			}

			config.spawns.add(new DungeonHooks.DungeonMob(weight, entityType));
		}

		return config;
	}
}