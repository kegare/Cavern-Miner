package cavern.miner.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class GeneralConfig
{
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	public static final GeneralConfig INSTANCE = new GeneralConfig(BUILDER);
	public static final ForgeConfigSpec SPEC = BUILDER.build();

	public final ForgeConfigSpec.BooleanValue updateNotification;

	public final ForgeConfigSpec.IntValue findRadius;
	public final ForgeConfigSpec.BooleanValue posCache;
	public final ForgeConfigSpec.IntValue sleepWait;

	public final ForgeConfigSpec.BooleanValue disableMiner;

	public GeneralConfig(final ForgeConfigSpec.Builder builder)
	{
		String serverSide = "Note: If multiplayer, server-side only.";

		builder.push("general");
		updateNotification = builder.comment("When true, notify as chat message if a new version is available.").define("update_notification", true);
		builder.pop();

		builder.push("caver");
		findRadius = builder.comment("How far (in blocks) the cave portal must be found.", serverSide).defineInRange("find_radius", 32, 10, 256);
		posCache = builder.comment("If cache and teleports the previous cave portal position.", serverSide).define("pos_cache", false);
		sleepWait = builder.comment("How long (in seconds) the player must wait before sleeping in the caverns.", serverSide).defineInRange("sleep_wait", 180, 0, 100000);
		builder.pop();

		builder.push("miner");
		disableMiner = builder.comment("If disable the miner status for all players.").define("disable_miner", false);
		builder.pop();
	}

	public static final OreEntryConfig ORE_ENTRIES = new OreEntryConfig();
	public static final MinerRankConfig MINER_RANKS = new MinerRankConfig();
	public static final RandomiteDropConfig RANDOMITE_DROPS = new RandomiteDropConfig();
	public static final TowerDungeonMobConfig TOWER_DUNGEON_MOBS = new TowerDungeonMobConfig();

	public static void loadConfig()
	{
		ORE_ENTRIES.loadFromFile();

		if (ORE_ENTRIES.getEntries().isEmpty())
		{
			ORE_ENTRIES.setDefault();
			ORE_ENTRIES.saveToFile();
		}

		ORE_ENTRIES.registerEntries();

		MINER_RANKS.loadFromFile();

		if (MINER_RANKS.getEntries().isEmpty())
		{
			MINER_RANKS.setDefault();
			MINER_RANKS.saveToFile();
		}

		MINER_RANKS.registerEntries();

		RANDOMITE_DROPS.loadFromFile();

		if (RANDOMITE_DROPS.getEntries().isEmpty())
		{
			RANDOMITE_DROPS.setDefault();
			RANDOMITE_DROPS.saveToFile();
		}

		RANDOMITE_DROPS.registerEntries();

		if (!TOWER_DUNGEON_MOBS.loadFromFile())
		{
			TOWER_DUNGEON_MOBS.setDefault();
			TOWER_DUNGEON_MOBS.saveToFile();
		}

		TOWER_DUNGEON_MOBS.registerEntries();
	}
}