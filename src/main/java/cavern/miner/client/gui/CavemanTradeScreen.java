package cavern.miner.client.gui;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import cavern.miner.client.ItemStackCache;
import cavern.miner.entity.CavemanEntity;
import cavern.miner.entity.CavemanTrade;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveEntities;
import cavern.miner.init.CaveNetworkConstants;
import cavern.miner.network.CavemanTradeEffectMessage;
import cavern.miner.network.CavemanTradeItemMessage;
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
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class CavemanTradeScreen extends Screen
{
	private final List<CavemanTrade.TradeEntry> entries;

	private TradeList list;
	private CavemanEntity caveman;

	public CavemanTradeScreen(List<CavemanTrade.TradeEntry> entries)
	{
		super(new TranslationTextComponent("entity.cavern.caveman"));
		this.entries = entries;
	}

	private CavemanEntity getCaveman()
	{
		if (caveman == null)
		{
			caveman = CaveEntities.CAVEMAN.get().create(minecraft.world);
		}

		return caveman;
	}

	@Override
	protected void init()
	{
		list = new TradeList(minecraft);
		children.add(list);

		addButton(new Button(width / 2 + 10, height - 20 - 4, 145, 20, I18n.format("gui.done"), o -> onClose()));

		super.init();
	}

	@Override
	public void render(int mouseX, int mouseY, float particalTicks)
	{
		renderBackground();

		list.render(mouseX, mouseY, particalTicks);

		drawCenteredString(font, title.getFormattedText(), width / 2, 16, 0xFFFFFF);

		Integer point = null;

		if (minecraft.player != null)
		{
			point = minecraft.player.getCapability(CaveCapabilities.MINER).map(Miner::getPoint).orElse(null);
		}

		if (point != null)
		{
			drawString(font, String.format("%s: %d", I18n.format("cavern.miner.point"), point.intValue()), width / 2 - 155, height - 18, 0xEEEEEE);
		}

		InventoryScreen.drawEntityOnScreen(65, height / 2 + 80, 50, 65 - mouseX, height / 2 - 25 - mouseY, getCaveman());

		super.render(mouseX, mouseY, particalTicks);
	}

	@Override
	public void onClose()
	{
		super.onClose();

		if (list.getSelected() != null)
		{
			CavemanTrade.TradeEntry entry = list.getSelected().entry;

			if (entry instanceof CavemanTrade.EffectEntry)
			{
				CaveNetworkConstants.PLAY.send(PacketDistributor.SERVER.noArg(), new CavemanTradeEffectMessage((CavemanTrade.EffectEntry)entry));
			}
			else
			{
				CaveNetworkConstants.PLAY.send(PacketDistributor.SERVER.noArg(), new CavemanTradeItemMessage(entry));
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	protected class TradeList extends ExtendedList<TradeList.TradeEntry>
	{
		protected TradeList(Minecraft mc)
		{
			super(mc, CavemanTradeScreen.this.width, CavemanTradeScreen.this.height, 32, CavemanTradeScreen.this.height - 20 - 8, 18);

			CavemanTradeScreen.this.entries.forEach(o -> addEntry(new TradeEntry(o)));
		}

		@Override
		protected boolean isFocused()
		{
			return CavemanTradeScreen.this.getFocused() == this;
		}

		@OnlyIn(Dist.CLIENT)
		protected class TradeEntry extends ExtendedList.AbstractListEntry<TradeEntry>
		{
			private final CavemanTrade.TradeEntry entry;

			protected TradeEntry(CavemanTrade.TradeEntry entry)
			{
				this.entry = entry;
			}

			@Override
			public void render(int entryID, int top, int left, int right, int bottom, int mouseX, int mouseY, boolean mouseOver, float particalTicks)
			{
				ITextComponent name = entry.getDisplayName();
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