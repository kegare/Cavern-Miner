package cavern.miner.client.config.common;

import cavern.miner.client.config.CaveCategoryEntry;
import cavern.miner.config.MiningConfig;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MiningConfigEntry extends CaveCategoryEntry
{
	public MiningConfigEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
	{
		super(owningScreen, owningEntryList, prop);
	}

	@Override
	protected Configuration getConfig()
	{
		return MiningConfig.config;
	}

	@Override
	protected String getEntryName()
	{
		return "mining";
	}
}