package cavern.miner.client.config.dimension;

import cavern.miner.client.config.GuiCaveConfig;
import cavern.miner.client.gui.GuiEditVeins;
import cavern.miner.config.manager.CaveVeinManager;
import cavern.miner.world.VeinProvider;
import cavern.miner.world.WorldProviderCavern;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CavernVeinsEntry extends CategoryEntry
{
	public CavernVeinsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
	{
		super(owningScreen, owningEntryList, prop);
	}

	protected VeinProvider getVeinProvider()
	{
		return WorldProviderCavern.VEINS;
	}

	protected GuiConfigEntries.BooleanEntry getAutoVeins()
	{
		return GuiCaveConfig.getConfigEntry(owningEntryList.listEntries, "autoVeins", GuiConfigEntries.BooleanEntry.class);
	}

	protected GuiConfigEntries.ArrayEntry getAutoVeinsBlacklist()
	{
		return GuiCaveConfig.getConfigEntry(owningEntryList.listEntries, "autoVeinBlacklist", GuiConfigEntries.ArrayEntry.class);
	}

	@Override
	protected GuiScreen buildChildScreen()
	{
		return new GuiEditVeins(owningScreen, getVeinProvider(), this::getAutoVeins, this::getAutoVeinsBlacklist);
	}

	protected void refreshChildScreen()
	{
		if (childScreen != null && childScreen instanceof GuiEditVeins)
		{
			((GuiEditVeins)childScreen).refreshVeins();
		}
	}

	@Override
	public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY)
	{
		if (!super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY))
		{
			return false;
		}

		if (btnSelectCategory.mousePressed(mc, x, y))
		{
			refreshChildScreen();
		}

		return true;
	}

	@Override
	public boolean isDefault()
	{
		CaveVeinManager manager = getVeinProvider().getVeinManager();

		return manager == null || manager.getCaveVeins().isEmpty();
	}

	@Override
	public void setToDefault()
	{
		CaveVeinManager manager = getVeinProvider().getVeinManager();

		if (manager != null)
		{
			manager.getCaveVeins().clear();
			manager.saveToFile();
		}

		refreshChildScreen();
	}
}