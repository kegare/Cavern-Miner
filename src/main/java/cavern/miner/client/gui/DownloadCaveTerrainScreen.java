package cavern.miner.client.gui;

import javax.annotation.Nullable;

import net.minecraft.client.gui.screen.DownloadTerrainScreen;
import net.minecraft.client.renderer.RenderSkybox;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DownloadCaveTerrainScreen extends DownloadTerrainScreen
{
	public static final RenderSkyboxCube PANORAMA_RESOURCES = new RenderSkyboxCube(new ResourceLocation("cavern", "textures/gui/panorama/cavern"));

	private final boolean showFadeInAnimation;
	private final RenderSkybox panorama = new RenderSkybox(PANORAMA_RESOURCES);

	private long firstRenderTime;

	private boolean loaded;

	public DownloadCaveTerrainScreen()
	{
		this(true);
	}

	public DownloadCaveTerrainScreen(boolean fadeIn)
	{
		super();
		this.showFadeInAnimation = fadeIn;
	}

	@Override
	public void render(int mouseX, int mouseY, float particalTicks)
	{
		if (firstRenderTime == 0L)
		{
			firstRenderTime = Util.milliTime();
		}

		long renderTime = Util.milliTime() - firstRenderTime;
		float f = showFadeInAnimation ? renderTime / 1000.0F : 1.0F;

		fill(0, 0, width, height, -1);

		panorama.render(particalTicks, MathHelper.clamp(f, 0.0F, 0.8F));

		String text = getInfoText(Util.milliTime() - firstRenderTime);

		if (text != null)
		{
			drawCenteredString(font, text, width / 2, height / 2 + 40, 0xFFFFFF);
		}
	}

	@Nullable
	public String getInfoText(long renderTime)
	{
		if (renderTime > 500L)
		{
			return I18n.format("multiplayer.downloadingTerrain");
		}

		return null;
	}

	@Override
	public boolean shouldCloseOnEsc()
	{
		return isLoaded();
	}

	public boolean isLoaded()
	{
		if (loaded)
		{
			return true;
		}

		if (minecraft.player != null && minecraft.player.addedToChunk)
		{
			loaded = true;

			return true;
		}

		return Util.milliTime() - firstRenderTime > 10000L;
	}

	public void setLoaded()
	{
		loaded = true;
	}
}