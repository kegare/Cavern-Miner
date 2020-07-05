package cavern.miner.client.gui;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import com.mojang.blaze3d.systems.RenderSystem;

import cavern.miner.entity.CavemanEntity;
import cavern.miner.entity.CavemanTrade;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveEntities;
import cavern.miner.init.CaveNetworkConstants;
import cavern.miner.network.CavemanTradingMessage;
import cavern.miner.storage.Miner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class CavemanTradeScreen extends Screen
{
	private final CavemanEntity caveman;
	private final List<CavemanTrade.TradeEntry> entries;
	private final int[] inactiveEntries;

	private TradeList list;
	private Button doneButton;

	public CavemanTradeScreen(@Nullable CavemanEntity caveman, List<CavemanTrade.TradeEntry> entries, int[] inactiveEntries)
	{
		super(new TranslationTextComponent("entity.cavern.caveman"));

		if (caveman == null)
		{
			this.caveman = CaveEntities.CAVEMAN.get().create(minecraft.world);
		}
		else
		{
			this.caveman = caveman;
		}

		this.entries = entries;
		this.inactiveEntries = inactiveEntries;
	}

	@Override
	public ITextComponent getTitle()
	{
		return caveman != null ? caveman.getDisplayName() : super.getTitle();
	}

	@Override
	protected void init()
	{
		list = new TradeList(minecraft);
		children.add(list);

		doneButton = addButton(new Button(width / 2 - 70, height - 20 - 4, 150, 20, I18n.format("gui.done"), o ->
		{
			int id = -1;

			if (list.getSelected() != null)
			{
				id = list.getSelected().entryId;
			}

			CaveNetworkConstants.PLAY.send(PacketDistributor.SERVER.noArg(), new CavemanTradingMessage(caveman.getEntityId(), id));

			minecraft.displayGuiScreen(null);
		}));

		super.init();
	}

	@Override
	public void render(int mouseX, int mouseY, float particalTicks)
	{
		renderBackground();

		list.render(mouseX, mouseY, particalTicks);

		drawCenteredString(font, getTitle().getFormattedText(), width / 2, 16, 0xFFFFFF);

		Miner miner = null;

		if (minecraft.player != null)
		{
			miner = minecraft.player.getCapability(CaveCapabilities.MINER).orElse(null);
		}

		TradeList.TradeEntry selected = list.getSelected();

		if (miner != null)
		{
			int x = width - 28;
			int y = height - 22;
			int cost = selected == null ? 0 : selected.entry.getCost();
			int result = miner.getPoint() - cost;
			String point = Integer.toString(result);

			if (point.length() <= 1)
			{
				point = " " + point;
			}

			if (result < 0)
			{
				point = TextFormatting.RED + point;
			}
			else if (cost != 0)
			{
				point = TextFormatting.GRAY + point;
			}

			ItemStack stack = miner.getDisplayRank().getIconItem();

			RenderSystem.enableRescaleNormal();
			itemRenderer.renderItemIntoGUI(stack, x, y);
			itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, point);
			RenderSystem.disableRescaleNormal();
		}

		InventoryScreen.drawEntityOnScreen(65, height / 2 + 80, 50, 65 - mouseX, height / 2 - 25 - mouseY, caveman);

		super.render(mouseX, mouseY, particalTicks);
	}

	public void updateSelection()
	{
		TradeList.TradeEntry selected = list.getSelected();

		if (selected == null)
		{
			doneButton.active = true;
		}
		else
		{
			CavemanTrade.TradeEntry entry = selected.entry;
			Miner miner = null;

			if (minecraft.player != null)
			{
				miner = minecraft.player.getCapability(CaveCapabilities.MINER).orElse(null);
			}

			doneButton.active = miner == null || miner.getPoint() >= entry.getCost();

			if (doneButton.active)
			{
				doneButton.active = !ArrayUtils.contains(inactiveEntries, selected.entryId);
			}
		}
	}

	@Override
	public void onClose()
	{
		CaveNetworkConstants.PLAY.send(PacketDistributor.SERVER.noArg(), new CavemanTradingMessage(caveman.getEntityId(), -1));

		super.onClose();
	}

	@Override
	public boolean isPauseScreen()
	{
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	protected class TradeList extends ExtendedList<TradeList.TradeEntry>
	{
		protected TradeList(Minecraft mc)
		{
			super(mc, CavemanTradeScreen.this.width, CavemanTradeScreen.this.height, 32, CavemanTradeScreen.this.height - 20 - 8, 18);

			for (int i = 0; i < CavemanTradeScreen.this.entries.size(); ++i)
			{
				addEntry(new TradeEntry(i, CavemanTradeScreen.this.entries.get(i)));
			}
		}

		@Override
		protected void renderBackground()
		{
			CavemanTradeScreen.this.renderBackground();
		}

		@Override
		protected boolean isFocused()
		{
			return CavemanTradeScreen.this.getFocused() == this;
		}

		@Override
		protected void moveSelection(int diff)
		{
			super.moveSelection(diff);

			CavemanTradeScreen.this.updateSelection();
		}

		@OnlyIn(Dist.CLIENT)
		protected class TradeEntry extends ExtendedList.AbstractListEntry<TradeEntry>
		{
			private final int entryId;
			private final CavemanTrade.TradeEntry entry;

			private ITextComponent displayNameText;

			protected TradeEntry(int id, CavemanTrade.TradeEntry entry)
			{
				this.entryId = id;
				this.entry = entry;
			}

			public ITextComponent getDisplayName()
			{
				if (displayNameText != null)
				{
					return displayNameText;
				}

				ITextComponent result = entry.getDisplayName().setStyle(new Style());
				Minecraft mc = CavemanTradeScreen.this.minecraft;

				if (mc.player != null)
				{
					mc.player.getCapability(CaveCapabilities.MINER).ifPresent(o ->
					{
						if (o.getPoint() < entry.getCost() || ArrayUtils.contains(CavemanTradeScreen.this.inactiveEntries, entryId))
						{
							result.applyTextStyle(TextFormatting.GRAY);
						}
					});
				}

				displayNameText = result;

				return result;
			}

			@Override
			public void render(int entryID, int top, int left, int right, int bottom, int mouseX, int mouseY, boolean mouseOver, float particalTicks)
			{
				ITextComponent name = getDisplayName();
				FontRenderer font = CavemanTradeScreen.this.font;
				int x = TradeList.this.width / 2;
				int y = top + 1;

				TradeList.this.drawCenteredString(font, name.getFormattedText(), x, y, 0xFFFFFF);

				ItemRenderer itemRenderer = CavemanTradeScreen.this.itemRenderer;
				ItemStack stack = entry.getIconItem();

				x = TradeList.this.width / 2 - 100;
				y = top - 1;

				RenderSystem.enableRescaleNormal();
				itemRenderer.renderItemIntoGUI(stack, x, y);
				itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, null);
				RenderSystem.disableRescaleNormal();

				stack = entry.getRankIconItem();
				x = TradeList.this.width / 2 + 90;

				RenderSystem.enableRescaleNormal();
				itemRenderer.renderItemIntoGUI(stack, x, y);
				itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, Integer.toString(entry.getCost()));
				RenderSystem.disableRescaleNormal();
			}

			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button)
			{
				if (button == 0)
				{
					if (TradeList.this.getSelected() == this)
					{
						TradeList.this.setSelected(null);
					}
					else
					{
						TradeList.this.setSelected(this);
					}

					CavemanTradeScreen.this.updateSelection();

					return true;
				}

				return false;
			}
		}
	}
}