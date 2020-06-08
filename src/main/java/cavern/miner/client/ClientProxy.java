package cavern.miner.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientProxy
{
	public static PlayerEntity getClientPlayer()
	{
		Minecraft mc = Minecraft.getInstance();

		return mc.player;
	}
}