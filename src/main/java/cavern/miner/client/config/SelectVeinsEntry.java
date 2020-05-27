package cavern.miner.client.config;

import java.util.List;

import cavern.miner.client.gui.GuiSelectOreDict;
import cavern.miner.client.gui.GuiSelectOreDict.OreDictEntry;
import cavern.miner.client.gui.ISelectorCallback;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ArrayEntry;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SelectVeinsEntry extends ArrayEntry
{
	public SelectVeinsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
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
				btnValue.displayString = I18n.format("cavern.config.select.oreDict.selected", i);
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

			mc.displayGuiScreen(createSelectOreDictGuiScreen());
		}
	}

	protected GuiSelectOreDict createSelectOreDictGuiScreen()
	{
		return new GuiSelectOreDict(owningScreen, this, new ISelectorCallback<OreDictEntry>()
		{
			@Override
			public boolean isValidEntry(OreDictEntry entry)
			{
				String name = entry.getName();

				if (name.startsWith("ore") && name.length() > 3 && Character.isUpperCase(name.charAt(3)) || name.startsWith("stone") && name.length() > 5 && Character.isUpperCase(name.charAt(5)))
				{
					return !entry.getItemStack().isEmpty() && entry.getItemStack().getItem() instanceof ItemBlock;
				}

				return false;
			}

			@Override
			public void onSelected(List<OreDictEntry> selected) {}
		});
	}
}