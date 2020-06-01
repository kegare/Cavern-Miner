package cavern.miner.client.config.dimension;

import cavern.miner.world.VeinProvider;
import cavern.miner.world.WorldProviderHugeCavern;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HugeCavernVeinsEntry extends CavernVeinsEntry
{
	public HugeCavernVeinsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
	{
		super(owningScreen, owningEntryList, configElement);
	}

	@Override
	protected VeinProvider getVeinProvider()
	{
		return WorldProviderHugeCavern.VEINS;
	}
}