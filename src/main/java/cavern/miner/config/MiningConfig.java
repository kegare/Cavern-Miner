package cavern.miner.config;

import cavern.miner.client.DisplayCorner;
import net.minecraftforge.common.ForgeConfigSpec;

public class MiningConfig
{
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	public static final MiningConfig INSTANCE = new MiningConfig(BUILDER);
	public static final ForgeConfigSpec SPEC = BUILDER.build();

	public final ForgeConfigSpec.BooleanValue alwaysShow;
	public final ForgeConfigSpec.BooleanValue showRank;
	public final ForgeConfigSpec.EnumValue<DisplayCorner> displayConer;

	public MiningConfig(final ForgeConfigSpec.Builder builder)
	{
		builder.push("miner_status");
		alwaysShow = builder.comment("Whether to always show the mining status.").define("always_show", false);
		showRank = builder.comment("Whether to show the miner rank along with mining points.").define("show_rank", true);
		displayConer = builder.comment("The display corner of miner status.").defineEnum("display_corner", DisplayCorner.BOTTOM_RIGHT);
		builder.pop();
	}
}