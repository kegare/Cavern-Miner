package cavern.miner.config.dimension;

import java.io.File;

import cavern.miner.config.CavernModConfig;
import cavern.miner.init.CaveBiomes;
import cavern.miner.world.spawner.WorldSpawnerType;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.Tags;

public class HugeCavernConfig
{
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	public static final HugeCavernConfig INSTANCE = new HugeCavernConfig(BUILDER);
	public static final ForgeConfigSpec SPEC = BUILDER.build();

	public final ForgeConfigSpec.DoubleValue lightBrightness;

	public final ForgeConfigSpec.DoubleValue cave;

	public final ForgeConfigSpec.EnumValue<WorldSpawnerType> spawnerType;
	public final ForgeConfigSpec.IntValue chunkRadius;
	public final ForgeConfigSpec.IntValue heightRadius;
	public final ForgeConfigSpec.IntValue maxCount;
	public final ForgeConfigSpec.IntValue safeDistance;

	public final PortalConfig portal = new PortalConfig(getConfigDir());
	public final VeinConfig veins = new VeinConfig(getConfigDir());
	public final NaturalSpawnConfig naturalSpawns = new NaturalSpawnConfig(getConfigDir());

	public HugeCavernConfig(final ForgeConfigSpec.Builder builder)
	{
		String serverSide = "Note: If multiplayer, server-side only.";

		builder.push("render");
		lightBrightness = builder.comment("The brightness of natural light.").defineInRange("light_brightness", 0.1D, 0.0D, 1.0D);
		builder.pop();

		builder.push("decoration");
		cave = builder.comment("The generation probability of caves.", serverSide).defineInRange("cave", 0.5D, 0.0D, 1.0D);
		builder.pop();

		builder.push("natural_spawn");
		spawnerType = builder.comment("The spawner type of natural monsters spawn.", serverSide).defineEnum("spawner_type", WorldSpawnerType.CAVERN);
		chunkRadius = builder.comment("How far (in chunks) monsters must spawn.", serverSide).defineInRange("chunk_radius", 6, 1, 10);
		heightRadius = builder.comment("How high (in blocks) monsters must spawn.", serverSide).defineInRange("height_radius", 70, 10, 200);
		maxCount = builder.comment("How many monsters must spawn.", serverSide).defineInRange("max_count", 30, 0, 5000);
		safeDistance = builder.comment("How far (in blocks) monsters must not spawn from players.", serverSide).defineInRange("safe_distance", 20, 0, 100);
		builder.pop();
	}

	public void load()
	{
		portal.loadFromFile();

		if (portal.getTriggerItems().isEmpty() || portal.getFrameBlocks().isEmpty())
		{
			portal.getTriggerItems().clear();
			portal.getTriggerItems().add(Tags.Items.GEMS_DIAMOND);
			portal.getFrameBlocks().clear();
			portal.getFrameBlocks().add(Blocks.MOSSY_COBBLESTONE).add(Blocks.MOSSY_STONE_BRICKS);
			portal.saveToFile();
		}

		if (!veins.loadFromFile())
		{
			veins.setDefault();
			veins.saveToFile();
		}

		if (!naturalSpawns.loadFromFile())
		{
			naturalSpawns.setDefault();
			naturalSpawns.saveToFile();
		}

		CaveBiomes.HUGE_CAVERN.ifPresent(naturalSpawns::registerSpawns);
	}

	public static File getConfigDir()
	{
		return new File(CavernModConfig.getConfigDir(), "huge_cavern");
	}
}