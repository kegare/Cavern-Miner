package cavern.miner.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.systems.RenderSystem;

import cavern.miner.client.ItemStackCache;
import cavern.miner.storage.MinerRecord;
import cavern.miner.world.vein.OrePointHelper;
import cavern.miner.world.vein.OreRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
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
	private TextFieldWidget searchBar;

	private String lastFilterText = "";
	private boolean filtered = true;

	public MinerRecordScreen(MinerRecord record)
	{
		super(new TranslationTextComponent("cavern.miner.record"));
		this.record = record;
	}

	@Override
	protected void init()
	{
		list = new RecordList(minecraft);

		int fieldWidth = 135;

		searchBar = new TextFieldWidget(font, width / 2 - fieldWidth - 5, height - 16 - 6, fieldWidth, 16, searchBar, I18n.format("itemGroup.search"));
		searchBar.setFocused2(false);

		String text = searchBar.getText();

		if (!text.isEmpty())
		{
			list.filterEntries(text);

			lastFilterText = text;
		}

		addButton(new Button(width / 2 + 5, height - 20 - 4, fieldWidth, 20, I18n.format("gui.done"), o -> onClose()));

		children.add(list);
		children.add(searchBar);
	}

	@Override
	public void render(int mouseX, int mouseY, float particalTicks)
	{
		renderBackground();

		list.render(mouseX, mouseY, particalTicks);

		drawCenteredString(font, title.getFormattedText(), width / 2, 16, 0xFFFFFF);
		drawRightAlignedString(font, String.format("%s: %s (%d)", I18n.format("cavern.miner.score"), list.getScoreRank(), list.totalScore), width - 12, 16, 0xC0C0C0);

		super.render(mouseX, mouseY, particalTicks);

		searchBar.render(mouseX, mouseY, particalTicks);
	}

	@Override
	public void tick()
	{
		searchBar.tick();

		final String text = searchBar.getText();

		if (!text.equalsIgnoreCase(lastFilterText))
		{
			filtered = false;
		}

		if (!filtered)
		{
			list.filterEntries(text);

			lastFilterText = text;
			filtered = true;
		}
	}

	@OnlyIn(Dist.CLIENT)
	protected class RecordList extends ExtendedList<RecordList.RecordEntry>
	{
		protected final List<RecordEntry> entries = new ArrayList<>();

		protected final LoadingCache<String, List<RecordEntry>>  filterCache = CacheBuilder.newBuilder().build(new CacheLoader<String, List<RecordEntry>>()
		{
			@Override
			public List<RecordEntry> load(final String key) throws Exception
			{
				return RecordList.this.entries.stream()
					.filter(o -> StringUtils.containsIgnoreCase(o.getDisplayName().getUnformattedComponentText(), key)).collect(Collectors.toList());
			}
		});

		private final int totalCount;
		private final int totalScore;

		protected RecordList(Minecraft mc)
		{
			super(mc, MinerRecordScreen.this.width, MinerRecordScreen.this.height, 32, MinerRecordScreen.this.height - 20 - 8, 18);

			int id = 0;
			int count = 0;
			int score = 0;

			for (Map.Entry<Block, Integer> entry : MinerRecordScreen.this.record.getEntries())
			{
				entries.add(new RecordEntry(id++, entry));

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

			replaceEntries(entries);
		}

		protected void filterEntries(final String text)
		{
			if (text.isEmpty())
			{
				replaceEntries(entries);
			}
			else
			{
				replaceEntries(filterCache.getUnchecked(text));
			}
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
		protected void renderBackground()
		{
			MinerRecordScreen.this.renderBackground();
		}

		@Override
		protected boolean isFocused()
		{
			return MinerRecordScreen.this.getFocused() == this;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int particalTicks)
		{
			if (super.mouseClicked(mouseX, mouseY, particalTicks))
			{
				MinerRecordScreen.this.searchBar.setFocused2(false);

				return true;
			}

			return false;
		}

		@OnlyIn(Dist.CLIENT)
		protected class RecordEntry extends ExtendedList.AbstractListEntry<RecordEntry>
		{
			private final int entryId;
			private final Block block;
			private final int count;

			protected RecordEntry(int id, Map.Entry<Block, Integer> entry)
			{
				this.entryId = id;
				this.block = entry.getKey();
				this.count = entry.getValue();
			}

			public ITextComponent getDisplayName()
			{
				return block.getNameTextComponent();
			}

			@Override
			public void render(int entryID, int top, int left, int right, int bottom, int mouseX, int mouseY, boolean mouseOver, float particalTicks)
			{
				ITextComponent name = getDisplayName();
				FontRenderer font = MinerRecordScreen.this.font;
				int x = RecordList.this.width / 2;
				int y = top + 1;

				RecordList.this.drawCenteredString(font, name.getFormattedText(), x, y, 0xFFFFFF);

				ItemRenderer itemRenderer = MinerRecordScreen.this.itemRenderer;
				ItemStack stack = ItemStackCache.get(block);

				x = RecordList.this.width / 2 - 100;
				y = top - 1;

				RenderSystem.enableRescaleNormal();
				itemRenderer.renderItemIntoGUI(stack, x, y);
				itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, Integer.toString(count));
				RenderSystem.disableRescaleNormal();

				switch (entryId)
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
				itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, String.format("%.2f", per) + "%");
				RenderSystem.disableRescaleNormal();
			}
		}
	}
}