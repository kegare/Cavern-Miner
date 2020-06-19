package cavern.miner.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IRenderHandler;

@OnlyIn(Dist.CLIENT)
public class EmptyRenderer implements IRenderHandler
{
	public static final EmptyRenderer INSTANCE = new EmptyRenderer();

	@Override
	public void render(int ticks, float partialTicks, ClientWorld world, Minecraft mc) {}
}