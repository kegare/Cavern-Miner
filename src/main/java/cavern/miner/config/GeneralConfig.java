package cavern.miner.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class GeneralConfig
{
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	public static final GeneralConfig INSTANCE = new GeneralConfig(BUILDER);
	public static final ForgeConfigSpec SPEC = BUILDER.build();

	public final ForgeConfigSpec.BooleanValue updateNotification;

	public final ForgeConfigSpec.EnumValue<CavebornConfig.Type> cavebornSpawn;
	public final ForgeConfigSpec.IntValue findRadius;
	public final ForgeConfigSpec.BooleanValue posCache;
	public final ForgeConfigSpec.IntValue sleepWait;

	public final ForgeConfigSpec.BooleanValue disableMiner;

	public final CavebornConfig cavebornItems = new CavebornConfig();
	public final OreEntryConfig oreEntries = new OreEntryConfig();
	public final MinerRankConfig minerRanks = new MinerRankConfig();
	public final RandomiteDropConfig randomiteDrops = new RandomiteDropConfig();

	public GeneralConfig(final ForgeConfigSpec.Builder builder)
	{
		String serverSide = "Note: If multiplayer, server-side only.";

		builder.push("general");
		updateNotification = builder.comment("When true, notify as chat message if a new version is available.").define("update_notification", true);
		builder.pop();

		builder.push("caver");
		cavebornSpawn = builder.comment("If player will first spawn in the caverns.", serverSide).defineEnum("caveborn_spawn", CavebornConfig.Type.DISABLED);
		findRadius = builder.comment("How far (in blocks) the cave portal must be found.", serverSide).defineInRange("find_radius", 32, 10, 256);
		posCache = builder.comment("If cache and teleports the previous cave portal position.", serverSide).define("pos_cache", false);
		sleepWait = builder.comment("How long (in seconds) the player must wait before sleeping in the caverns.", serverSide).defineInRange("sleep_wait", 180, 0, 100000);
		builder.pop();

		builder.push("miner");
		disableMiner = builder.comment("If disable the miner status for all players.").define("disable_miner", false);
		builder.pop();
	}

	public void load()
	{
		if (!cavebornItems.loadFromFile())
		{
			cavebornItems.setDefault();
			cavebornItems.saveToFile();
		}

		oreEntries.loadFromFile();

		if (oreEntries.getEntries().isEmpty())
		{
			oreEntries.setDefault();
			oreEntries.saveToFile();
		}

		oreEntries.registerEntries();

		minerRanks.loadFromFile();

		if (minerRanks.getEntries().isEmpty())
		{
			minerRanks.setDefault();
			minerRanks.saveToFile();
		}

		minerRanks.registerEntries();

		randomiteDrops.loadFromFile();

		if (randomiteDrops.getEntries().isEmpty())
		{
			randomiteDrops.setDefault();
			randomiteDrops.saveToFile();
		}

		randomiteDrops.registerEntries();
	}
}