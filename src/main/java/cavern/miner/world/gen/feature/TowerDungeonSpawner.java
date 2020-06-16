package cavern.miner.world.gen.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.EntityType;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.DungeonHooks;

public final class TowerDungeonSpawner
{
	private static final List<DungeonHooks.DungeonMob> ENTRIES = new ArrayList<>();

	private static final Random RANDOM = new Random();

	private TowerDungeonSpawner() {}

	public static void set(Collection<DungeonHooks.DungeonMob> entries)
	{
		Iterator<DungeonHooks.DungeonMob> iterator = entries.iterator();

		while (iterator.hasNext())
		{
			DungeonHooks.DungeonMob entry = iterator.next();

			if (entry.itemWeight <= 0)
			{
				iterator.remove();
			}
			else if (ENTRIES.contains(entry) || !ENTRIES.add(entry))
			{
				iterator.remove();
			}
		}
	}

	public static EntityType<?> get()
	{
		return ENTRIES.isEmpty() ? DungeonHooks.getRandomDungeonMob(RANDOM) : WeightedRandom.getRandomItem(RANDOM, ENTRIES).type;
	}
}