package cavern.miner.client.renderer;

import cavern.miner.client.renderer.model.CavemanModel;
import cavern.miner.entity.CavemanEntity;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CavemanRenderer extends BipedRenderer<CavemanEntity, CavemanModel<CavemanEntity>>
{
	private static final ResourceLocation CAVEMAN_TEXTURES = new ResourceLocation("cavern", "textures/entity/caveman.png");

	public CavemanRenderer(EntityRendererManager renderManager)
	{
		super(renderManager, new CavemanModel<>(), 0.5F);
	}

	@Override
	public ResourceLocation getEntityTexture(CavemanEntity entity)
	{
		return CAVEMAN_TEXTURES;
	}
}