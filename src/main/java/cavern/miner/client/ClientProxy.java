package cavern.miner.client;

import cavern.miner.client.gui.DownloadCaveTerrainScreen;
import cavern.miner.client.gui.MinerRecordScreen;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.storage.Miner;
import cavern.miner.storage.MinerRecord;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientProxy
{
	public static PlayerEntity getPlayer()
	{
		Minecraft mc = Minecraft.getInstance();

		return mc.player;
	}

	public static void displayLoadingScreen()
	{
		Minecraft mc = Minecraft.getInstance();

		mc.displayGuiScreen(new DownloadCaveTerrainScreen());
	}

	public static void closeLoadingScreen()
	{
		Minecraft mc = Minecraft.getInstance();

		if (mc.currentScreen != null && mc.currentScreen instanceof DownloadCaveTerrainScreen)
		{
			DownloadCaveTerrainScreen loadScreen = (DownloadCaveTerrainScreen)mc.currentScreen;

			loadScreen.setLoaded();

			mc.displayGuiScreen(null);
		}
	}

	public static void displayMinerRecordScreen()
	{
		Minecraft mc = Minecraft.getInstance();

		if (mc.player != null)
		{
			MinerRecord record = mc.player.getCapability(CaveCapabilities.MINER).map(Miner::getRecord).orElse(null);

			if (record != null)
			{
				mc.displayGuiScreen(new MinerRecordScreen(record));
			}
		}
	}
}