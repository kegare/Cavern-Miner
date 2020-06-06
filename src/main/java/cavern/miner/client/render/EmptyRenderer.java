package cavern.miner.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IRenderHandler;

public class EmptyRenderer implements IRenderHandler
{
	public static final EmptyRenderer INSTANCE = new EmptyRenderer();

	@OnlyIn(Dist.CLIENT)
	@Override
	public void render(int ticks, float partialTicks, ClientWorld world, Minecraft mc) {}
}