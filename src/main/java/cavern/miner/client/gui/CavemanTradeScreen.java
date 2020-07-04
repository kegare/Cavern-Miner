package cavern.miner.client.gui;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import cavern.miner.client.ItemStackCache;
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
import net.minecraft.item.Items;
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

	private TradeList list;

	public CavemanTradeScreen(@Nullable CavemanEntity caveman, List<CavemanTrade.TradeEntry> entries)
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
	}

	@Override
	protected void init()
	{
		list = new TradeList(minecraft);
		children.add(list);

		addButton(new Button(width / 2 - 70, height - 20 - 4, 150, 20, I18n.format("gui.done"), o -> onClose()));

		super.init();
	}

	@Override
	public void render(int mouseX, int mouseY, float particalTicks)
	{
		renderBackground();

		list.render(mouseX, mouseY, particalTicks);

		drawCenteredString(font, title.getFormattedText(), width / 2, 16, 0xFFFFFF);

		Miner miner = null;

		if (minecraft.player != null)
		{
			miner = minecraft.player.getCapability(CaveCapabilities.MINER).orElse(null);
		}

		if (miner != null)
		{
			int x = width - 28;
			int y = height - 22;
			int cost = list.getSelected() == null ? 0 : list.getSelected().entry.getCost();
			String point = Integer.toString(miner.getPoint() - cost);

			if (point.length() <= 1)
			{
				point = " " + point;
			}

			if (cost != 0)
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

	@Override
	public void onClose()
	{
		super.onClose();

		CaveNetworkConstants.PLAY.send(PacketDistributor.SERVER.noArg(), new CavemanTradingMessage(caveman.getEntityId(), list.getSelected() == null ? -1 : list.getSelected().entryId));
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
		protected boolean isFocused()
		{
			return CavemanTradeScreen.this.getFocused() == this;
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
						if (o.getPoint() < entry.getCost())
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

				stack = ItemStackCache.get(Items.STONE_PICKAXE);
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

					return true;
				}

				return false;
			}
		}
	}
}