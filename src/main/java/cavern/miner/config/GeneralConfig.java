package cavern.miner.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class GeneralConfig
{
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	public static final GeneralConfig INSTANCE = new GeneralConfig(BUILDER);
	public static final ForgeConfigSpec SPEC = BUILDER.build();

	public final ForgeConfigSpec.IntValue findRange;
	public final ForgeConfigSpec.BooleanValue posCache;
	public final ForgeConfigSpec.IntValue sleepWait;

	public final ForgeConfigSpec.BooleanValue disableMiner;

	public GeneralConfig(final ForgeConfigSpec.Builder builder)
	{
		String serverSide = "Note: If multiplayer, server-side only.";

		builder.push("caver");
		findRange = builder.comment("How far the cave portal must be found.", serverSide).defineInRange("find_range", 32, 10, 256);
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
	}
}