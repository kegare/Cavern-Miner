package cavern.miner.config;

import java.util.List;

import com.google.common.collect.Lists;

import cavern.miner.client.config.CaveConfigEntries;
import cavern.miner.config.property.ConfigCaveborn;
import cavern.miner.config.property.ConfigItems;
import cavern.miner.core.CavernMod;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

public class GeneralConfig
{
	public static Configuration config;

	public static boolean versionNotify;

	public static ConfigCaveborn caveborn = new ConfigCaveborn();
	public static ConfigItems cavebornBonusItems = new ConfigItems();

	public static int findPortalRange;
	public static boolean portalCache;
	public static boolean portalMenu;

	public static int sleepWaitTime;
	public static boolean sleepRefresh;

	public static boolean generousRandomite;
	public static ConfigItems randomiteBlacklist = new ConfigItems();

	protected static final Side SIDE = FMLLaunchHandler.side();

	public static void syncConfig()
	{
		String category = "general";
		Property prop;
		String comment;
		List<String> propOrder = Lists.newArrayList();

		if (config == null)
		{
			config = Config.loadConfig(category);
		}

		prop = config.get(category, "versionNotify", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, does not have to match client-side and server-side.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		versionNotify = prop.getBoolean(versionNotify);

		prop = config.get(category, "caveborn", ConfigCaveborn.Type.DISABLED.ordinal());
		prop.setMinValue(0).setMaxValue(ConfigCaveborn.Type.values().length - 1).setConfigEntryClass(CaveConfigEntries.cycleInteger);
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

		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		caveborn.setValue(prop.getInt(caveborn.getValue()));

		NonNullList<ItemStack> items = NonNullList.create();

		items.add(new ItemStack(Items.STONE_PICKAXE));
		items.add(new ItemStack(Items.STONE_SWORD));
		items.add(new ItemStack(Blocks.TORCH));
		items.add(new ItemStack(Items.BREAD));

		prop = config.get(category, "cavebornBonusItems", cavebornBonusItems.createValues(items));
		prop.setConfigEntryClass(CaveConfigEntries.selectBlocksAndItems);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		cavebornBonusItems.setValues(prop.getStringList());

		prop = config.get(category, "findPortalRange", 32);
		prop.setMinValue(10).setMaxValue(200);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		findPortalRange = prop.getInt(findPortalRange);

		prop = config.get(category, "portalCache", false);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		portalCache = prop.getBoolean(portalCache);

		prop = config.get(category, "portalMenu", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		portalMenu = prop.getBoolean(portalMenu);

		prop = config.get(category, "sleepWaitTime", 300);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		sleepWaitTime = prop.getInt(sleepWaitTime);

		prop = config.get(category, "sleepRefresh", true);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		sleepRefresh = prop.getBoolean(sleepRefresh);

		prop = config.get(category, "generousRandomite", false);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += " [default: " + prop.getDefault() + "]";
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		generousRandomite = prop.getBoolean(generousRandomite);

		prop = config.get(category, "randomiteBlacklist", new String[] {Blocks.BEDROCK.getRegistryName().toString()});
		prop.setConfigEntryClass(CaveConfigEntries.selectBlocksAndItems);
		prop.setLanguageKey(Config.LANG_KEY + category + "." + prop.getName());
		comment = CavernMod.proxy.translate(prop.getLanguageKey() + ".tooltip");
		comment += Configuration.NEW_LINE;
		comment += "Note: If multiplayer, server-side only.";
		prop.setComment(comment);
		propOrder.add(prop.getName());
		randomiteBlacklist.setValues(prop.getStringList());

		config.setCategoryPropertyOrder(category, propOrder);

		Config.saveConfig(config);
	}
}