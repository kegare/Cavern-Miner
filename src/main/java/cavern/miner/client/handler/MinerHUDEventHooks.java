package cavern.miner.client.handler;

import cavern.miner.api.CavernAPI;
import cavern.miner.client.CaveRenderingRegistry;
import cavern.miner.config.MiningConfig;
import cavern.miner.config.property.ConfigDisplayPos;
import cavern.miner.data.Miner;
import cavern.miner.data.MinerRank;
import cavern.miner.data.MiningCache;
import cavern.miner.util.CaveUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class MinerHUDEventHooks
{
	private int posX;
	private int posY;

	private double miningPointPer = -1.0D;

	private ConfigDisplayPos.Type getDisplayType()
	{
		return MiningConfig.minerStatusPos.getType();
	}

	private boolean canRenderHUD(Minecraft mc)
	{
		ConfigDisplayPos.Type type = getDisplayType();

		if (type.isHidden())
		{
			return false;
		}

		if (mc.currentScreen != null && !GuiChat.class.isInstance(mc.currentScreen))
		{
			return false;
		}

		if (!CavernAPI.dimension.isInCaverns(mc.player))
		{
			return false;
		}

		for (ItemStack held : mc.player.getHeldEquipment())
		{
			if (CaveUtils.isPickaxe(held))
			{
				return true;
			}
		}

		return MiningConfig.alwaysShowMinerStatus || mc.player.capabilities.isCreativeMode || mc.gameSettings.advancedItemTooltips;
	}

	private void setDisplayPos(ConfigDisplayPos.Type type, Minecraft mc, int scaledWidth, int scaledHeight)
	{
		switch (type)
		{
			case TOP_RIGHT:
				posX = scaledWidth - 20;
				posY = 5;

				if (!mc.player.getActivePotionEffects().isEmpty())
				{
					posY = 30;
				}

				break;
			case TOP_LEFT:
				posX = 5;
				posY = 5;
				break;
			case BOTTOM_RIGHT:
				posX = scaledWidth - 20;
				posY = scaledHeight - 21;
				break;
			case BOTTOM_LEFT:
				posX = 5;
				posY = scaledHeight - 21;
				break;
			default:
		}
	}

	private double calcMiningPointPer(int point, int phase, boolean direct)
	{
		double per = point == 0 ? 0.0D : (double)point / (double)phase * 100.0D;

		if (direct)
		{
			return per;
		}

		double diff = Math.abs(per - miningPointPer);
		double d1 = 0.0175D;
		double d2 = 0.35D;

		if (miningPointPer < 0.0D || diff < d1)
		{
			miningPointPer = per;
		}
		else
		{
			if (per > miningPointPer)
			{
				if (diff > 1.0D)
				{
					miningPointPer += d2;
				}
				else
				{
					miningPointPer += d1;
				}
			}
			else if (per < miningPointPer)
			{
				if (diff > 1.0D)
				{
					miningPointPer -= d2 * 2.0D;
				}
				else
				{
					miningPointPer -= d1 * 1.5D;
				}
			}
		}

		return miningPointPer;
	}

	@SubscribeEvent
	public void onRenderGamePostOverlay(RenderGameOverlayEvent.Post event)
	{
		if (event.getType() != ElementType.HOTBAR)
		{
			return;
		}

		Minecraft mc = FMLClientHandler.instance().getClient();
		ConfigDisplayPos.Type displayType = getDisplayType();

		if (!canRenderHUD(mc))
		{
			miningPointPer = -1.0D;

			return;
		}

		ScaledResolution resolution = event.getResolution();
		Miner miner = Miner.get(mc.player, true);

		if (miner == null || miner.getPoint() < 0)
		{
			return;
		}

		MinerRank minerRank = MinerRank.get(miner.getRank());

		String point = Integer.toString(miner.getPoint());
		String rank = I18n.format(minerRank.getUnlocalizedName());

		setDisplayPos(displayType, mc, resolution.getScaledWidth(), resolution.getScaledHeight());

		int x = posX;
		int y = posY;

		RenderItem renderItem = mc.getRenderItem();
		FontRenderer renderer = mc.fontRenderer;
		boolean flag = false;

		MiningCache cache = MiningCache.get(mc.player);
		long processTime = mc.world.getTotalWorldTime() - cache.getLastMiningTime();

		if (cache.getLastMiningTime() > 0L && processTime < 50 && cache.getLastMiningBlock() != null && cache.getLastMiningPoint() != 0)
		{
			IBlockState state = cache.getLastMiningBlock();
			ItemStack stack = new ItemStack(CaveRenderingRegistry.getRenderBlock(state.getBlock()), 1, state.getBlock().getMetaFromState(state));

			RenderHelper.enableGUIStandardItemLighting();
			renderItem.renderItemIntoGUI(stack, x, y);
			renderItem.renderItemOverlayIntoGUI(renderer, stack, x, y, Integer.toString(cache.getLastMiningPoint()));
			RenderHelper.disableStandardItemLighting();

			rank = stack.getDisplayName();
			flag = true;
		}

		if (flag)
		{
			x += displayType.isLeft() ? 20 : -20;
		}

		renderItem.renderItemIntoGUI(minerRank.getIconItem(), x, y);

		GlStateManager.pushMatrix();
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		if (point.length() <= 1)
		{
			point = " " + point;
		}

		MinerRank nextRank = MinerRank.get(miner.getRank() + 1);

		if (minerRank.getRank() < nextRank.getRank())
		{
			String per = String.format("%.2f", calcMiningPointPer(miner.getPoint(), nextRank.getPhase(), false)) + "%";

			point = displayType.isLeft() ? point + " < " + per : per + " > " + point;
		}
		else if (minerRank.getRank() == MinerRank.VALUES.length - 1)
		{
			point = "MAX";
		}

		String combo = null;

		if (cache.getMiningCombo() > 0)
		{
			TextFormatting format = TextFormatting.WHITE;

			if (processTime < 3 * 20)
			{
				format = TextFormatting.BOLD;
			}
			else if (processTime > 12 * 20)
			{
				format = TextFormatting.GRAY;
			}

			combo = format + String.format("%d COMBO!", cache.getMiningCombo()) + TextFormatting.RESET;
		}

		boolean showRank = MiningConfig.showMinerRank;
		boolean hasCombo = combo != null;
		int pointX = displayType.isLeft() ? x + 5 : x + 17 - renderer.getStringWidth(point);
		int pointY = y + 9;
		int rankX = showRank ? displayType.isLeft() ? posX + 5 : posX + 17 - renderer.getStringWidth(rank) : -1;
		int rankY = showRank ? displayType.isTop() ? y + 21 : y - 12 : -1;
		int comboX = hasCombo ? displayType.isLeft() ? posX + 5 : posX + 17 - renderer.getStringWidth(combo) : -1;
		int comboY = hasCombo ? displayType.isTop() ? y + 33 : y - 24 : -1;

		renderer.drawStringWithShadow(point, pointX, pointY, 0xCECECE);

		if (showRank)
		{
			renderer.drawStringWithShadow(rank, rankX, rankY, 0xCECECE);

			if (hasCombo)
			{
				renderer.drawStringWithShadow(combo, comboX, comboY, 0xFFFFFF);
			}
		}

		GlStateManager.enableDepth();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
}