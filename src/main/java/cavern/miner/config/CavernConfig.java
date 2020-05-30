package cavern.miner.config;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cavern.miner.client.config.CaveConfigEntries;
import cavern.miner.config.manager.CaveBiome;
import cavern.miner.config.manager.CaveBiomeManager;
import cavern.miner.config.manager.CaveVein;
import cavern.miner.config.manager.CaveVeinManager;
import cavern.miner.config.property.ConfigEntities;
import cavern.miner.config.property.ConfigItems;
import cavern.miner.core.CavernMod;
import cavern.miner.entity.monster.EntityCavenicSkeleton;
import cavern.miner.entity.monster.EntityCavenicSpider;
import cavern.miner.entity.monster.EntityCavenicZombie;
import cavern.miner.util.BlockMeta;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CavernConfig
{
	public static Configuration config;

	public static int dimensionId;
	public static boolean halfHeight;

	public static ConfigItems triggerItems = new ConfigItems();

	public static boolean generateCaves;
	public static boolean generateRavine;
	public static boolean generateExtremeCaves;
	public static boolean generateExtremeRavine;
	public static boolean generateLakes;
	public static boolean generateDungeons;
	public static boolean generateTowerDungeons;
	public static boolean generateMineshaft;

	public static ConfigEntities dungeonMobs = new ConfigEntities();
	public static ConfigEntities towerDungeonMobs = new ConfigEntities();

	public static int monsterSpawn;
	public static double caveBrightness;
	public static boolean keepPortalChunk;

	public static boolean autoVeins;
	public static String[] autoVeinBlacklist;

	public static final CaveBiomeManager BIOMES = new CaveBiomeManager();
	public static final CaveVeinManager VEINS = new CaveVeinManager();

	public static void syncConfig()
	{
		String category = "dimension";
		Property prop;
		String comment;
		List<String> propOrder = Lists.newArrayList();

		if (config == null)
		{
			config = Config.loadConfig("cavern", category);
		}

		prop = config.get(category, "dimension", -50);
		prop.setRequiresMcRestart(true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		dimensionId = prop.getInt(dimensionId);

		prop = config.get(category, "halfHeight", false);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		halfHeight = prop.getBoolean(halfHeight);

		prop = config.get(category, "triggerItems", new String[0]);
		prop.setConfigEntryClass(CaveConfigEntries.selectBlocksAndItems);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		triggerItems.setValues(prop.getStringList());

		prop = config.get(category, "generateCaves", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		generateCaves = prop.getBoolean(generateCaves);

		prop = config.get(category, "generateRavine", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		generateRavine = prop.getBoolean(generateRavine);

		prop = config.get(category, "generateExtremeCaves", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		generateExtremeCaves = prop.getBoolean(generateExtremeCaves);

		prop = config.get(category, "generateExtremeRavine", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		generateExtremeRavine = prop.getBoolean(generateExtremeRavine);

		prop = config.get(category, "generateLakes", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		generateLakes = prop.getBoolean(generateLakes);

		prop = config.get(category, "generateDungeons", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		generateDungeons = prop.getBoolean(generateDungeons);

		prop = config.get(category, "generateTowerDungeons", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		generateTowerDungeons = prop.getBoolean(generateTowerDungeons);

		prop = config.get(category, "generateMineshaft", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		generateMineshaft = prop.getBoolean(generateMineshaft);

		Set<Class<? extends Entity>> mobs = Sets.newHashSet();

		mobs.add(EntityZombie.class);
		mobs.add(EntitySkeleton.class);
		mobs.add(EntitySpider.class);
		mobs.add(EntityCaveSpider.class);
		mobs.add(EntityCreeper.class);
		mobs.add(EntityEnderman.class);
		mobs.add(EntitySilverfish.class);

		prop = config.get(category, "dungeonMobs", mobs.stream().map(EntityList::getKey).map(ResourceLocation::toString).sorted().toArray(String[]::new));
		prop.setConfigEntryClass(CaveConfigEntries.selectMobs);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		dungeonMobs.setValues(prop.getStringList());

		mobs.clear();
		mobs.add(EntityZombie.class);
		mobs.add(EntitySkeleton.class);
		mobs.add(EntitySpider.class);
		mobs.add(EntityCaveSpider.class);
		mobs.add(EntityEnderman.class);
		mobs.add(EntityCavenicSkeleton.class);
		mobs.add(EntityCavenicZombie.class);
		mobs.add(EntityCavenicSpider.class);

		prop = config.get(category, "towerDungeonMobs", mobs.stream().map(EntityList::getKey).map(ResourceLocation::toString).sorted().toArray(String[]::new));
		prop.setConfigEntryClass(CaveConfigEntries.selectMobs);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		towerDungeonMobs.setValues(prop.getStringList());

		prop = config.get(category, "monsterSpawn", 80);
		prop.setMinValue(0).setMaxValue(5000);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + ", default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		monsterSpawn = prop.getInt(monsterSpawn);

		prop = config.get(category, "caveBrightness", 0.035D);
		prop.setMinValue(0.0D).setMaxValue(1.0D);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + ", default: " + prop.getDefault() + "]";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		caveBrightness = prop.getDouble(caveBrightness);

		prop = config.get(category, "keepPortalChunk", false);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		keepPortalChunk = prop.getBoolean(keepPortalChunk);

		prop = config.get(category, "autoVeins", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		autoVeins = prop.getBoolean(autoVeins);

		String[] blacklist = {"oreQuartz", "stoneAndesitePolished", "stoneDioritePolished", "stoneGranitePolished"};

		prop = config.get(category, "autoVeinBlacklist", blacklist);
		prop.setConfigEntryClass(CaveConfigEntries.selectVeins);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		autoVeinBlacklist = prop.getStringList();

		config.setCategoryPropertyOrder(category, propOrder);

		Config.saveConfig(config);
	}

	public static void syncBiomesConfig()
	{
		if (BIOMES.config == null)
		{
			BIOMES.config = Config.loadConfig("cavern", "biomes");
		}
		else
		{
			BIOMES.getCaveBiomes().clear();
		}

		if (!BIOMES.config.getCategoryNames().isEmpty())
		{
			addBiomesFromConfig(BIOMES);
		}

		Config.saveConfig(BIOMES.config);
	}

	public static void syncVeinsConfig()
	{
		if (VEINS.config == null)
		{
			VEINS.config = Config.loadConfig("cavern", "veins");
		}
		else
		{
			VEINS.getCaveVeins().clear();
		}

		if (!VEINS.config.getCategoryNames().isEmpty())
		{
			addVeinsFromConfig(VEINS);
		}

		Config.saveConfig(VEINS.config);
	}

	public static void generateBiomesConfig(CaveBiomeManager manager, Collection<CaveBiome> biomes)
	{
		String category = "biomes";
		Property prop;
		String comment;

		for (CaveBiome caveBiome : biomes)
		{
			Biome biome = caveBiome.getBiome();
			String entry = biome.getRegistryName().toString();
			List<String> propOrder = Lists.newArrayList();

			prop = manager.config.get(entry, "terrainBlock", biome.fillerBlock.getBlock().getRegistryName().toString());
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			prop.setComment(comment);
			propOrder.add(prop.getName());
			prop.set(caveBiome.getTerrainBlock().getBlockName());

			prop = manager.config.get(entry, "terrainBlockMeta", Integer.toString(biome.fillerBlock.getBlock().getMetaFromState(biome.fillerBlock)));
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			prop.setComment(comment);
			propOrder.add(prop.getName());
			prop.set(caveBiome.getTerrainBlock().getMeta());

			prop = manager.config.get(entry, "topBlock", biome.topBlock.getBlock().getRegistryName().toString());
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			prop.setComment(comment);
			propOrder.add(prop.getName());
			prop.set(caveBiome.getTopBlock().getBlockName());

			prop = manager.config.get(entry, "topBlockMeta", Integer.toString(biome.topBlock.getBlock().getMetaFromState(biome.topBlock)));
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			prop.setComment(comment);
			propOrder.add(prop.getName());
			prop.set(caveBiome.getTopBlock().getMeta());

			manager.config.setCategoryPropertyOrder(entry, propOrder);

			manager.addCaveBiome(caveBiome);
		}
	}

	public static void addBiomesFromConfig(CaveBiomeManager manager)
	{
		for (String name : manager.config.getCategoryNames())
		{
			Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(name));

			if (biome == null)
			{
				continue;
			}

			ConfigCategory category = manager.config.getCategory(name);

			String terrainBlock = category.get("terrainBlock").getString();
			String terrainBlockMeta = category.get("terrainBlockMeta").getString();
			String topBlock = category.get("topBlock").getString();
			String topBlockMeta = category.get("topBlockMeta").getString();

			CaveBiome caveBiome = new CaveBiome(biome);

			int terrainMeta;
			int topMeta;

			try
			{
				terrainMeta = Integer.parseInt(terrainBlockMeta);
			}
			catch (NumberFormatException e)
			{
				terrainMeta = 0;
			}

			try
			{
				topMeta = Integer.parseInt(topBlockMeta);
			}
			catch (NumberFormatException e)
			{
				topMeta = 0;
			}

			caveBiome.setTerrainBlock(new BlockMeta(terrainBlock, Blocks.STONE, terrainMeta));
			caveBiome.setTopBlock(new BlockMeta(topBlock, Blocks.STONE, topMeta));

			manager.addCaveBiome(caveBiome);
		}
	}

	public static void generateVeinsConfig(CaveVeinManager manager, Collection<CaveVein> veins)
	{
		String category = "veins";
		Property prop;
		String comment;
		String blockDefault = Blocks.STONE.getRegistryName().toString();
		int index = 0;

		for (CaveVein vein : veins)
		{
			String entry = Integer.toString(index);
			List<String> propOrder = Lists.newArrayList();

			prop = manager.config.get(entry, "block", blockDefault);
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			prop.setComment(comment);
			propOrder.add(prop.getName());
			prop.set(vein.getBlockMeta().getBlockName());

			prop = manager.config.get(entry, "blockMeta", Integer.toString(0));
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			prop.setComment(comment);
			propOrder.add(prop.getName());
			prop.set(vein.getBlockMeta().getMeta());

			prop = manager.config.get(entry, "targetBlock", blockDefault);
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			prop.setComment(comment);
			propOrder.add(prop.getName());
			prop.set(vein.getTarget().getBlockName());

			prop = manager.config.get(entry, "targetBlockMeta", Integer.toString(0));
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			prop.setComment(comment);
			propOrder.add(prop.getName());
			prop.set(vein.getTarget().getMeta());

			prop = manager.config.get(entry, "weight", 1);
			prop.setMinValue(0).setMaxValue(100);
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + "]";
			prop.setComment(comment);
			propOrder.add(prop.getName());
			prop.set(vein.getWeight());

			prop = manager.config.get(entry, "size", 1);
			prop.setMinValue(0).setMaxValue(100);
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + "]";
			prop.setComment(comment);
			propOrder.add(prop.getName());
			prop.set(vein.getSize());

			prop = manager.config.get(entry, "minHeight", 0);
			prop.setMinValue(0).setMaxValue(255);
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + ", default: " + prop.getDefault() + "]";
			prop.setComment(comment);
			propOrder.add(prop.getName());
			prop.set(vein.getMinHeight());

			prop = manager.config.get(entry, "maxHeight", 255);
			prop.setMinValue(0).setMaxValue(255);
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + ", default: " + prop.getDefault() + "]";
			prop.setComment(comment);
			propOrder.add(prop.getName());
			prop.set(vein.getMaxHeight());

			prop = manager.config.get(entry, "biomes", new String[0]);
			prop.setMaxListLength(256);
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			prop.setComment(comment);
			propOrder.add(prop.getName());
			prop.set(ObjectUtils.defaultIfNull(vein.getBiomes(), new String[0]));

			manager.config.setCategoryPropertyOrder(entry, propOrder);

			manager.addCaveVein(vein);

			++index;
		}
	}

	public static boolean addVeinsFromConfig(CaveVeinManager manager)
	{
		boolean flag = false;

		for (String name : manager.config.getCategoryNames())
		{
			if (NumberUtils.isCreatable(name))
			{
				try
				{
					ConfigCategory category = manager.config.getCategory(name);

					String block = category.get("block").getString();
					String blockMeta = category.get("blockMeta").getString();
					String targetBlock = category.get("targetBlock").getString();
					String targetBlockMeta = category.get("targetBlockMeta").getString();
					int weight = category.get("weight").getInt();
					int size = category.get("size").getInt();
					int minHeight = category.get("minHeight").getInt();
					int maxHeight = category.get("maxHeight").getInt();
					String[] biomes = category.get("biomes").getStringList();

					CaveVein vein = new CaveVein();
					int meta;
					int targetMeta;

					try
					{
						meta = Integer.parseInt(blockMeta);
					}
					catch (NumberFormatException e)
					{
						meta = 0;
					}

					try
					{
						targetMeta = Integer.parseInt(targetBlockMeta);
					}
					catch (NumberFormatException e)
					{
						targetMeta = 0;
					}

					vein.setBlockMeta(new BlockMeta(block, Blocks.STONE, meta));
					vein.setTarget(new BlockMeta(targetBlock, Blocks.STONE, targetMeta));
					vein.setWeight(weight);
					vein.setSize(size);
					vein.setMinHeight(minHeight);
					vein.setMaxHeight(maxHeight);
					vein.setBiomes(biomes);

					manager.addCaveVein(vein);
				}
				catch (Exception e) {}
			}
			else
			{
				flag = true;
			}
		}

		return flag;
	}
}