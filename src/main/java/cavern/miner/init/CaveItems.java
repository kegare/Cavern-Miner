package cavern.miner.init;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.item.CaveItemTier;
import cavern.miner.item.CaveSpawnEggItem;
import cavern.miner.item.CavernItemGroup;
import cavern.miner.item.CavernPortalItem;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
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
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, "cavern");

	public static final RegistryObject<CavernPortalItem> CAVERN_PORTAL = REGISTRY.register("cavern_portal", () -> createPortalItem(CaveBlocks.CAVERN_PORTAL.get()));
	public static final RegistryObject<CavernPortalItem> HUGE_CAVERN_PORTAL = REGISTRY.register("huge_cavern_portal", () -> createPortalItem(CaveBlocks.HUGE_CAVERN_PORTAL.get()));

	public static final RegistryObject<BlockItem> MAGNITE_ORE = REGISTRY.register("magnite_ore", () -> createBlockItem(CaveBlocks.MAGNITE_ORE.get()));
	public static final RegistryObject<BlockItem> MAGNITE_BLOCK = REGISTRY.register("magnite_block", () -> createBlockItem(CaveBlocks.MAGNITE_BLOCK.get()));
	public static final RegistryObject<BlockItem> AQUAMARINE_ORE = REGISTRY.register("aquamarine_ore", () -> createBlockItem(CaveBlocks.AQUAMARINE_ORE.get()));
	public static final RegistryObject<BlockItem> AQUAMARINE_BLOCK = REGISTRY.register("aquamarine_block", () -> createBlockItem(CaveBlocks.AQUAMARINE_BLOCK.get()));
	public static final RegistryObject<BlockItem> RANDOMITE_ORE = REGISTRY.register("randomite_ore", () -> createBlockItem(CaveBlocks.RANDOMITE_ORE.get()));
	public static final RegistryObject<BlockItem> CRACKED_STONE = REGISTRY.register("cracked_stone", () -> createBlockItem(CaveBlocks.CRACKED_STONE.get()));

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

	public static final RegistryObject<SpawnEggItem> CAVEMAN_SPAWN_EGG = REGISTRY.register("caveman_spawn_egg",
		() -> new CaveSpawnEggItem<>(CaveEntities.CAVEMAN, EntityType.PLAYER, 0xAAAAAA, 0xCCCCCC, createProperties()));
	public static final RegistryObject<SpawnEggItem> CAVENIC_SKELETON_SPAWN_EGG = REGISTRY.register("cavenic_skeleton_spawn_egg",
		() -> new CaveSpawnEggItem<>(CaveEntities.CAVENIC_SKELETON, EntityType.SKELETON, 0xAAAAAA, 0xDDDDDD, createProperties()));

	private static CavernPortalItem createPortalItem(CavernPortalBlock block)
	{
		return new CavernPortalItem(block, new Item.Properties().maxStackSize(1));
	}

	private static BlockItem createBlockItem(Block block)
	{
		return new BlockItem(block, createProperties());
	}

	public static Item.Properties createProperties()
	{
		return new Item.Properties().group(CavernItemGroup.INSTANCE);
	}
}