package cavern.miner.client.config.dimension;

import cavern.miner.client.gui.GuiEditBiomes;
import cavern.miner.config.CavernConfig;
import cavern.miner.config.manager.CaveBiomeManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CavernBiomesEntry extends CategoryEntry
{
	public CavernBiomesEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
	{
		super(owningScreen, owningEntryList, configElement);
	}

	protected CaveBiomeManager getBiomeManager()
	{
		return CavernConfig.BIOMES;
	}

	@Override
	protected GuiScreen buildChildScreen()
	{
		return new GuiEditBiomes(owningScreen, getBiomeManager());
	}

	@Override
	public boolean isDefault()
	{
		return getBiomeManager().getCaveBiomes().isEmpty();
	}

	@Override
	public void setToDefault()
	{
		CaveBiomeManager manager = getBiomeManager();

		manager.getCaveBiomes().clear();
		manager.saveToFile();

		if (childScreen != null && childScreen instanceof GuiEditBiomes)
		{
			((GuiEditBiomes)childScreen).refreshBiomes();
		}
	}
}