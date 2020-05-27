package cavern.miner.entity;

import cavern.miner.entity.monster.EntityCaveman;
import cavern.miner.entity.monster.EntityCavenicCreeper;
import cavern.miner.entity.monster.EntityCavenicSkeleton;
import cavern.miner.entity.monster.EntityCavenicSpider;
import cavern.miner.entity.monster.EntityCavenicWitch;
import cavern.miner.entity.monster.EntityCavenicZombie;
import cavern.miner.entity.passive.EntityDurangHog;
import cavern.miner.util.CaveUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.IForgeRegistry;

public final class CaveEntityRegistry
{
	public static final NonNullList<SpawnListEntry> SPAWNS = NonNullList.create();
	public static final NonNullList<SpawnListEntry> ANIMAL_SPAWNS = NonNullList.create();

	private static EntityEntry createEntry(Class<? extends Entity> entityClass, String key, String name, int primaryColor, int secondaryColor)
	{
		EntityEntry entry = new EntityEntry(entityClass, name);
		ResourceLocation regKey = CaveUtils.getKey(key);

		entry.setRegistryName(regKey);
		entry.setEgg(new EntityEggInfo(regKey, primaryColor, secondaryColor));

		return entry;
	}

	public static void registerEntities(IForgeRegistry<EntityEntry> registry)
	{
		registry.register(createEntry(EntityCavenicSkeleton.class, "cavenic_skeleton", "CavenicSkeleton", 0xAAAAAA, 0xDDDDDD));
		registry.register(createEntry(EntityCavenicCreeper.class, "cavenic_creeper", "CavenicCreeper", 0xAAAAAA, 0x2E8B57));
		registry.register(createEntry(EntityCavenicZombie.class, "cavenic_zombie", "CavenicZombie", 0xAAAAAA, 0x00A0A0));
		registry.register(createEntry(EntityCavenicSpider.class, "cavenic_spider", "CavenicSpider", 0xAAAAAA, 0x811F1F));
		registry.register(createEntry(EntityCavenicWitch.class, "cavenic_witch", "CavenicWitch", 0xAAAAAA, 0x4A5348));
		registry.register(createEntry(EntityCaveman.class, "caveman", "Caveman", 0xAAAAAA, 0xCCCCCC));
		registry.register(createEntry(EntityDurangHog.class, "durang_hog", "DurangHog", 0xC69EA0, 0x7D5150));
	}

	public static void regsiterSpawns()
	{
		SPAWNS.add(new SpawnListEntry(EntityCavenicSkeleton.class, 20, 1, 1));
		SPAWNS.add(new SpawnListEntry(EntityCavenicCreeper.class, 30, 1, 1));
		SPAWNS.add(new SpawnListEntry(EntityCavenicZombie.class, 30, 2, 2));
		SPAWNS.add(new SpawnListEntry(EntityCavenicSpider.class, 30, 1, 1));
		SPAWNS.add(new SpawnListEntry(EntityCavenicWitch.class, 15, 1, 1));
		SPAWNS.add(new SpawnListEntry(EntityCaveman.class, 35, 1, 1));

		ANIMAL_SPAWNS.add(new SpawnListEntry(EntityDurangHog.class, 5, 1, 2));
	}
}