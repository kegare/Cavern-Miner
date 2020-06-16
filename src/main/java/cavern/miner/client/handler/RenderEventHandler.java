package cavern.miner.client.handler;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import cavern.miner.config.client.ClientConfig;
import cavern.miner.world.dimension.CavernDimension;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "cavern", value = Dist.CLIENT)
public class RenderEventHandler
{
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void setupFog(final EntityViewRenderEvent.FogDensity event)
	{
		if (!ClientConfig.INSTANCE.caveFog.get())
		{
			return;
		}

		if (event.getType() != FogRenderer.FogType.FOG_TERRAIN)
		{
			return;
		}

		Entity entity = event.getInfo().getRenderViewEntity();

		if (entity == null || !(entity.world.getDimension() instanceof CavernDimension))
		{
			return;
		}

		CavernDimension cavern = (CavernDimension)entity.world.getDimension();
		float density = cavern.getFogDensity(entity);

		if (density > 0.0F)
		{
			RenderSystem.fogMode(GlStateManager.FogMode.EXP2);

			event.setDensity(density);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void renderFog(final EntityViewRenderEvent.FogColors event)
	{
		if (!ClientConfig.INSTANCE.caveFog.get())
		{
			return;
		}

		Entity entity = event.getInfo().getRenderViewEntity();

		if (entity == null || !(entity.world.getDimension() instanceof CavernDimension))
		{
			return;
		}

		CavernDimension cavern = (CavernDimension)entity.world.getDimension();
		float depth = cavern.getFogDepth(entity);

		if (depth > 0.0F)
		{
			float red = event.getRed();
			float green = event.getGreen();
			float blue = event.getBlue();
			float f = 1.0F / red;

			if (f > 1.0F / green)
			{
				f = 1.0F / green;
			}

			if (f > 1.0F / blue)
			{
				f = 1.0F / blue;
			}

			event.setRed(red * (1.0F - depth) + red * f * depth);
			event.setGreen(green * (1.0F - depth) + green * f * depth);
			event.setBlue(blue * (1.0F - depth) + blue * f * depth);
		}
	}
}