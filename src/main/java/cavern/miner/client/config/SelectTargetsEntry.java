package cavern.miner.client.config;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SelectTargetsEntry extends SelectBlocksAndOreDictsEntry
{
	public SelectTargetsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
	{
		super(owningScreen, owningEntryList, configElement);
	}

	@Override
	public void updateValueButtonText()
	{
		super.updateValueButtonText();

		if (isDefault() && ArrayUtils.isEmpty(configElement.getDefaults()))
		{
			btnValue.displayString = I18n.format("cavern.config.auto");
		}
	}
}