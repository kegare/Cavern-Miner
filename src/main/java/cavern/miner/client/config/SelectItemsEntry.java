package cavern.miner.client.config;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import cavern.miner.client.gui.GuiSelectItem;
import cavern.miner.client.gui.ISelectorCallback;
import cavern.miner.util.ItemMeta;
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
public class SelectItemsEntry extends ArrayEntry implements ISelectorCallback<ItemMeta>
{
	public SelectItemsEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement)
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
				btnValue.displayString = I18n.format("cavern.config.select.item.selected", i);
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

			mc.displayGuiScreen(new GuiSelectItem(owningScreen, this, this));
		}
	}

	@Override
	public boolean isValidEntry(ItemMeta entry)
	{
		return !entry.isEmpty() && !(entry.getItem() instanceof ItemBlock);
	}

	@Override
	public void onSelected(List<ItemMeta> selected) {}
}