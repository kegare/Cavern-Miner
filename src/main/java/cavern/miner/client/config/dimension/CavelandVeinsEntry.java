package cavern.miner.client.config.dimension;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import cavern.miner.client.config.CaveConfigGui;
import cavern.miner.client.gui.GuiVeinsEditor;
import cavern.miner.config.CavelandConfig;
import cavern.miner.config.manager.CaveVeinManager;
import cavern.miner.world.WorldProviderCaveland;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CavelandVeinsEntry extends CategoryEntry
{
	public CavelandVeinsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
	{
		super(owningScreen, owningEntryList, configElement);
	}

	protected GuiConfigEntries.BooleanEntry getAutoVeins()
	{
		return CaveConfigGui.getConfigEntry(owningEntryList.listEntries, "autoVeins", GuiConfigEntries.BooleanEntry.class);
	}

	protected GuiConfigEntries.ArrayEntry getAutoVeinsBlacklist()
	{
		return CaveConfigGui.getConfigEntry(owningEntryList.listEntries, "autoVeinBlacklist", GuiConfigEntries.ArrayEntry.class);
	}

	@Override
	protected GuiScreen buildChildScreen()
	{
		return new GuiVeinsEditor(owningScreen, WorldProviderCaveland.VEINS, () -> getAutoVeins(), () -> getAutoVeinsBlacklist());
	}

	protected void refreshChildScreen()
	{
		if (childScreen != null && childScreen instanceof GuiVeinsEditor)
		{
			((GuiVeinsEditor)childScreen).refreshVeins();
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
		return CavelandConfig.VEINS.getCaveVeins().isEmpty();
	}

	@Override
	public void setToDefault()
	{
		CaveVeinManager manager = CavelandConfig.VEINS;

		try
		{
			FileUtils.forceDelete(new File(manager.config.toString()));
		}
		catch (IOException e)
		{
			e.printStackTrace();

			return;
		}

		manager.getCaveVeins().clear();

		manager.config = null;
		CavelandConfig.syncVeinsConfig();

		refreshChildScreen();
	}
}