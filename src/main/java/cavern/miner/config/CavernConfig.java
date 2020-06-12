package cavern.miner.config;

import java.io.File;

import net.minecraft.block.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.Tags;

public class CavernConfig
{
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	public static final CavernConfig INSTANCE = new CavernConfig(BUILDER);
	public static final ForgeConfigSpec SPEC = BUILDER.build();

	public final ForgeConfigSpec.IntValue chunkRadius;
	public final ForgeConfigSpec.IntValue heightRadius;
	public final ForgeConfigSpec.IntValue maxCount;
	public final ForgeConfigSpec.IntValue safeDistance;

	public CavernConfig(final ForgeConfigSpec.Builder builder)
	{
		String serverSide = "Note: If multiplayer, server-side only.";

		builder.push("natural_spawn");
		chunkRadius = builder.comment("How far (in chunks) monsters must spawn.", serverSide).defineInRange("chunk_radius", 6, 1, 10);
		heightRadius = builder.comment("How high (in blocks) monsters must spawn.", serverSide).defineInRange("height_radius", 50, 10, 200);
		maxCount= builder.comment("How many monsters must spawn.", serverSide).defineInRange("max_count", 120, 0, 5000);
		safeDistance= builder.comment("How far (in blocks) monsters must not spawn.", serverSide).defineInRange("safe_distance", 16, 0, 100);
		builder.pop();
	}

	public static File getConfigDir()
	{
		return new File(CavernModConfig.getConfigDir(), "cavern");
	}

	public static final PortalConfig PORTAL = new PortalConfig(getConfigDir());

	public static final VeinConfig VEINS = new VeinConfig(getConfigDir());

	public static void loadConfig()
	{
		PORTAL.loadFromFile();

		if (PORTAL.getTriggerItems().isEmpty() || PORTAL.getFrameBlocks().isEmpty())
		{
			PORTAL.getTriggerItems().clear();
			PORTAL.getTriggerItems().add(Tags.Items.GEMS_EMERALD);
			PORTAL.getFrameBlocks().clear();
			PORTAL.getFrameBlocks().add(Blocks.MOSSY_COBBLESTONE).add(Blocks.MOSSY_STONE_BRICKS);
			PORTAL.saveToFile();
		}

		if (!VEINS.loadFromFile())
		{
			VEINS.setDefault();
			VEINS.saveToFile();
		}
	}
}