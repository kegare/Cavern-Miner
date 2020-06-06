package cavern.miner.world;

import net.minecraft.world.gen.OverworldGenSettings;

public class CavernGenSettings extends OverworldGenSettings
{
	@Override
	public int getBedrockRoofHeight()
	{
		return 0;
	}

	@Override
	public int getBedrockFloorHeight()
	{
		return 255;
	}
}