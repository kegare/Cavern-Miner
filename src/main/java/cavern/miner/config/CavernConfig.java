package cavern.miner.config;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cavern.miner.client.config.CaveConfigEntries;
import cavern.miner.config.manager.CaveBiomeManager;
import cavern.miner.config.manager.CaveVeinManager;
import cavern.miner.config.property.ConfigBlocks;
import cavern.miner.config.property.ConfigEntities;
import cavern.miner.config.property.ConfigItems;
import cavern.miner.core.CavernMod;
import cavern.miner.entity.monster.EntityCavenicSkeleton;
import cavern.miner.entity.monster.EntityCavenicSpider;
import cavern.miner.entity.monster.EntityCavenicZombie;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class CavernConfig
{
	public static Configuration config;

	public static int dimensionId;
	public static boolean halfHeight;

	public static ConfigItems triggerItems = new ConfigItems();
	public static ConfigBlocks portalFrameBlocks = new ConfigBlocks();

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

	public static final CaveBiomeManager BIOMES = new CaveBiomeManager("cavern");
	public static final CaveVeinManager VEINS = new CaveVeinManager("cavern");

	public static void syncConfig()
	{
		String category = "dimension";
		Property prop;
		String comment;
		List<String> propOrder = Lists.newArrayList();

		if (config == null)
		{
			config = Config.loadConfig("cavern");
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

		prop = config.get(category, "portalFrameBlocks", new String[0]);
		prop.setConfigEntryClass(CaveConfigEntries.selectBlocks);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		portalFrameBlocks.setValues(prop.getStringList());

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

		prop = config.get(category, "dungeonMobs", mobs.stream().map(mob -> EntityList.getKey(mob).toString()).sorted().toArray(String[]::new));
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

		prop = config.get(category, "towerDungeonMobs", mobs.stream().map(mob -> EntityList.getKey(mob).toString()).sorted().toArray(String[]::new));
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
}