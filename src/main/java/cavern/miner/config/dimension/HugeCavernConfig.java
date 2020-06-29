package cavern.miner.config.dimension;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;

import cavern.miner.config.CavernModConfig;
import cavern.miner.init.CaveBiomes;
import cavern.miner.world.spawner.NaturalSpawnerType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.Tags;

public class HugeCavernConfig
{
	public static final HugeCavernConfig INSTANCE;
	public static final ForgeConfigSpec SPEC;

	static
	{
		final Pair<HugeCavernConfig, ForgeConfigSpec> factory = new ForgeConfigSpec.Builder().configure(HugeCavernConfig::new);

		INSTANCE = factory.getLeft();
		SPEC = factory.getRight();
	}

	public final ForgeConfigSpec.DoubleValue lightBrightness;

	public final ForgeConfigSpec.BooleanValue flatBedrock;
	public final ForgeConfigSpec.DoubleValue cave;

	public final ForgeConfigSpec.EnumValue<NaturalSpawnerType> spawnerType;
	public final ForgeConfigSpec.IntValue chunkRadius;
	public final ForgeConfigSpec.IntValue heightRadius;
	public final ForgeConfigSpec.IntValue maxCount;
	public final ForgeConfigSpec.IntValue safeDistance;

	public final PortalConfig portal = new PortalConfig(getConfigDir())
	{
		@Override
		public void setToDefault()
		{
			super.setToDefault();

			getTriggerItems().clear();
			getTriggerItems().add(Tags.Items.GEMS_DIAMOND);
		}
	};

	public final VeinConfig veins = new VeinConfig(getConfigDir());
	public final NaturalSpawnConfig naturalSpawns = new NaturalSpawnConfig(getConfigDir(), () -> Arrays.asList(CaveBiomes.HUGE_CAVERN.get()));

	private HugeCavernConfig(final ForgeConfigSpec.Builder builder)
	{
		String serverSide = "Note: If multiplayer, server-side only.";

		builder.push("render");
		lightBrightness = builder.comment("The brightness of natural light.").defineInRange("light_brightness", 0.1D, 0.0D, 1.0D);
		builder.pop();

		builder.push("decoration");
		flatBedrock = builder.comment("If true, generate flat bedrock layers.", serverSide).define("flat_bedrock", false);
		cave = builder.comment("The generation probability of caves.", serverSide).defineInRange("cave", 0.5D, 0.0D, 1.0D);
		builder.pop();

		builder.push("natural_spawn");
		spawnerType = builder.comment("The spawner type of natural monsters spawn.", serverSide).defineEnum("spawner_type", NaturalSpawnerType.CAVERN);
		chunkRadius = builder.comment("How far (in chunks) monsters must spawn.", serverSide).defineInRange("chunk_radius", 6, 1, 10);
		heightRadius = builder.comment("How high (in blocks) monsters must spawn.", serverSide).defineInRange("height_radius", 70, 10, 200);
		maxCount = builder.comment("How many monsters must spawn.", serverSide).defineInRange("max_count", 30, 0, 5000);
		safeDistance = builder.comment("How far (in blocks) monsters must not spawn from players.", serverSide).defineInRange("safe_distance", 20, 0, 100);
		builder.pop();
	}

	public void load()
	{
		portal.load();
		veins.load();
		naturalSpawns.load();
	}

	public static File getConfigDir()
	{
		return new File(CavernModConfig.getConfigDir(), "huge_cavern");
	}
}