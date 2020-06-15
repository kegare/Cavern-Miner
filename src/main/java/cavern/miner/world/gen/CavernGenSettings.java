package cavern.miner.world.gen;

import net.minecraft.world.gen.GenerationSettings;

public class CavernGenSettings extends GenerationSettings
{
	@Override
	public int getBedrockRoofHeight()
	{
		return 255;
	}

	@Override
	public int getBedrockFloorHeight()
	{
		return 0;
	}

	public int getGroundHeight()
	{
		return 150;
	}
}