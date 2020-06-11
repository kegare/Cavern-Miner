package cavern.miner.config;

import java.io.File;

import net.minecraft.block.Blocks;
import net.minecraftforge.common.Tags;

public class CavernConfig
{
	public static final PortalConfig PORTAL = new PortalConfig(getConfigDir());

	public static final VeinConfig VEINS = new VeinConfig(getConfigDir());

	public static File getConfigDir()
	{
		return new File(CavernModConfig.getConfigDir(), "cavern");
	}

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