package cavern.miner.event;

import cavern.miner.init.CaveCapabilities;
import cavern.miner.network.CaveNetworkConstants;
import cavern.miner.network.MiningInteractMessage;
import cavern.miner.storage.Miner;
import cavern.miner.util.BlockStateHelper;
import cavern.miner.vein.OrePointHelper;
import cavern.miner.vein.OreRegistry;
import cavern.miner.world.CavernDimension;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = "cavern")
public class MinerEventHandler
{
	@SubscribeEvent
	public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event)
	{
		PlayerEntity player = event.getPlayer();

		player.getCapability(CaveCapabilities.MINER).ifPresent(Miner::sendToClient);
	}

	@SubscribeEvent
	public static void onPlayerChangedDimension(final PlayerEvent.PlayerChangedDimensionEvent event)
	{
		PlayerEntity player = event.getPlayer();

		player.getCapability(CaveCapabilities.MINER).ifPresent(Miner::sendToClient);
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		PlayerEntity player = event.getPlayer();

		if (player.world.getDimension() instanceof CavernDimension)
		{
			player.timeUntilPortal = 100;
		}

		player.getCapability(CaveCapabilities.MINER).ifPresent(Miner::sendToClient);
	}

	@SubscribeEvent
	public static void onBlockBreak(final BreakEvent event)
	{
		PlayerEntity breaker = event.getPlayer();

		if (breaker == null || breaker instanceof FakePlayer || !(breaker instanceof ServerPlayerEntity))
		{
			return;
		}

		ServerPlayerEntity player = (ServerPlayerEntity)breaker;
		Miner miner = player.getCapability(CaveCapabilities.MINER).orElse(null);

		if (miner == null)
		{
			return;
		}

		ServerWorld world = player.getServerWorld();

		if (!(world.dimension instanceof CavernDimension))
		{
			return;
		}

		ItemStack stack = player.getHeldItemMainhand();

		if (!stack.getToolTypes().contains(ToolType.PICKAXE))
		{
			return;
		}

		BlockPos pos = event.getPos();
		BlockState state = event.getState();

		if (BlockStateHelper.equals(state, PlacedCache.get(world.getServer().getFolderName(), world.dimension).getCache(pos)))
		{
			return;
		}

		int point = OrePointHelper.getPoint(OreRegistry.getEntry(state));

		if (point == 0)
		{
			return;
		}

		miner.addPoint(point).sendToClient();
		miner.getMiningCache().put(state, point);

		CaveNetworkConstants.PLAY.send(PacketDistributor.PLAYER.with(() -> player), new MiningInteractMessage(state, point));
	}

	@SubscribeEvent
	public static void onBlockPlace(final BlockEvent.EntityPlaceEvent event)
	{
		Entity placer = event.getEntity();

		if (placer == null || placer instanceof FakePlayer || !(placer instanceof ServerPlayerEntity))
		{
			return;
		}

		ServerPlayerEntity player = (ServerPlayerEntity)placer;
		ServerWorld world = player.getServerWorld();

		if (!(world.dimension instanceof CavernDimension))
		{
			return;
		}

		BlockState state = event.getPlacedBlock();
		int point = OrePointHelper.getPoint(OreRegistry.getEntry(state));

		if (point == 0)
		{
			return;
		}

		PlacedCache.get(world.getServer().getFolderName(), world.dimension).addCache(event.getPos(), state);
	}

	@SubscribeEvent
	public static void onLivingUpdate(final LivingUpdateEvent event)
	{
		LivingEntity entity = event.getEntityLiving();

		if (entity.ticksExisted % 10 == 0 && entity instanceof PlayerEntity)
		{
			entity.getCapability(CaveCapabilities.MINER).ifPresent(o -> o.getMiningCache().updateCombo());
		}
	}
}