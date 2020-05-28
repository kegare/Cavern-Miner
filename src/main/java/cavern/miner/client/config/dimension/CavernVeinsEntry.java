package cavern.miner.client.config.dimension;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import cavern.miner.client.gui.GuiVeinsEditor;
import cavern.miner.config.CavernConfig;
import cavern.miner.config.manager.CaveVeinManager;
import cavern.miner.world.WorldProviderCavern;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.GuiConfigEntries.IConfigEntry;
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

	protected String[] getAutoVeinsBlacklist()
	{
		if (owningEntryList.listEntries != null)
		{
			for (IConfigEntry entry : owningEntryList.listEntries)
			{
				if (entry.getName().endsWith("autoVeinBlacklist") && entry instanceof GuiConfigEntries.ArrayEntry)
				{
					Object[] values = ((GuiConfigEntries.ArrayEntry)entry).getCurrentValues();

					if (values.length > 0)
					{
						return Arrays.asList(values).toArray(new String[values.length]);
					}
				}
			}
		}

		return null;
	}

	@Override
	protected GuiScreen buildChildScreen()
	{
		return new GuiVeinsEditor(owningScreen, WorldProviderCavern.VEINS, CavernConfig.VEINS, getAutoVeinsBlacklist());
	}

	@Override
	public boolean isDefault()
	{
		return false;
	}

	@Override
	public void setToDefault()
	{
		CaveVeinManager manager = CavernConfig.VEINS;

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
		CavernConfig.syncVeinsConfig();

		if (childScreen != null && childScreen instanceof GuiVeinsEditor)
		{
			((GuiVeinsEditor)childScreen).refreshVeins(manager.getCaveVeins());
		}
	}
}