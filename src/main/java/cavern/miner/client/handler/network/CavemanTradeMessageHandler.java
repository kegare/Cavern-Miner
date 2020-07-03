package cavern.miner.client.handler.network;

import cavern.miner.client.gui.CavemanTradeScreen;
import cavern.miner.network.CavemanTradeMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;

@OnlyIn(Dist.CLIENT)
public class CavemanTradeMessageHandler implements SafeRunnable
{
	private final CavemanTradeMessage msg;

	public CavemanTradeMessageHandler(CavemanTradeMessage msg)
	{
		this.msg = msg;
	}

	@Override
	public void run()
	{
		Minecraft mc = Minecraft.getInstance();

		mc.displayGuiScreen(new CavemanTradeScreen(msg.getEntries()));
	}
}