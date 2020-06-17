package cavern.miner.init;

import cavern.miner.item.CaveItemTier;
import cavern.miner.item.CaveSpawnEggItem;
import cavern.miner.item.CavernItemGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class CaveItems
{
	public static final DeferredRegister<Item> REGISTRY = new DeferredRegister<>(ForgeRegistries.ITEMS, "cavern");

	public static final RegistryObject<Item> MAGNITE_INGOT = REGISTRY.register("magnite_ingot", () -> new Item(createProperties()));
	public static final RegistryObject<Item> MAGNITE_NUGGET = REGISTRY.register("magnite_nugget", () -> new Item(createProperties()));
	public static final RegistryObject<SwordItem> MAGNITE_SWORD = REGISTRY.register("magnite_sword", () -> new SwordItem(CaveItemTier.MAGNITE, 3, -2.4F, createProperties()));
	public static final RegistryObject<PickaxeItem> MAGNITE_PICKAXE = REGISTRY.register("magnite_pickaxe", () -> new PickaxeItem(CaveItemTier.MAGNITE, 1, -2.8F, createProperties()));
	public static final RegistryObject<AxeItem> MAGNITE_AXE = REGISTRY.register("magnite_axe", () -> new AxeItem(CaveItemTier.MAGNITE, 6.0F, -3.0F, createProperties()));
	public static final RegistryObject<ShovelItem> MAGNITE_SHOVEL = REGISTRY.register("magnite_shovel", () -> new ShovelItem(CaveItemTier.MAGNITE, 1.5F, -3.0F, createProperties()));

	public static final RegistryObject<Item> AQUAMARINE = REGISTRY.register("aquamarine", () -> new Item(createProperties()));
	public static final RegistryObject<PickaxeItem> AQUAMARINE_PICKAXE = REGISTRY.register("aquamarine_pickaxe", () -> new PickaxeItem(CaveItemTier.AQUAMARINE, 1, -2.4F, createProperties()));
	public static final RegistryObject<AxeItem> AQUAMARINE_AXE = REGISTRY.register("aquamarine_axe", () -> new AxeItem(CaveItemTier.AQUAMARINE, 5.0F, -2.8F, createProperties()));
	public static final RegistryObject<ShovelItem> AQUAMARINE_SHOVEL = REGISTRY.register("aquamarine_shovel", () -> new ShovelItem(CaveItemTier.AQUAMARINE, 1.5F, -2.8F, createProperties()));

	public static final RegistryObject<SpawnEggItem> CAVENIC_SKELETON_SPAWN_EGG = REGISTRY.register("cavenic_skeleton_spawn_egg",
		() -> new CaveSpawnEggItem<>(CaveEntities.CAVENIC_SKELETON, EntityType.SKELETON, 0xAAAAAA, 0xDDDDDD, createProperties()));

	public static Item.Properties createProperties()
	{
		return new Item.Properties().group(CavernItemGroup.INSTANCE);
	}
}