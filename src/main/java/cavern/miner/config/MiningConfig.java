package cavern.miner.config;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import cavern.miner.client.config.CaveConfigEntries;
import cavern.miner.config.property.ConfigBlocks;
import cavern.miner.config.property.ConfigDisplayPos;
import cavern.miner.config.property.ConfigMiningPoints;
import cavern.miner.core.CavernMod;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class MiningConfig
{
	public static Configuration config;

	public static ConfigDisplayPos minerStatusPos = new ConfigDisplayPos();
	public static boolean showMinerRank;
	public static boolean alwaysShowMinerStatus;

	public static ConfigMiningPoints miningPoints = new ConfigMiningPoints();
	public static boolean miningCombo;
	public static boolean criticalMining;

	public static boolean actualMining;
	public static boolean collectDrops;
	public static boolean collectExps;
	public static ConfigBlocks veinTargetBlocks = new ConfigBlocks();
	public static ConfigBlocks areaTargetBlocks = new ConfigBlocks();

	public static void syncConfig()
	{
		String category = "mining";
		Property prop;
		String comment;

		if (config == null)
		{
			config = Config.loadConfig(category);
		}

		String subCategory = category;
		List<String> propOrder = Collections.emptyList();

		if (GeneralConfig.SIDE.isClient())
		{
			subCategory = "display";
			propOrder = Lists.newArrayList();

			prop = config.get(subCategory, "minerStatusPos", ConfigDisplayPos.Type.BOTTOM_RIGHT.ordinal());
			prop.setMinValue(0).setMaxValue(ConfigDisplayPos.Type.values().length - 1).setConfigEntryClass(CaveConfigEntries.cycleInteger);
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			comment += " [range: " + prop.getMinValue() + " ~ " + prop.getMaxValue() + ", default: " + prop.getDefault() + "]";

			int min = Integer.parseInt(prop.getMinValue());
			int max = Integer.parseInt(prop.getMaxValue());

			for (int i = min; i <= max; ++i)
			{
				comment += Configuration.NEW_LINE + i + ": " + CavernMod.proxy.translate(prop.getLanguageKey() + "." + i);

				if (i < max)
				{
					comment += ",";
				}
			}

			prop.setComment(comment);
			propOrder.add(prop.getName());
			minerStatusPos.setValue(prop.getInt(minerStatusPos.getValue()));

			prop = config.get(subCategory, "showMinerRank", true);
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			comment += " [default: " + prop.getDefault() + "]";
			prop.setComment(comment);
			propOrder.add(prop.getName());
			showMinerRank = prop.getBoolean(showMinerRank);

			prop = config.get(subCategory, "alwaysShowMinerStatus", false);
			prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
			comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
			prop.setComment(comment);
			propOrder.add(prop.getName());
			alwaysShowMinerStatus = prop.getBoolean(alwaysShowMinerStatus);

			config.setCategoryPropertyOrder(subCategory, propOrder);
		}

		subCategory = "general";
		propOrder = Lists.newArrayList();

		prop = config.get(subCategory, "miningPoints", new String[0]);
		prop.setConfigEntryClass(CaveConfigEntries.miningPoints);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		miningPoints.setValues(prop.getStringList());

		prop = config.get(subCategory, "miningCombo", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, does not have to match client-side and server-side.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		miningCombo = prop.getBoolean(miningCombo);

		prop = config.get(subCategory, "criticalMining", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		criticalMining = prop.getBoolean(criticalMining);

		config.setCategoryPropertyOrder(subCategory, propOrder);

		subCategory = "enchantment";
		propOrder = Lists.newArrayList();

		prop = config.get(subCategory, "actualMining", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		actualMining = prop.getBoolean(actualMining);

		prop = config.get(subCategory, "collectDrops", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		collectDrops = prop.getBoolean(collectDrops);

		prop = config.get(subCategory, "collectExps", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		collectExps = prop.getBoolean(collectExps);

		prop = config.get(subCategory, "veinTargetBlocks", new String[0]);
		prop.setConfigEntryClass(CaveConfigEntries.selectTargets);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		veinTargetBlocks.setValues(prop.getStringList());

		prop = config.get(subCategory, "areaTargetBlocks", new String[0]);
		prop.setConfigEntryClass(CaveConfigEntries.selectTargets);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		areaTargetBlocks.setValues(prop.getStringList());

		config.setCategoryPropertyOrder(subCategory, propOrder);

		Config.saveConfig(config);
	}
}