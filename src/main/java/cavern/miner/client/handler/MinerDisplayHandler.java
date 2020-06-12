package cavern.miner.client.handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import cavern.miner.client.ItemStackCache;
import cavern.miner.config.client.ClientConfig;
import cavern.miner.config.client.DisplayCorner;
import cavern.miner.config.client.DisplayType;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.storage.Miner;
import cavern.miner.storage.MinerCache;
import cavern.miner.storage.MinerRank;
import cavern.miner.world.CavernDimension;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "cavern", value = Dist.CLIENT)
public class MinerDisplayHandler
{
	private static int posX;
	private static int posY;
	private static double pointPer;

	private static boolean canDisplay()
	{
		DisplayType type = ClientConfig.INSTANCE.displayType.get();

		if (type == DisplayType.HIDDEN)
		{
			return false;
		}

		Minecraft mc = Minecraft.getInstance();

		if (!(mc.world.dimension instanceof CavernDimension))
		{
			return false;
		}

		for (ItemStack stack : mc.player.getHeldEquipment())
		{
			if (stack.getToolTypes().contains(ToolType.PICKAXE))
			{
				return true;
			}
		}

		return mc.player.isCreative() || type == DisplayType.ALWAYS;
	}

	private static void setDisplayPosition(DisplayCorner corner, int width, int height)
	{
		switch (corner)
		{
			case TOP_RIGHT:
				posX = width - 20;
				posY = 5;

				Minecraft mc = Minecraft.getInstance();

				if (!mc.player.getActivePotionMap().isEmpty())
				{
					posY = 30;
				}

				break;
			case TOP_LEFT:
				posX = 5;
				posY = 5;
				break;
			case BOTTOM_RIGHT:
				posX = width - 20;
				posY = height - 21;
				break;
			case BOTTOM_LEFT:
				posX = 5;
				posY = height - 21;
				break;
		}
	}

	private static double calcPointPer(int point, int phase)
	{
		double per = point == 0 ? 0.0D : (double)point / (double)phase * 100.0D;
		double diff = Math.abs(per - pointPer);
		double move = diff * 0.1D;

		if (pointPer < 0.0D)
		{
			pointPer = per;
		}
		else if (per > pointPer)
		{
			pointPer += move;
		}
		else if (per < pointPer)
		{
			pointPer -= move;
		}

		return Math.max(pointPer, 0.0D);
	}

	@SubscribeEvent
	public static void renderOverlay(RenderGameOverlayEvent.Post event)
	{
		if (event.getType() != ElementType.HOTBAR)
		{
			return;
		}

		if (!canDisplay())
		{
			pointPer = -1.0D;

			return;
		}

		Minecraft mc = Minecraft.getInstance();
		Miner miner = mc.player.getCapability(CaveCapabilities.MINER).orElse(null);

		if (miner == null)
		{
			return;
		}

		MinerRank.DisplayEntry rank = miner.getDisplayRank();

		if (rank == null)
		{
			return;
		}

		MainWindow window = event.getWindow();
		DisplayCorner corner = ClientConfig.INSTANCE.displayConer.get();

		setDisplayPosition(corner, window.getScaledWidth(), window.getScaledHeight());

		int x = posX;
		int y = posY;
		String pointText = Integer.toString(miner.getPoint());
		String rankText = rank.getDisplayName();

		ItemRenderer itemRenderer = mc.getItemRenderer();
		FontRenderer fontRenderer = mc.fontRenderer;

		MinerCache cache = miner.getCache();
		BlockState lastBlock = cache.getLastBlock();
		long diffTime = System.currentTimeMillis() - cache.getLastTime();

		if (ClientConfig.INSTANCE.displayType.get() == DisplayType.ACTION)
		{
			if (lastBlock == null || diffTime > 15000L)
			{
				return;
			}
		}

		boolean showLastMine = false;

		if (lastBlock != null && diffTime <= 3000L)
		{
			ItemStack stack = ItemStackCache.get(lastBlock.getBlock());

			RenderSystem.enableRescaleNormal();
			itemRenderer.renderItemIntoGUI(stack, x, y);
			itemRenderer.renderItemOverlayIntoGUI(fontRenderer, stack, x, y, Integer.toString(cache.getLastPoint()));
			RenderSystem.disableRescaleNormal();

			rankText = stack.getDisplayName().getUnformattedComponentText();

			showLastMine = true;
		}

		if (showLastMine)
		{
			x += corner.isLeft() ? 20 : -20;
		}

		RenderSystem.enableRescaleNormal();
		itemRenderer.renderItemIntoGUI(rank.getIconItem(), x, y);
		RenderSystem.disableRescaleNormal();

		if (pointText.length() <= 1)
		{
			pointText = " " + pointText;
		}

		int nextPhase = rank.getNextPhase();

		if (nextPhase > 0)
		{
			String per = String.format("%.2f", calcPointPer(miner.getPoint(), nextPhase)) + "%";

			pointText = corner.isLeft() ? pointText + " < " + per : per + " > " + pointText;
		}
		else
		{
			pointText = "MAX";
		}

		String comboText = null;

		if (cache.getCombo() > 0)
		{
			TextFormatting format = TextFormatting.WHITE;

			if (diffTime < 3000L)
			{
				format = TextFormatting.BOLD;
			}
			else if (diffTime > 8000L)
			{
				format = TextFormatting.GRAY;
			}

			comboText = format + String.format("%d COMBO!", cache.getCombo()) + TextFormatting.RESET;
		}

		RenderSystem.pushMatrix();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		MatrixStack matrix = new MatrixStack();
		IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());

		matrix.push();
		matrix.translate(0.0D, 0.0D, itemRenderer.zLevel + 200.0F);

		int x2 = corner.isLeft() ? x + 5 : x + 17 - fontRenderer.getStringWidth(pointText);
		int z2 = y + 9;

		fontRenderer.renderString(pointText, x2, z2, 0xCECECE, true, matrix.getLast().getMatrix(), buffer, false, 0, 15728880);

		matrix.pop();
		buffer.finish();

		if (ClientConfig.INSTANCE.showRank.get())
		{
			matrix = new MatrixStack();
			buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());

			matrix.push();
			matrix.translate(0.0D, 0.0D, itemRenderer.zLevel + 200.0F);

			x2 = corner.isLeft() ? posX + 5 : posX + 17 - fontRenderer.getStringWidth(rankText);
			z2 = corner.isTop() ? y + 21 : y - 12;

			fontRenderer.renderString(rankText, x2, z2, 0xCECECE, true, matrix.getLast().getMatrix(), buffer, false, 0, 15728880);

			matrix.pop();
			buffer.finish();
		}

		if (comboText != null)
		{
			matrix = new MatrixStack();
			buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());

			matrix.push();
			matrix.translate(0.0D, 0.0D, itemRenderer.zLevel + 200.0F);

			x2 = corner.isLeft() ? posX + 5 : posX + 17 - fontRenderer.getStringWidth(comboText);
			z2 = corner.isTop() ? y + 33 : y - 24;

			fontRenderer.renderString(comboText, x2, z2, 0xFFFFFF, true, matrix.getLast().getMatrix(), buffer, false, 0, 15728880);

			matrix.pop();
			buffer.finish();
		}

		RenderSystem.disableBlend();
		RenderSystem.popMatrix();
	}
}