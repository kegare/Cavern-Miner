package cavern.miner.client;

import cavern.miner.proxy.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public PlayerEntity getClientPlayer()
	{
		Minecraft mc = Minecraft.getInstance();

		return mc.player;
	}
}