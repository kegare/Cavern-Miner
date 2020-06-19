package cavern.miner.client.handler;

import cavern.miner.client.gui.DownloadCaveTerrainScreen;
import cavern.miner.world.dimension.CavernDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DownloadTerrainScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "cavern", value = Dist.CLIENT)
public class ClientEventHandler
{
	@SubscribeEvent
	public static void onOpenGui(GuiOpenEvent event)
	{
		Minecraft mc = Minecraft.getInstance();

		if (!mc.isIntegratedServerRunning() || mc.world == null || mc.player == null)
		{
			return;
		}

		if (event.getGui() != null && event.getGui().getClass() == DownloadTerrainScreen.class)
		{
			if (mc.world.getDimension() instanceof CavernDimension && mc.player.timeUntilPortal > 0)
			{
				event.setGui(new DownloadCaveTerrainScreen());
			}
		}
		else if (mc.currentScreen != null && mc.currentScreen instanceof DownloadCaveTerrainScreen)
		{
			DownloadCaveTerrainScreen loadScreen = (DownloadCaveTerrainScreen)mc.currentScreen;

			if (!loadScreen.isLoaded())
			{
				event.setCanceled(true);
			}
		}
	}
}