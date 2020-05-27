package cavern.miner.block;

import cavern.miner.item.CaveItems;
import cavern.miner.item.ItemAcresia;
import cavern.miner.item.ItemBlockCave;
import cavern.miner.item.ItemBlockPerverted;
import cavern.miner.item.ItemCave;
import cavern.miner.item.ItemCavePortal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

public final class CaveBlocks
{
	public static final BlockCavernPortal CAVERN_PORTAL = new BlockCavernPortal();
	public static final BlockHugeCavernPortal HUGE_CAVERN_PORTAL = new BlockHugeCavernPortal();
	public static final BlockCavelandPortal CAVELAND_PORTAL = new BlockCavelandPortal();

	public static final BlockCavernPortal[] CAVE_PORTALS = {CAVERN_PORTAL, HUGE_CAVERN_PORTAL, CAVELAND_PORTAL};

	public static final BlockCave CAVE_BLOCK = new BlockCave();
	public static final BlockCavenicBush CAVENIC_SHROOM = new BlockCavenicBush();
	public static final BlockAcresia ACRESIA = new BlockAcresia();
	public static final BlockPervertedLog PERVERTED_LOG = new BlockPervertedLog();
	public static final BlockPervertedLeaves PERVERTED_LEAVES = new BlockPervertedLeaves();
	public static final BlockPervertedSapling PERVERTED_SAPLING = new BlockPervertedSapling();

	public static void registerBlocks(IForgeRegistry<Block> registry)
	{
		registry.register(CAVERN_PORTAL.setRegistryName("cavern_portal"));
		registry.register(HUGE_CAVERN_PORTAL.setRegistryName("huge_cavern_portal"));
		registry.register(CAVELAND_PORTAL.setRegistryName("caveland_portal"));

		registry.register(CAVE_BLOCK.setRegistryName("cave_block"));
		registry.register(CAVENIC_SHROOM.setRegistryName("cavenic_shroom"));
		registry.register(ACRESIA.setRegistryName("acresia"));
		registry.register(PERVERTED_LOG.setRegistryName("perverted_log"));
		registry.register(PERVERTED_LEAVES.setRegistryName("perverted_leaves"));
		registry.register(PERVERTED_SAPLING.setRegistryName("perverted_sapling"));
	}

	public static void registerItemBlocks(IForgeRegistry<Item> registry)
	{
		registry.register(new ItemCavePortal(CAVERN_PORTAL));
		registry.register(new ItemCavePortal(HUGE_CAVERN_PORTAL));
		registry.register(new ItemCavePortal(CAVELAND_PORTAL));

		registry.register(new ItemBlockCave(CAVE_BLOCK));
		registry.register(new ItemBlock(CAVENIC_SHROOM).setRegistryName(CAVENIC_SHROOM.getRegistryName()));
		registry.register(new ItemAcresia(ACRESIA));
		registry.register(new ItemBlockPerverted(PERVERTED_LOG, Blocks.LOG));
		registry.register(new ItemBlockPerverted(PERVERTED_LEAVES, Blocks.LEAVES));
		registry.register(new ItemBlockPerverted(PERVERTED_SAPLING, Blocks.SAPLING));
	}

	@SideOnly(Side.CLIENT)
	public static void registerModels()
	{
		ModelLoader.setCustomStateMapper(CAVE_BLOCK, new StateMap.Builder().withName(BlockCave.VARIANT).build());
		ModelLoader.setCustomStateMapper(PERVERTED_LOG, new StateMap.Builder().withName(BlockOldLog.VARIANT).withSuffix("_log").build());
		ModelLoader.setCustomStateMapper(PERVERTED_LEAVES, new StateMap.Builder().withName(BlockOldLeaf.VARIANT).withSuffix("_leaves").ignore(BlockLeaves.CHECK_DECAY, BlockLeaves.DECAYABLE).build());
		ModelLoader.setCustomStateMapper(PERVERTED_SAPLING, new StateMap.Builder().withName(BlockSapling.TYPE).withSuffix("_sapling").build());

		registerModel(CAVERN_PORTAL);
		registerModel(HUGE_CAVERN_PORTAL);
		registerModel(CAVELAND_PORTAL);

		registerModels(CAVE_BLOCK, "aquamarine_ore", "aquamarine_block", "magnite_ore", "magnite_block", "randomite_ore", "hexcite_ore", "hexcite_block", "fissured_stone");
		registerModel(CAVENIC_SHROOM);
		registerModels(ACRESIA, "acresia_seeds", "acresia_fruits");
		registerVanillaModels(PERVERTED_LOG, "oak_log", "spruce_log", "birch_log", "jungle_log");
		registerVanillaModels(PERVERTED_LEAVES, "oak_leaves", "spruce_leaves", "birch_leaves", "jungle_leaves");
		registerVanillaModels(PERVERTED_SAPLING, "oak_sapling", "spruce_sapling", "birch_sapling", "jungle_sapling", "acacia_sapling", "dark_oak_sapling");
	}

	@SideOnly(Side.CLIENT)
	public static void registerModel(Block block)
	{
		CaveItems.registerModel(Item.getItemFromBlock(block));
	}

	@SideOnly(Side.CLIENT)
	public static void registerModel(Block block, String modelName)
	{
		CaveItems.registerModel(Item.getItemFromBlock(block), modelName);
	}

	@SideOnly(Side.CLIENT)
	public static void registerModels(Block block, String... modelNames)
	{
		CaveItems.registerModels(Item.getItemFromBlock(block), modelNames);
	}

	@SideOnly(Side.CLIENT)
	public static void registerVanillaModel(Block block, String modelName)
	{
		CaveItems.registerVanillaModel(Item.getItemFromBlock(block), modelName);
	}

	@SideOnly(Side.CLIENT)
	public static void registerVanillaModels(Block block, String... modelNames)
	{
		CaveItems.registerVanillaModels(Item.getItemFromBlock(block), modelNames);
	}

	@SideOnly(Side.CLIENT)
	public static void registerBlockColors(BlockColors colors)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();

		colors.registerBlockColorHandler((state, world, pos, tintIndex) ->
		{
			PERVERTED_LEAVES.setGraphicsLevel(mc.gameSettings.fancyGraphics);

			BlockPlanks.EnumType type = state.getValue(BlockOldLeaf.VARIANT);

			switch (type)
			{
				case SPRUCE:
					return ColorizerFoliage.getFoliageColorPine();
				case BIRCH:
					return ColorizerFoliage.getFoliageColorBirch();
				default:
			}

			if (world != null && pos != null)
			{
				BiomeColorHelper.getFoliageColorAtPos(world, pos);
			}

			return ColorizerFoliage.getFoliageColorBasic();
		},
		PERVERTED_LEAVES);
	}

	@SideOnly(Side.CLIENT)
	public static void registerItemBlockColors(BlockColors blockColors, ItemColors itemColors)
	{
		itemColors.registerItemColorHandler((stack, tintIndex) ->
		{
			@SuppressWarnings("deprecation")
			IBlockState state = ((ItemBlock)stack.getItem()).getBlock().getStateFromMeta(stack.getMetadata());

			return blockColors.colorMultiplier(state, null, null, tintIndex);
		},
		PERVERTED_LEAVES);
	}

	public static void registerOreDicts()
	{
		OreDictionary.registerOre("oreAquamarine", BlockCave.EnumType.AQUAMARINE_ORE.getItemStack());
		OreDictionary.registerOre("blockAquamarine", BlockCave.EnumType.AQUAMARINE_BLOCK.getItemStack());
		OreDictionary.registerOre("oreMagnite", BlockCave.EnumType.MAGNITE_ORE.getItemStack());
		OreDictionary.registerOre("blockMagnite", BlockCave.EnumType.MAGNITE_BLOCK.getItemStack());
		OreDictionary.registerOre("oreRandomite", BlockCave.EnumType.RANDOMITE_ORE.getItemStack());
		OreDictionary.registerOre("oreHexcite", BlockCave.EnumType.HEXCITE_ORE.getItemStack());
		OreDictionary.registerOre("blockHexcite", BlockCave.EnumType.HEXCITE_BLOCK.getItemStack());
		OreDictionary.registerOre("oreFissured", BlockCave.EnumType.FISSURED_STONE.getItemStack());
		OreDictionary.registerOre("treeLeaves", new ItemStack(PERVERTED_LEAVES, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("treeSapling", new ItemStack(PERVERTED_SAPLING, 1, OreDictionary.WILDCARD_VALUE));
	}

	public static void registerSmeltingRecipes()
	{
		GameRegistry.addSmelting(BlockCave.EnumType.AQUAMARINE_ORE.getItemStack(), ItemCave.EnumType.AQUAMARINE.getItemStack(), 1.0F);
		GameRegistry.addSmelting(BlockCave.EnumType.MAGNITE_ORE.getItemStack(), ItemCave.EnumType.MAGNITE_INGOT.getItemStack(), 0.7F);
		GameRegistry.addSmelting(BlockCave.EnumType.HEXCITE_ORE.getItemStack(), ItemCave.EnumType.HEXCITE.getItemStack(), 1.0F);

		GameRegistry.addSmelting(new ItemStack(PERVERTED_LOG, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Items.COAL, 1, 1), 0.0F);
	}
}