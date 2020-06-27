package cavern.miner.config.dimension;

import java.io.File;
import java.util.Arrays;

import cavern.miner.config.CavernModConfig;
import cavern.miner.init.CaveBiomes;
import cavern.miner.world.spawner.NaturalSpawnerType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.Tags;

public class CavernConfig
{
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	public static final CavernConfig INSTANCE = new CavernConfig(BUILDER);
	public static final ForgeConfigSpec SPEC = BUILDER.build();

	public final ForgeConfigSpec.DoubleValue lightBrightness;

	public final ForgeConfigSpec.DoubleValue cave;
	public final ForgeConfigSpec.DoubleValue canyon;
	public final ForgeConfigSpec.DoubleValue extremeCave;
	public final ForgeConfigSpec.DoubleValue extremeCanyon;
	public final ForgeConfigSpec.DoubleValue towerDungeon;
	public final ForgeConfigSpec.BooleanValue groundDecoration;

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
			getTriggerItems().add(Tags.Items.GEMS_EMERALD);
		}
	};

	public final VeinConfig veins = new VeinConfig(getConfigDir());
	public final NaturalSpawnConfig naturalSpawns = new NaturalSpawnConfig(getConfigDir(), () -> Arrays.asList(CaveBiomes.CAVERN.get()));
	public final TowerDungeonMobConfig towerDungeonMobs = new TowerDungeonMobConfig(getConfigDir());

	public CavernConfig(final ForgeConfigSpec.Builder builder)
	{
		String serverSide = "Note: If multiplayer, server-side only.";

		builder.push("render");
		lightBrightness = builder.comment("The brightness of natural light.").defineInRange("light_brightness", 0.1D, 0.0D, 1.0D);
		builder.pop();

		builder.push("decoration");
		cave = builder.comment("The generation probability of caves.", serverSide).defineInRange("cave", 0.2D, 0.0D, 1.0D);
		canyon = builder.comment("The generation probability of canyons.", serverSide).defineInRange("canyon", 0.02D, 0.0D, 1.0D);
		extremeCave = builder.comment("The generation probability of extreme caves.", serverSide).defineInRange("extreme_cave", 0.15D, 0.0D, 1.0D);
		extremeCanyon = builder.comment("The generation probability of extreme canyons.", serverSide).defineInRange("extreme_canyon", 0.001D, 0.0D, 1.0D);
		towerDungeon = builder.comment("The generation probability of tower dungeons.", serverSide).defineInRange("tower_dungeon", 0.01D, 0.0D, 1.0D);
		groundDecoration = builder.comment("If true, decorate upper caves like on the overworld.", serverSide).define("ground_decoration", true);
		builder.pop();

		builder.push("natural_spawn");
		spawnerType = builder.comment("The spawner type of natural monsters spawn.", serverSide).defineEnum("spawner_type", NaturalSpawnerType.CAVERN);
		chunkRadius = builder.comment("How far (in chunks) monsters must spawn.", serverSide).defineInRange("chunk_radius", 5, 1, 10);
		heightRadius = builder.comment("How high (in blocks) monsters must spawn.", serverSide).defineInRange("height_radius", 70, 10, 200);
		maxCount = builder.comment("How many monsters must spawn.", serverSide).defineInRange("max_count", 70, 0, 5000);
		safeDistance = builder.comment("How far (in blocks) monsters must not spawn from players.", serverSide).defineInRange("safe_distance", 16, 0, 100);
		builder.pop();
	}

	public void load()
	{
		portal.load();
		veins.load();
		naturalSpawns.load();
		towerDungeonMobs.load();
	}

	public static File getConfigDir()
	{
		return new File(CavernModConfig.getConfigDir(), "cavern");
	}
}