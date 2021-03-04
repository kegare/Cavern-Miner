package cavern.miner.config.client;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig
{
	public static final ClientConfig INSTANCE;
	public static final ForgeConfigSpec SPEC;

	static
	{
		final Pair<ClientConfig, ForgeConfigSpec> factory = new ForgeConfigSpec.Builder().configure(ClientConfig::new);

		INSTANCE = factory.getLeft();
		SPEC = factory.getRight();
	}

	public final ForgeConfigSpec.EnumValue<DisplayType> displayType;
	public final ForgeConfigSpec.EnumValue<DisplayCorner> displayConer;
	public final ForgeConfigSpec.BooleanValue showRank;

	public final ForgeConfigSpec.BooleanValue caveFog;
	public final ForgeConfigSpec.BooleanValue caveMusic;

	private ClientConfig(final ForgeConfigSpec.Builder builder)
	{
		builder.push("miner");
		displayType = builder.comment("The display type of miner status.").defineEnum("display_type", DisplayType.HOLD);
		displayConer = builder.comment("The display corner of miner status.").defineEnum("display_corner", DisplayCorner.BOTTOM_RIGHT);
		showRank = builder.comment("When enabled, the miner rank name will be shown along with the miner points.").define("show_rank", true);
		builder.pop();

		builder.push("dimension");
		caveFog = builder.comment("When true, render the cave fog.").define("cave_fog", false);
		caveMusic = builder.comment("When true, play the original music for caverns.").define("cave_music", true);
		builder.pop();
	}
}