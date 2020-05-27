package cavern.miner.client.config.dimension;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import cavern.miner.client.gui.GuiVeinsEditor;
import cavern.miner.config.CavelandConfig;
import cavern.miner.config.manager.CaveVeinManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.GuiConfigEntries.IConfigEntry;
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

	@Override
	protected GuiScreen buildChildScreen()
	{
		return new GuiVeinsEditor(owningScreen, CavelandConfig.VEINS);
	}

	@Override
	public boolean isDefault()
	{
		return false;
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

		if (childScreen != null && childScreen instanceof GuiVeinsEditor)
		{
			((GuiVeinsEditor)childScreen).refreshVeins(manager.getCaveVeins());
		}
	}

	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial)
	{
		super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);

		btnSelectCategory.displayString = I18n.format(enabled() ? name : "cavern.config.auto");
	}

	@Override
	public boolean enabled()
	{
		if (owningEntryList.listEntries != null)
		{
			for (IConfigEntry entry : owningEntryList.listEntries)
			{
				if (entry.getName().endsWith("autoVeins") && entry instanceof GuiConfigEntries.BooleanEntry)
				{
					return !((GuiConfigEntries.BooleanEntry)entry).getCurrentValue();
				}
			}
		}

		return super.enabled();
	}
}