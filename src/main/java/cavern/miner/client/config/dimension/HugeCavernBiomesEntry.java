package cavern.miner.client.config.dimension;

import cavern.miner.config.HugeCavernConfig;
import cavern.miner.config.manager.CaveBiomeManager;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HugeCavernBiomesEntry extends CavernBiomesEntry
{
	public HugeCavernBiomesEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
	{
		super(owningScreen, owningEntryList, configElement);
	}

	@Override
	protected CaveBiomeManager getBiomeManager()
	{
		return HugeCavernConfig.BIOMES;
	}
}