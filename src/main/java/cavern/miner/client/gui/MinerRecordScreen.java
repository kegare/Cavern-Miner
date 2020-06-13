package cavern.miner.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;

import cavern.miner.client.ItemStackCache;
import cavern.miner.storage.MinerRecord;
import cavern.miner.world.vein.OrePointHelper;
import cavern.miner.world.vein.OreRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
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

@OnlyIn(Dist.CLIENT)
public class MinerRecordScreen extends Screen
{
	private final MinerRecord record;

	private RecordList list;

	public MinerRecordScreen(MinerRecord record)
	{
		super(new TranslationTextComponent("cavern.miner.record"));
		this.record = record;
	}

	@Override
	protected void init()
	{
		list = new RecordList(minecraft);
		children.add(list);

		addButton(new Button(width / 2 + 10, height - 20 - 4, 150, 20, I18n.format("gui.done"), o -> onClose()));

		super.init();
	}

	@Override
	public void render(int mouseX, int mouseY, float particalTicks)
	{
		renderBackground();

		list.render(mouseX, mouseY, particalTicks);

		drawCenteredString(font, title.getFormattedText(), width / 2, 16, 0xFFFFFF);
		drawString(font, String.format("%s: %s (%d)", I18n.format("cavern.miner.score"), list.getScoreRank(), list.totalScore), width / 2 - 155, height - 18, 0xEEEEEE);

		super.render(mouseX, mouseY, particalTicks);
	}

	@OnlyIn(Dist.CLIENT)
	protected class RecordList extends ExtendedList<RecordList.RecordEntry>
	{
		private final int totalCount;
		private final int totalScore;

		protected RecordList(Minecraft mc)
		{
			super(mc, MinerRecordScreen.this.width, MinerRecordScreen.this.height, 32, MinerRecordScreen.this.height - 20 - 8, 18);

			List<Map.Entry<Block, Integer>> list = new ArrayList<>(MinerRecordScreen.this.record.getEntries());

			list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

			int count = 0;
			int score = 0;

			for (Map.Entry<Block, Integer> entry : list)
			{
				RecordEntry recordEntry = new RecordEntry(entry);

				addEntry(recordEntry);

				count += entry.getValue();

				int i = OrePointHelper.getPoint(OreRegistry.getEntry(entry.getKey()));

				if (i > 1)
				{
					i += (count - 1) * Math.max(i / 2, 1);
				}

				score += i;
			}

			this.totalCount = count;
			this.totalScore = score;
		}

		protected String getScoreRank()
		{
			if (totalScore >= 100000)
			{
				return "S+";
			}

			if (totalScore >= 50000)
			{
				return "S";
			}

			if (totalScore >= 10000)
			{
				return "A";
			}

			if (totalScore >= 5000)
			{
				return "B";
			}

			if (totalScore >= 3000)
			{
				return "C";
			}

			if (totalScore >= 1000)
			{
				return "D";
			}

			return "E";
		}

		@Override
		protected int getScrollbarPosition()
		{
			return super.getScrollbarPosition() + 20;
		}

		@Override
		public int getRowWidth()
		{
			return super.getRowWidth() + 30;
		}

		@Override
		protected boolean isFocused()
		{
			return MinerRecordScreen.this.getFocused() == this;
		}

		@OnlyIn(Dist.CLIENT)
		protected class RecordEntry extends ExtendedList.AbstractListEntry<RecordEntry>
		{
			private final Block block;
			private final int count;

			protected RecordEntry(Map.Entry<Block, Integer> entry)
			{
				this.block = entry.getKey();
				this.count = entry.getValue();
			}

			@Override
			public void render(int entryID, int top, int left, int right, int bottom, int mouseX, int mouseY, boolean mouseOver, float particalTicks)
			{
				ITextComponent name = block.getNameTextComponent();
				FontRenderer fontRenderer = MinerRecordScreen.this.font;
				int x = RecordList.this.width / 2;
				int y = top + 1;

				RecordList.this.drawCenteredString(fontRenderer, name.getFormattedText(), x, y, 0xFFFFFF);

				ItemRenderer itemRenderer = MinerRecordScreen.this.itemRenderer;
				ItemStack stack = ItemStackCache.get(block);

				x = RecordList.this.width / 2 - 100;
				y = top - 1;

				RenderSystem.enableRescaleNormal();
				itemRenderer.renderItemIntoGUI(stack, x, y);
				itemRenderer.renderItemOverlayIntoGUI(fontRenderer, stack, x, y, Integer.toString(count));
				RenderSystem.disableRescaleNormal();

				switch (entryID)
				{
					case 0:
						stack = ItemStackCache.get(Items.DIAMOND_PICKAXE);
						break;
					case 1:
						stack = ItemStackCache.get(Items.GOLDEN_PICKAXE);
						break;
					case 2:
						stack = ItemStackCache.get(Items.IRON_PICKAXE);
						break;
					case 3:
						stack = ItemStackCache.get(Items.STONE_PICKAXE);
						break;
					default:
						stack = ItemStackCache.get(Items.WOODEN_PICKAXE);
						break;
				}

				double per = (double)count / (double)RecordList.this.totalCount * 100.0D;

				x = RecordList.this.width / 2 + 90;

				RenderSystem.enableRescaleNormal();
				itemRenderer.renderItemIntoGUI(stack, x, y);
				itemRenderer.renderItemOverlayIntoGUI(fontRenderer, stack, x, y, String.format("%.2f", per) + "%");
				RenderSystem.disableRescaleNormal();
			}
		}
	}
}