package cavern.miner.init;

import cavern.miner.client.renderer.CavenicSkeletonRenderer;
import cavern.miner.entity.CavenicSkeletonEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CaveEntities
{
	public static final DeferredRegister<EntityType<?>> REGISTRY = new DeferredRegister<>(ForgeRegistries.ENTITIES, "cavern");

	public static final RegistryObject<EntityType<CavenicSkeletonEntity>> CAVENIC_SKELETON = REGISTRY.register("cavenic_skeleton",
		() -> EntityType.Builder.create(CavenicSkeletonEntity::new, EntityClassification.MONSTER).immuneToFire().size(0.6F, 2.2F).build("cavern:cavenic_skeleton"));

	public static void registerSpawnPlacements()
	{
		CAVENIC_SKELETON.ifPresent(o -> EntitySpawnPlacementRegistry.register(o,
			EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MonsterEntity::canMonsterSpawnInLight));
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerRenderers()
	{
		CAVENIC_SKELETON.ifPresent(o -> RenderingRegistry.registerEntityRenderingHandler(o, CavenicSkeletonRenderer::new));
	}
}