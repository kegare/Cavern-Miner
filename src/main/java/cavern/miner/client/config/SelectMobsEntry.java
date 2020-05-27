package cavern.miner.client.config;

import cavern.miner.client.gui.GuiSelectMob;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ArrayEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SelectMobsEntry extends ArrayEntry
{
	public SelectMobsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
	{
		super(owningScreen, owningEntryList, configElement);
	}

	@Override
	public void updateValueButtonText()
	{
		super.updateValueButtonText();

		if (isDefault())
		{
			btnValue.displayString = I18n.format("gui.default");
		}
		else
		{
			int i = currentValues.length;

			if (i > 0)
			{
				btnValue.displayString = I18n.format("cavern.config.select.mob.selected", i);
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

			mc.displayGuiScreen(new GuiSelectMob(owningScreen, this));
		}
	}
}