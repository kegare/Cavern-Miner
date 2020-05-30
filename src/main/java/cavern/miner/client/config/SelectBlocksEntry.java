package cavern.miner.client.config;

import org.apache.commons.lang3.ArrayUtils;

import cavern.miner.client.gui.GuiSelectBlock;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ArrayEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SelectBlocksEntry extends ArrayEntry
{
	public SelectBlocksEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
	{
		super(owningScreen, owningEntryList, configElement);
	}

	@Override
	public void updateValueButtonText()
	{
		super.updateValueButtonText();

		if (isDefault() && ArrayUtils.isEmpty(configElement.getDefaults()))
		{
			btnValue.displayString = I18n.format("gui.default");
		}
		else
		{
			int i = currentValues.length;

			if (i > 0)
			{
				btnValue.displayString = I18n.format("cavern.config.select.block.selected", i);
			}
		}
	}

	@Override
	public void valueButtonPressed(int index)
	{
		if (GuiScreen.isShiftKeyDown())
		{
			super.valueButtonPressed(index);
		}
		else if (btnValue.enabled)
		{
			btnValue.playPressSound(mc.getSoundHandler());

			mc.displayGuiScreen(new GuiSelectBlock(owningScreen, this));
		}
	}
}