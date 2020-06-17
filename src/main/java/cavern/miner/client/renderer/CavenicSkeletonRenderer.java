package cavern.miner.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CavenicSkeletonRenderer extends SkeletonRenderer
{
	private static final ResourceLocation SKELETON_TEXTURES = new ResourceLocation("cavern", "textures/entity/cavenic_skeleton.png");

	public CavenicSkeletonRenderer(EntityRendererManager renderManager)
	{
		super(renderManager);
	}

	@Override
	public ResourceLocation getEntityTexture(AbstractSkeletonEntity entity)
	{
		return SKELETON_TEXTURES;
	}
}