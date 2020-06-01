package cavern.miner.client.config;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import cavern.miner.client.gui.GuiSelectBlock;
import cavern.miner.client.gui.GuiSelectOreDict;
import cavern.miner.client.gui.GuiSelectOreDict.OreDictEntry;
import cavern.miner.client.gui.Selector;
import cavern.miner.client.gui.SelectSwitch;
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
public class SelectBlocksAndOreDictsEntry extends ArrayEntry
{
	public SelectBlocksAndOreDictsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
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

			mc.displayGuiScreen(getSelectBlockGuiScreen());
		}
	}

	public GuiSelectBlock getSelectBlockGuiScreen()
	{
		GuiSelectBlock selectBlock = createSelectBlockGuiScreen();
		GuiSelectOreDict selectOreDict = createSelectOreDictGuiScreen();

		selectBlock.setSwitchEntry(new SelectSwitch(selectOreDict, "oreDict"));
		selectOreDict.setSwitchEntry(new SelectSwitch(selectBlock, "block"));

		return selectBlock;
	}

	protected GuiSelectBlock createSelectBlockGuiScreen()
	{
		return new GuiSelectBlock(owningScreen, this);
	}

	protected GuiSelectOreDict createSelectOreDictGuiScreen()
	{
		return new GuiSelectOreDict(owningScreen, this, new Selector<OreDictEntry>()
		{
			@Override
			public boolean isValidEntry(OreDictEntry entry)
			{
				return !entry.getItemStack().isEmpty() && entry.getItemStack().getItem() instanceof ItemBlock;
			}

			@Override
			public void onSelected(List<OreDictEntry> selected) {}
		});
	}
}