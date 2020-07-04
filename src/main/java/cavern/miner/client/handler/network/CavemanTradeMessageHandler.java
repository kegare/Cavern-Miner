package cavern.miner.client.handler.network;

import cavern.miner.client.gui.CavemanTradeScreen;
import cavern.miner.entity.CavemanEntity;
import cavern.miner.network.CavemanTradeMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
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

		if (mc.world == null)
		{
			return;
		}

		Entity entity = mc.world.getEntityByID(msg.getEntityId());
		CavemanEntity caveman;

		if (entity != null && entity instanceof CavemanEntity)
		{
			caveman = (CavemanEntity)entity;
		}
		else
		{
			caveman = null;
		}

		mc.displayGuiScreen(new CavemanTradeScreen(caveman, msg.getEntries()));
	}
}