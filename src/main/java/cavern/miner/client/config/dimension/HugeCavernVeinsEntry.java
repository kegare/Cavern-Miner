package cavern.miner.client.config.dimension;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import cavern.miner.client.gui.GuiVeinsEditor;
import cavern.miner.config.HugeCavernConfig;
import cavern.miner.config.manager.CaveVeinManager;
import cavern.miner.world.WorldProviderHugeCavern;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.CategoryEntry;
import net.minecraftforge.fml.client.config.GuiConfigEntries.IConfigEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class HugeCavernVeinsEntry extends CategoryEntry
{
	public HugeCavernVeinsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
	{
		super(owningScreen, owningEntryList, configElement);
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
		return new GuiVeinsEditor(owningScreen, WorldProviderHugeCavern.VEINS, HugeCavernConfig.VEINS, getAutoVeinsBlacklist());
	}

	@Override
	public boolean isDefault()
	{
		return false;
	}

	@Override
	public void setToDefault()
	{
		CaveVeinManager manager = HugeCavernConfig.VEINS;

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
		HugeCavernConfig.syncVeinsConfig();

		if (childScreen != null && childScreen instanceof GuiVeinsEditor)
		{
			((GuiVeinsEditor)childScreen).refreshVeins(manager.getCaveVeins());
		}
	}
}