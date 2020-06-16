package cavern.miner.config.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig
{
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	public static final ClientConfig INSTANCE = new ClientConfig(BUILDER);
	public static final ForgeConfigSpec SPEC = BUILDER.build();

	public final ForgeConfigSpec.EnumValue<DisplayType> displayType;
	public final ForgeConfigSpec.EnumValue<DisplayCorner> displayConer;
	public final ForgeConfigSpec.BooleanValue showRank;

	public final ForgeConfigSpec.BooleanValue caveFog;

	public ClientConfig(final ForgeConfigSpec.Builder builder)
	{
		builder.push("miner");
		displayType = builder.comment("The display type of miner status.").defineEnum("display_type", DisplayType.HOLD);
		displayConer = builder.comment("The display corner of miner status.").defineEnum("display_corner", DisplayCorner.BOTTOM_RIGHT);
		showRank = builder.comment("When enabled, the miner rank name will be shown along with the miner points.").define("show_rank", true);
		builder.pop();

		builder.push("dimension");
		caveFog = builder.comment("When true, render the cave fog.").define("cave_fog", true);
		builder.pop();
	}
}