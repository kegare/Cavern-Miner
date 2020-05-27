package cavern.miner.client;

import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.collect.Maps;

import cavern.miner.client.renderer.RenderCaveman;
import cavern.miner.client.renderer.RenderCavenicCreeper;
import cavern.miner.client.renderer.RenderCavenicSkeleton;
import cavern.miner.client.renderer.RenderCavenicSpider;
import cavern.miner.client.renderer.RenderCavenicWitch;
import cavern.miner.client.renderer.RenderCavenicZombie;
import cavern.miner.client.renderer.RenderDurangHog;
import cavern.miner.entity.monster.EntityCaveman;
import cavern.miner.entity.monster.EntityCavenicCreeper;
import cavern.miner.entity.monster.EntityCavenicSkeleton;
import cavern.miner.entity.monster.EntityCavenicSpider;
import cavern.miner.entity.monster.EntityCavenicWitch;
import cavern.miner.entity.monster.EntityCavenicZombie;
import cavern.miner.entity.passive.EntityDurangHog;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class CaveRenderingRegistry
{
	private static final Map<Block, Block> RENDER_BLOCK_MAP = Maps.newHashMap();

	public static void registerRenderers()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityCavenicSkeleton.class, RenderCavenicSkeleton::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityCavenicCreeper.class, RenderCavenicCreeper::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityCavenicZombie.class, RenderCavenicZombie::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityCavenicSpider.class, RenderCavenicSpider::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityCavenicWitch.class, RenderCavenicWitch::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityCaveman.class, RenderCaveman::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityDurangHog.class, RenderDurangHog::new);
	}

	public static void registerRenderBlocks()
	{
		RENDER_BLOCK_MAP.put(Blocks.LIT_REDSTONE_ORE, Blocks.REDSTONE_ORE);
	}

	public static Block getRenderBlock(Block block)
	{
		Block ret = RENDER_BLOCK_MAP.get(block);

		return ObjectUtils.defaultIfNull(ret, block);
	}
}