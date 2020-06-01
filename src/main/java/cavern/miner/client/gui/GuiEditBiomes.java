package cavern.miner.client.gui;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.CharUtils;
import org.lwjgl.input.Keyboard;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cavern.miner.client.config.GuiCaveConfig;
import cavern.miner.config.Config;
import cavern.miner.config.manager.CaveBiome;
import cavern.miner.config.manager.CaveBiomeManager;
import cavern.miner.util.BlockMeta;
import cavern.miner.util.CaveFilters;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEditBiomes extends GuiScreen
{
	protected final GuiScreen parent;
	protected final CaveBiomeManager manager;

	protected BiomeList biomeList;

	protected GuiButton doneButton;
	protected GuiButton editButton;
	protected GuiButton cancelButton;
	protected GuiButton addButton;
	protected GuiButton removeButton;
	protected GuiButton clearButton;

	protected GuiCheckBox detailInfo;
	protected GuiCheckBox instantFilter;

	protected GuiTextField filterTextField;

	protected boolean editMode;

	protected GuiTextField topBlockField;
	protected GuiTextField topMetaField;
	protected GuiTextField fillerBlockField;
	protected GuiTextField fillerMetaField;

	protected HoverChecker detailHoverChecker;
	protected HoverChecker instantHoverChecker;
	protected HoverChecker topBlockHoverChecker;
	protected HoverChecker fillerBlockHoverChecker;

	protected int maxLabelWidth;

	protected final List<String> editLabelList = Lists.newArrayList();
	protected final List<GuiTextField> editFieldList = Lists.newArrayList();

	public GuiEditBiomes(GuiScreen parent, CaveBiomeManager manager)
	{
		this.parent = parent;
		this.manager = manager;
	}

	@Override
	public void initGui()
	{
		if (biomeList == null)
		{
			biomeList = new BiomeList();

			refreshBiomes();
		}

		biomeList.setDimensions(width, height, 32, height - (editMode ? 78 : 28));

		if (doneButton == null)
		{
			doneButton = new GuiButtonExt(0, 0, 0, 65, 20, I18n.format("gui.done"));
		}

		doneButton.x = width / 2 + 135;
		doneButton.y = height - doneButton.height - 4;

		if (editButton == null)
		{
			editButton = new GuiButtonExt(1, 0, 0, doneButton.width, doneButton.height, I18n.format("gui.edit"));
			editButton.enabled = false;
		}

		editButton.x = doneButton.x - doneButton.width - 3;
		editButton.y = doneButton.y;
		editButton.enabled = biomeList.selected != null;
		editButton.visible = !editMode;

		if (cancelButton == null)
		{
			cancelButton = new GuiButtonExt(2, 0, 0, editButton.width, editButton.height, I18n.format("gui.cancel"));
		}

		cancelButton.x = editButton.x;
		cancelButton.y = editButton.y;
		cancelButton.visible = editMode;

		if (removeButton == null)
		{
			removeButton = new GuiButtonExt(4, 0, 0, doneButton.width, doneButton.height, I18n.format("gui.remove"));
		}

		removeButton.x = editButton.x - editButton.width - 3;
		removeButton.y = doneButton.y;
		removeButton.visible =  !editMode;

		if (addButton == null)
		{
			addButton = new GuiButtonExt(3, 0, 0, doneButton.width, doneButton.height, I18n.format("gui.add"));
		}

		addButton.x = removeButton.x - removeButton.width - 3;
		addButton.y = doneButton.y;
		addButton.visible = !editMode;

		if (clearButton == null)
		{
			clearButton = new GuiButtonExt(5, 0, 0, removeButton.width, removeButton.height, I18n.format("gui.clear"));
		}

		clearButton.x = removeButton.x;
		clearButton.y = removeButton.y;
		clearButton.visible = false;

		if (detailInfo == null)
		{
			detailInfo = new GuiCheckBox(6, 0, 5, I18n.format(Config.LANG_KEY + "detail"), true);
		}

		detailInfo.setIsChecked(GuiCaveConfig.detailInfo);
		detailInfo.x = width / 2 + 95;

		if (instantFilter == null)
		{
			instantFilter = new GuiCheckBox(7, 0, detailInfo.y + detailInfo.height + 2, I18n.format(Config.LANG_KEY + "instant"), true);
		}

		instantFilter.setIsChecked(GuiCaveConfig.instantFilter);
		instantFilter.x = detailInfo.x;

		buttonList.clear();
		buttonList.add(doneButton);

		if (editMode)
		{
			buttonList.add(cancelButton);
		}
		else
		{
			buttonList.add(editButton);
			buttonList.add(addButton);
			buttonList.add(removeButton);
			buttonList.add(clearButton);
		}

		buttonList.add(detailInfo);
		buttonList.add(instantFilter);

		if (filterTextField == null)
		{
			filterTextField = new GuiTextField(0, fontRenderer, 0, 0, 122, 16);
			filterTextField.setMaxStringLength(500);
		}

		filterTextField.x = width / 2 - 200;
		filterTextField.y = height - filterTextField.height - 6;

		detailHoverChecker = new HoverChecker(detailInfo, 800);
		instantHoverChecker = new HoverChecker(instantFilter, 800);

		editLabelList.clear();
		editLabelList.add(I18n.format(Config.LANG_KEY + "biomes.topBlock"));
		editLabelList.add("");
		editLabelList.add(I18n.format(Config.LANG_KEY + "biomes.fillerBlock"));
		editLabelList.add("");

		for (String key : editLabelList)
		{
			maxLabelWidth = Math.max(maxLabelWidth, fontRenderer.getStringWidth(key));
		}

		if (topBlockField == null)
		{
			topBlockField = new GuiTextField(1, fontRenderer, 0, 0, 0, 15);
			topBlockField.setMaxStringLength(100);
		}

		int i = maxLabelWidth + 8 + width / 2;
		topBlockField.x = width / 2 - i / 2 + maxLabelWidth + 10;
		topBlockField.y = biomeList.bottom + 7;
		int fieldWidth = width / 2 + i / 2 - 45 - topBlockField.x + 40;
		topBlockField.width = fieldWidth / 4 + fieldWidth / 2 - 1;

		if (topMetaField == null)
		{
			topMetaField = new GuiTextField(2, fontRenderer, 0, 0, 0, topBlockField.height);
			topMetaField.setMaxStringLength(100);
		}

		topMetaField.x = topBlockField.x + topBlockField.width + 3;
		topMetaField.y = topBlockField.y;
		topMetaField.width = fieldWidth / 4 - 1;

		if (fillerBlockField == null)
		{
			fillerBlockField = new GuiTextField(3, fontRenderer, 0, 0, 0, topBlockField.height);
			fillerBlockField.setMaxStringLength(100);
		}

		fillerBlockField.x = topBlockField.x;
		fillerBlockField.y = topBlockField.y + topBlockField.height + 5;
		fillerBlockField.width = topBlockField.width;

		if (fillerMetaField == null)
		{
			fillerMetaField = new GuiTextField(5, fontRenderer, 0, 0, 0, fillerBlockField.height);
			fillerMetaField.setMaxStringLength(100);
		}

		fillerMetaField.x = topMetaField.x;
		fillerMetaField.y = fillerBlockField.y;
		fillerMetaField.width = topMetaField.width;

		editFieldList.clear();

		if (editMode)
		{
			editFieldList.add(topBlockField);
			editFieldList.add(topMetaField);
			editFieldList.add(fillerBlockField);
			editFieldList.add(fillerMetaField);
		}

		topBlockHoverChecker = new HoverChecker(topBlockField.y - 1, topBlockField.y + topBlockField.height, topBlockField.x - maxLabelWidth - 12, topBlockField.x - 10, 800);
		fillerBlockHoverChecker = new HoverChecker(fillerBlockField.y - 1, fillerBlockField.y + fillerBlockField.height, fillerBlockField.x - maxLabelWidth - 12, fillerBlockField.x - 10, 800);
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if (button.enabled)
		{
			switch (button.id)
			{
				case 0:
					if (editMode)
					{
						for (CaveBiome caveBiome : biomeList.selected)
						{
							if (!Strings.isNullOrEmpty(topBlockField.getText()))
							{
								Block block = Block.getBlockFromName(topBlockField.getText());

								if (block != null && !(block instanceof BlockAir))
								{
									int meta;

									try
									{
										meta = Integer.parseInt(topMetaField.getText());

										if (meta < 0)
										{
											meta = 0;
										}
									}
									catch (NumberFormatException e)
									{
										meta = 0;
									}

									caveBiome.setTopBlock(new BlockMeta(block, meta));
								}
							}

							if (!Strings.isNullOrEmpty(fillerBlockField.getText()))
							{
								Block block = Block.getBlockFromName(fillerBlockField.getText());

								if (block != null && !(block instanceof BlockAir))
								{
									int meta;

									try
									{
										meta = Integer.parseInt(fillerMetaField.getText());

										if (meta < 0)
										{
											meta = 0;
										}
									}
									catch (NumberFormatException e)
									{
										meta = 0;
									}

									caveBiome.setFillerBlock(new BlockMeta(block, meta));
								}
							}
						}

						actionPerformed(cancelButton);

						biomeList.scrollToTop();
						biomeList.scrollToSelected();
					}
					else
					{
						manager.setCaveBiomes(biomeList.biomes);
						manager.saveToFile();

						actionPerformed(cancelButton);

						biomeList.selected.clear();
						biomeList.scrollToTop();
					}

					break;
				case 1:
					if (editMode)
					{
						actionPerformed(cancelButton);
					}
					else if (!biomeList.selected.isEmpty())
					{
						editMode = true;
						initGui();

						biomeList.scrollToTop();
						biomeList.scrollToSelected();

						if (biomeList.selected.size() > 1)
						{
							topBlockField.setText("");
							topMetaField.setText("");
							fillerBlockField.setText("");
							fillerMetaField.setText("");
						}
						else for (CaveBiome biome : biomeList.selected)
						{
							if (biome != null)
							{
								topBlockField.setText(biome.getTopBlock().getRegistryName().toString());
								topMetaField.setText(Integer.toString(biome.getTopBlock().getMeta()));
								fillerBlockField.setText(biome.getFillerBlock().getRegistryName().toString());
								fillerMetaField.setText(Integer.toString(biome.getFillerBlock().getMeta()));
							}
						}
					}

					break;
				case 2:
					if (editMode)
					{
						editMode = false;
						initGui();
					}
					else
					{
						mc.displayGuiScreen(parent);
					}

					break;
				case 3:
					Set<Biome> invisibleBiomes = biomeList.biomes.stream().map(CaveBiome::getBiome).collect(Collectors.toSet());

					mc.displayGuiScreen(new GuiSelectBiome(this, new Selector<Biome>()
					{
						@Override
						public boolean isValidEntry(Biome entry)
						{
							return entry != null && !invisibleBiomes.contains(entry);
						}

						@Override
						public void onSelected(List<Biome> selected)
						{
							if (editMode)
							{
								return;
							}

							biomeList.selected.clear();

							for (Biome biome : selected)
							{
								CaveBiome caveBiome = new CaveBiome(biome);

								biomeList.biomes.add(caveBiome);
								biomeList.contents.add(caveBiome);
								biomeList.selected.add(caveBiome);
							}

							biomeList.scrollToTop();
							biomeList.scrollToSelected();
						}
					}));

					break;
				case 4:
					for (CaveBiome biome : biomeList.selected)
					{
						biomeList.biomes.remove(biome);
						biomeList.contents.remove(biome);
					}

					biomeList.selected.clear();
					break;
				case 5:
					biomeList.biomes.forEach(biomeList.selected::add);

					actionPerformed(removeButton);
					break;
				case 6:
					GuiCaveConfig.detailInfo = detailInfo.isChecked();
					break;
				case 7:
					GuiCaveConfig.instantFilter = instantFilter.isChecked();
					break;
				default:
					biomeList.actionPerformed(button);
			}
		}
	}

	@Override
	public void updateScreen()
	{
		if (editMode)
		{
			for (GuiTextField textField : editFieldList)
			{
				textField.updateCursorCounter();
			}
		}
		else
		{
			editButton.enabled = !biomeList.selected.isEmpty();
			removeButton.enabled = editButton.enabled;

			filterTextField.updateCursorCounter();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float ticks)
	{
		biomeList.drawScreen(mouseX, mouseY, ticks);

		drawCenteredString(fontRenderer, I18n.format(Config.LANG_KEY + "biomes"), width / 2, 15, 0xFFFFFF);

		super.drawScreen(mouseX, mouseY, ticks);

		if (editMode)
		{
			GuiTextField textField;

			for (int i = 0, size = editFieldList.size(); i < size; ++i)
			{
				textField = editFieldList.get(i);
				textField.drawTextBox();

				drawString(fontRenderer, editLabelList.get(i), textField.x - maxLabelWidth - 10, textField.y + 3, 0xBBBBBB);
			}

			if (topBlockHoverChecker.checkHover(mouseX, mouseY))
			{
				List<String> hover = Lists.newArrayList();
				String key = Config.LANG_KEY + "biomes.topBlock";

				hover.add(TextFormatting.GRAY + I18n.format(key));
				hover.addAll(fontRenderer.listFormattedStringToWidth(I18n.format(key + ".tooltip"), 300));

				drawHoveringText(hover, mouseX, mouseY);
			}
			else if (fillerBlockHoverChecker.checkHover(mouseX, mouseY))
			{
				List<String> hover = Lists.newArrayList();
				String key = Config.LANG_KEY + "biomes.fillerBlock";

				hover.add(TextFormatting.GRAY + I18n.format(key));
				hover.addAll(fontRenderer.listFormattedStringToWidth(I18n.format(key + ".tooltip"), 300));

				drawHoveringText(hover, mouseX, mouseY);
			}
		}
		else
		{
			filterTextField.drawTextBox();
		}

		if (detailHoverChecker.checkHover(mouseX, mouseY))
		{
			drawHoveringText(fontRenderer.listFormattedStringToWidth(I18n.format(Config.LANG_KEY + "detail.hover"), 300), mouseX, mouseY);
		}
		else if (instantHoverChecker.checkHover(mouseX, mouseY))
		{
			drawHoveringText(fontRenderer.listFormattedStringToWidth(I18n.format(Config.LANG_KEY + "instant.hover"), 300), mouseX, mouseY);
		}
		else if (biomeList.isMouseYWithinSlotBounds(mouseY) && isCtrlKeyDown())
		{
			CaveBiome caveBiome = biomeList.contents.get(biomeList.getSlotIndexFromScreenCoords(mouseX, mouseY));
			List<String> info = Lists.newArrayList();
			String prefix = TextFormatting.GRAY.toString();

			info.add(prefix + I18n.format(Config.LANG_KEY + "biomes.topBlock") + ": " + caveBiome.getTopBlock().toString());
			info.add(prefix + I18n.format(Config.LANG_KEY + "biomes.fillerBlock") + ": " + caveBiome.getFillerBlock().toString());

			drawHoveringText(info, mouseX, mouseY);
		}

		if (biomeList.selected.size() > 1 && mouseX <= 100 && mouseY <= 20)
		{
			drawString(fontRenderer, I18n.format(Config.LANG_KEY + "select.entry.selected", biomeList.selected.size()), 5, 5, 0xEFEFEF);
		}
	}

	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();

		biomeList.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int x, int y, int code) throws IOException
	{
		super.mouseClicked(x, y, code);

		if (code == 1)
		{
			actionPerformed(editButton);
		}
		else if (editMode)
		{
			for (GuiTextField textField : editFieldList)
			{
				textField.mouseClicked(x, y, code);
			}

			if (!isShiftKeyDown())
			{
				if (topBlockField.isFocused())
				{
					topBlockField.setFocused(false);

					mc.displayGuiScreen(new GuiSelectBlock(this, topBlockField, topMetaField));
				}
				else if (fillerBlockField.isFocused())
				{
					fillerBlockField.setFocused(false);

					mc.displayGuiScreen(new GuiSelectBlock(this, fillerBlockField, fillerMetaField));
				}
			}
		}
		else
		{
			filterTextField.mouseClicked(x, y, code);
		}
	}

	@Override
	public void handleKeyboardInput() throws IOException
	{
		super.handleKeyboardInput();

		if (Keyboard.getEventKey() == Keyboard.KEY_LSHIFT || Keyboard.getEventKey() == Keyboard.KEY_RSHIFT)
		{
			clearButton.visible = !editMode && Keyboard.getEventKeyState();
		}
	}

	@Override
	protected void keyTyped(char c, int code) throws IOException
	{
		if (editMode)
		{
			for (GuiTextField textField : editFieldList)
			{
				if (code == Keyboard.KEY_ESCAPE)
				{
					textField.setFocused(false);
				}
				else if (textField.isFocused())
				{
					if (textField != topBlockField && textField != topMetaField && textField != fillerBlockField && textField != fillerMetaField)
					{
						if (!CharUtils.isAsciiControl(c) && !CharUtils.isAsciiNumeric(c))
						{
							continue;
						}
					}

					textField.textboxKeyTyped(c, code);
				}
			}
		}
		else
		{
			if (filterTextField.isFocused())
			{
				if (code == Keyboard.KEY_ESCAPE)
				{
					filterTextField.setFocused(false);
				}

				String prev = filterTextField.getText();

				filterTextField.textboxKeyTyped(c, code);

				String text = filterTextField.getText();
				boolean changed = text != prev;

				if (Strings.isNullOrEmpty(text) && changed)
				{
					biomeList.setFilter(null);
				}
				else if (instantFilter.isChecked() && changed || code == Keyboard.KEY_RETURN)
				{
					biomeList.setFilter(text);
				}
			}
			else
			{
				if (code == Keyboard.KEY_ESCAPE)
				{
					actionPerformed(doneButton);
				}
				else if (code == Keyboard.KEY_BACK)
				{
					biomeList.selected.clear();
				}
				else if (code == Keyboard.KEY_TAB)
				{
					if (++biomeList.nameType > 1)
					{
						biomeList.nameType = 0;
					}
				}
				else if (code == Keyboard.KEY_UP)
				{
					biomeList.scrollUp();
				}
				else if (code == Keyboard.KEY_DOWN)
				{
					biomeList.scrollDown();
				}
				else if (code == Keyboard.KEY_HOME)
				{
					biomeList.scrollToTop();
				}
				else if (code == Keyboard.KEY_END)
				{
					biomeList.scrollToEnd();
				}
				else if (code == Keyboard.KEY_SPACE)
				{
					biomeList.scrollToSelected();
				}
				else if (code == Keyboard.KEY_PRIOR)
				{
					biomeList.scrollToPrev();
				}
				else if (code == Keyboard.KEY_NEXT)
				{
					biomeList.scrollToNext();
				}
				else if (code == Keyboard.KEY_F || code == mc.gameSettings.keyBindChat.getKeyCode())
				{
					filterTextField.setFocused(true);
				}
				else if (isCtrlKeyDown() && code == Keyboard.KEY_A)
				{
					biomeList.contents.forEach(entry -> biomeList.selected.add(entry));
				}
				else if (code == Keyboard.KEY_DELETE && !biomeList.selected.isEmpty())
				{
					actionPerformed(removeButton);
				}
			}
		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	public void onGuiClosed()
	{
		biomeList.panoramaLocation = null;
	}

	public void refreshBiomes()
	{
		if (biomeList == null)
		{
			return;
		}

		biomeList.biomes.clear();
		biomeList.contents.clear();

		manager.getCaveBiomes().values().stream().sorted().forEach(biome ->
		{
			biomeList.biomes.add(biome);
			biomeList.contents.add(biome);
		});
	}

	protected class BiomeList extends GuiListSlot
	{
		protected final NonNullList<CaveBiome> biomes = NonNullList.create();
		protected final NonNullList<CaveBiome> contents = NonNullList.create();
		protected final Set<CaveBiome> selected = Sets.newTreeSet();

		protected final Map<String, List<CaveBiome>> filterCache = Maps.newHashMap();

		protected int nameType;
		protected boolean clickFlag;

		protected BiomeList()
		{
			super(GuiEditBiomes.this.mc, 0, 0, 0, 0, 22);
		}

		@Override
		public void scrollToSelected()
		{
			if (!selected.isEmpty())
			{
				int amount = 0;

				for (CaveBiome biome : selected)
				{
					amount = contents.indexOf(biome) * getSlotHeight();

					if (getAmountScrolled() != amount)
					{
						break;
					}
				}

				scrollToTop();
				scrollBy(amount);
			}
		}

		@Override
		protected int getSize()
		{
			return contents.size();
		}

		@Override
		protected void drawBackground()
		{
			drawDefaultBackground();
		}

		@Override
		protected void drawSlot(int slot, int par2, int par3, int par4, int mouseX, int mouseY, float partialTicks)
		{
			CaveBiome caveBiome = contents.get(slot);
			Biome biome = caveBiome.getBiome();
			String text;

			switch (nameType)
			{
				case 1:
					text = biome.getRegistryName().toString();
					break;
				default:
					text = biome.getBiomeName();
					break;
			}

			if (!Strings.isNullOrEmpty(text))
			{
				drawCenteredString(fontRenderer, text, width / 2, par3 + 3, 0xFFFFFF);
			}

			if (detailInfo.isChecked() || Keyboard.isKeyDown(Keyboard.KEY_TAB))
			{
				drawItemStack(itemRender, caveBiome.getTopBlock(), width / 2 - 100, par3 + 1);
				drawItemStack(itemRender, caveBiome.getFillerBlock(), width / 2 + 90, par3 + 1);
			}
		}

		@Override
		protected void elementClicked(int slot, boolean flag, int mouseX, int mouseY)
		{
			if (editMode)
			{
				return;
			}

			CaveBiome biome = contents.get(slot);

			if ((clickFlag = !clickFlag == true) && !selected.remove(biome))
			{
				if (!isCtrlKeyDown())
				{
					selected.clear();
				}

				selected.add(biome);
			}
		}

		@Override
		protected boolean isSelected(int slot)
		{
			return selected.contains(contents.get(slot));
		}

		protected void setFilter(String filter)
		{
			List<CaveBiome> result;

			if (Strings.isNullOrEmpty(filter))
			{
				result = biomes;
			}
			else if (filter.equals("selected"))
			{
				result = Lists.newArrayList(selected);
			}
			else
			{
				if (!filterCache.containsKey(filter))
				{
					filterCache.put(filter, biomes.parallelStream().filter(e -> filterMatch(e, filter)).collect(Collectors.toList()));
				}

				result = filterCache.get(filter);
			}

			if (!contents.equals(result))
			{
				contents.clear();
				contents.addAll(result);
			}
		}

		protected boolean filterMatch(CaveBiome entry, String filter)
		{
			if (filter.startsWith("top:"))
			{
				filter = filter.substring(filter.indexOf(":") + 1);

				return CaveFilters.blockFilter(entry.getTopBlock(), filter);
			}

			if (filter.startsWith("filler:"))
			{
				filter = filter.substring(filter.indexOf(":") + 1);

				return CaveFilters.blockFilter(entry.getFillerBlock(), filter);
			}

			return CaveFilters.biomeFilter(entry.getBiome(), filter);
		}
	}
}