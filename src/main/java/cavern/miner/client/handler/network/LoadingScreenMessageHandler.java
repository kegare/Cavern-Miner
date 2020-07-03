package cavern.miner.client.handler.network;

import cavern.miner.client.gui.DownloadCaveTerrainScreen;
import cavern.miner.network.LoadingScreenMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;

@OnlyIn(Dist.CLIENT)
public class LoadingScreenMessageHandler implements SafeRunnable
{
	private final LoadingScreenMessage msg;

	public LoadingScreenMessageHandler(LoadingScreenMessage msg)
	{
		this.msg = msg;
	}

	@Override
	public void run()
	{
		Minecraft mc = Minecraft.getInstance();

		switch (msg.getStage())
		{
			case LOAD:
				if (mc.currentScreen == null || !(mc.currentScreen instanceof DownloadCaveTerrainScreen))
				{
					mc.displayGuiScreen(new DownloadCaveTerrainScreen());
				}

				break;
			case DONE:
				if (mc.currentScreen != null && mc.currentScreen instanceof DownloadCaveTerrainScreen)
				{
					DownloadCaveTerrainScreen loadScreen = (DownloadCaveTerrainScreen)mc.currentScreen;

					loadScreen.setLoaded();

					mc.displayGuiScreen(null);
				}

				break;
			default:
		}
	}
}