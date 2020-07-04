package cavern.miner.init;

import cavern.miner.client.renderer.CavemanRenderer;
import cavern.miner.client.renderer.CavenicSkeletonRenderer;
import cavern.miner.entity.CavemanEntity;
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

public final class CaveEntities
{
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITIES, "cavern");

	public static final RegistryObject<EntityType<CavemanEntity>> CAVEMAN = REGISTRY.register("caveman",
		() -> EntityType.Builder.create(CavemanEntity::new, EntityClassification.AMBIENT).immuneToFire().size(0.48F, 1.85F).build("cavern:caveman"));

	public static final RegistryObject<EntityType<CavenicSkeletonEntity>> CAVENIC_SKELETON = REGISTRY.register("cavenic_skeleton",
		() -> EntityType.Builder.create(CavenicSkeletonEntity::new, EntityClassification.MONSTER).immuneToFire().size(0.6F, 2.2F).build("cavern:cavenic_skeleton"));

	public static void registerSpawnPlacements()
	{
		EntitySpawnPlacementRegistry.register(CAVEMAN.get(), EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
			Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, CavemanEntity::canSpawnInLight);
		EntitySpawnPlacementRegistry.register(CAVENIC_SKELETON.get(), EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
			Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MonsterEntity::canMonsterSpawnInLight);
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerRenderers()
	{
		RenderingRegistry.registerEntityRenderingHandler(CAVEMAN.get(), CavemanRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(CAVENIC_SKELETON.get(), CavenicSkeletonRenderer::new);
	}
}