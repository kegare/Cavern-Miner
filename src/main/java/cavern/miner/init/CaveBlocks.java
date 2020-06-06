package cavern.miner.init;

import java.util.function.Supplier;

import cavern.miner.block.CaveOreBlock;
import cavern.miner.block.CavernPortalBlock;
import cavern.miner.block.CrackedStoneBlock;
import cavern.miner.block.RandomiteOreBlock;
import cavern.miner.item.CavernPortalItem;
import cavern.miner.vein.OreRegistry;
import cavern.miner.world.VeinProvider;
import net.minecraft.block.Block;
import net.minecraft.block.OreBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class CaveBlocks
{
	public static final DeferredRegister<Block> REGISTRY = new DeferredRegister<>(ForgeRegistries.BLOCKS, "cavern");

	public static final RegistryObject<CavernPortalBlock> CAVERN_PORTAL = REGISTRY.register("cavern_portal", () -> new CavernPortalBlock(createPortalProperties()));

	public static final Supplier<CavernPortalBlock[]> CAVE_PORTALS = () -> new CavernPortalBlock[] {CAVERN_PORTAL.get()};

	public static final RegistryObject<Block> MAGNITE_ORE = REGISTRY.register("magnite_ore", () -> new OreBlock(createOreProperties()));
	public static final RegistryObject<Block> MAGNITE_BLOCK = REGISTRY.register("magnite_block",
		() -> new Block(Block.Properties.create(Material.IRON, MaterialColor.ORANGE_TERRACOTTA).hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)));
	public static final RegistryObject<Block> AQUAMARINE_ORE = REGISTRY.register("aquamarine_ore", () -> new CaveOreBlock(createOreProperties(), rand -> MathHelper.nextInt(rand, 1, 3)));
	public static final RegistryObject<Block> AQUAMARINE_BLOCK = REGISTRY.register("aquamarine_block",
		() -> new Block(Block.Properties.create(Material.IRON, MaterialColor.LIGHT_BLUE).hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)));
	public static final RegistryObject<Block> RANDOMITE_ORE = REGISTRY.register("randomite_ore", () -> new RandomiteOreBlock(createOreProperties()));
	public static final RegistryObject<Block> CRACKED_STONE = REGISTRY.register("cracked_stone", () -> new CrackedStoneBlock(createOreProperties()));

	private static Block.Properties createPortalProperties()
	{
		return Block.Properties.create(Material.PORTAL).doesNotBlockMovement().hardnessAndResistance(-1.0F).sound(SoundType.GLASS).lightValue(5).noDrops();
	}

	private static Block.Properties createOreProperties()
	{
		return Block.Properties.create(Material.ROCK).hardnessAndResistance(3.0F, 3.0F);
	}

	private static BlockItem createBlockItem(Block block)
	{
		if (block instanceof CavernPortalBlock)
		{
			return new CavernPortalItem((CavernPortalBlock)block, new Item.Properties());
		}

		return new BlockItem(block, CaveItems.createProperties());
	}

	public static void registerBlockItems(final DeferredRegister<Item> registry)
	{
		for (RegistryObject<Block> block : REGISTRY.getEntries())
		{
			registry.register(block.getId().getPath(), () -> createBlockItem(block.get()));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerRenderType()
	{
		RenderTypeLookup.setRenderLayer(CAVERN_PORTAL.get(), RenderType.getTranslucent());

		RenderTypeLookup.setRenderLayer(MAGNITE_ORE.get(), RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(AQUAMARINE_ORE.get(), RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(RANDOMITE_ORE.get(), RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(CRACKED_STONE.get(), RenderType.getCutoutMipped());
	}

	public static void registerOres()
	{
		OreRegistry.registerTag(new OreRegistry.TagEntry(Tags.Blocks.ORES_COAL, VeinProvider.Rarity.COMMON, 1));
		OreRegistry.registerTag(new OreRegistry.TagEntry(Tags.Blocks.ORES_IRON, VeinProvider.Rarity.COMMON, 1));
		OreRegistry.registerTag(new OreRegistry.TagEntry(Tags.Blocks.ORES_GOLD, VeinProvider.Rarity.RARE, 2));
		OreRegistry.registerTag(new OreRegistry.TagEntry(Tags.Blocks.ORES_REDSTONE, VeinProvider.Rarity.UNCOMMON, 2));
		OreRegistry.registerTag(new OreRegistry.TagEntry(Tags.Blocks.ORES_LAPIS, VeinProvider.Rarity.RARE, 2));
		OreRegistry.registerTag(new OreRegistry.TagEntry(Tags.Blocks.ORES_EMERALD, VeinProvider.Rarity.EMERALD, 3));
		OreRegistry.registerTag(new OreRegistry.TagEntry(Tags.Blocks.ORES_DIAMOND, VeinProvider.Rarity.DIAMOND, 5));

		OreRegistry.registerTag(new OreRegistry.TagEntry(CaveTags.Blocks.ORES_MAGNITE, VeinProvider.Rarity.COMMON, 1));
		OreRegistry.registerTag(new OreRegistry.TagEntry(CaveTags.Blocks.ORES_AQUAMARINE, VeinProvider.Rarity.AQUA, 2));
		OreRegistry.registerTag(new OreRegistry.TagEntry(CaveTags.Blocks.ORES_RANDOMITE, VeinProvider.Rarity.RANDOMITE, 2));
		OreRegistry.registerBlock(new OreRegistry.BlockEntry(CaveBlocks.CRACKED_STONE.get(), VeinProvider.Rarity.RANDOMITE, 2));
	}
}