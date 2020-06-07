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

	public final ForgeConfigSpec.BooleanValue alwaysShow;
	public final ForgeConfigSpec.BooleanValue showRank;
	public final ForgeConfigSpec.EnumValue<DisplayCorner> displayConer;

	public GeneralConfig(final ForgeConfigSpec.Builder builder)
	{
		builder.push("portal");
		findRange = builder.comment("How far the cave portal must be found.").defineInRange("find_range", 32, 10, 256);
		posCache = builder.comment("If cache and teleports the previous cave portal position.").define("pos_cache", false);
		builder.pop();

		builder.push("caver");
		sleepWait = builder.comment("How long (in seconds) the player must wait before sleeping in the caverns.").defineInRange("sleep_wait", 180, 0, 100000);
		builder.pop();

		builder.push("miner");
		alwaysShow = builder.comment("Whether to always show the mining status.").define("always_show", false);
		showRank = builder.comment("Whether to show the miner rank along with mining points.").define("show_rank", true);
		displayConer = builder.comment("The display corner of miner status.").defineEnum("display_corner", DisplayCorner.BOTTOM_RIGHT);
		builder.pop();
	}
}