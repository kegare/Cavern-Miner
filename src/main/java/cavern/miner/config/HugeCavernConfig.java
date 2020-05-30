package cavern.miner.config;

import java.util.List;

import com.google.common.collect.Lists;

import cavern.miner.client.config.CaveConfigEntries;
import cavern.miner.config.manager.CaveBiomeManager;
import cavern.miner.config.manager.CaveVeinManager;
import cavern.miner.config.property.ConfigItems;
import cavern.miner.core.CavernMod;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class HugeCavernConfig
{
	public static Configuration config;

	public static int dimensionId;
	public static boolean halfHeight;

	public static ConfigItems triggerItems = new ConfigItems();

	public static boolean generateCaves;
	public static boolean generateLakes;

	public static int monsterSpawn;

	public static double caveBrightness;
	public static boolean caveFog;

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
			config = Config.loadConfig("hugecavern", category);
		}

		prop = config.get(category, "dimension", -51);
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

		prop = config.get(category, "generateLakes", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		generateLakes = prop.getBoolean(generateLakes);

		prop = config.get(category, "monsterSpawn", 50);
		prop.setMinValue(0).setMaxValue(5000);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + ", default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		monsterSpawn = prop.getInt(monsterSpawn);

		if (GeneralConfig.SIDE.isClient())
		{
			prop = config.get(category, "caveBrightness", 0.05D);
			prop.setMinValue(0.0D).setMaxValue(1.0D);
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + ", default: " + prop.getDefault() + "]";
			prop.setComment(comment);
			propOrder.add(prop.getName());
			caveBrightness = prop.getDouble(caveBrightness);

			prop = config.get(category, "caveFog", true);
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			comment += " [default: " + prop.getDefault() + "]";
			comment += Configuration.NEW_LINE;
			comment += "Note: If multiplayer, server-side only.";
			prop.setComment(comment);
			propOrder.add(prop.getName());
			caveFog = prop.getBoolean(caveFog);
		}

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
			BIOMES.config = Config.loadConfig("hugecavern", "biomes");
		}
		else
		{
			BIOMES.getCaveBiomes().clear();
		}

		if (!BIOMES.config.getCategoryNames().isEmpty())
		{
			CavernConfig.addBiomesFromConfig(BIOMES);
		}

		Config.saveConfig(BIOMES.config);
	}

	public static void syncVeinsConfig()
	{
		if (VEINS.config == null)
		{
			VEINS.config = Config.loadConfig("hugecavern", "veins");
		}
		else
		{
			VEINS.getCaveVeins().clear();
		}

		if (!VEINS.config.getCategoryNames().isEmpty())
		{
			CavernConfig.addVeinsFromConfig(VEINS);
		}

		Config.saveConfig(VEINS.config);
	}
}