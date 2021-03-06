package cavern.miner.handler;

import java.util.HashSet;
import java.util.Set;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.config.GeneralConfig;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveDimensions;
import cavern.miner.util.BlockPosHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "cavern")
public class CavebornEventHandler
{
	private static final Set<String> FIRST_PLAYERS = new HashSet<>();

	@SubscribeEvent
	public static void onPlayerLoading(final PlayerEvent.LoadFromFile event)
	{
		String uuid = event.getPlayerUUID();

		for (String str : event.getPlayerDirectory().list())
		{
			if (str.startsWith(uuid))
			{
				return;
			}
		}

		DimensionType dim = GeneralConfig.INSTANCE.cavebornSpawn.get().getDimension();

		if (dim == null)
		{
			return;
		}

		event.getPlayer().dimension = dim;

		FIRST_PLAYERS.add(uuid);
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event)
	{
		if (!(event.getPlayer() instanceof ServerPlayerEntity))
		{
			return;
		}

		ServerPlayerEntity player = (ServerPlayerEntity)event.getPlayer();

		if (!FIRST_PLAYERS.remove(player.getCachedUniqueIdString()))
		{
			return;
		}

		DimensionType dim = GeneralConfig.INSTANCE.cavebornSpawn.get().getDimension();

		if (dim == null)
		{
			return;
		}

		CavernPortalBlock portal = CaveDimensions.getPortalBlock(dim);

		if (portal != null)
		{
			player.getCapability(CaveCapabilities.TELEPORTER_CACHE).ifPresent(o -> o.setLastDim(portal.getRegistryName(), DimensionType.OVERWORLD));
		}

		ServerWorld world = player.getServerWorld();
		BlockPos pos = player.getPosition();

		if (!placeEntity(world, pos, player))
		{
			return;
		}

		pos = player.getPosition();

		double posX = pos.getX() + 0.5D;
		double posY = pos.getY() + 0.5D;
		double posZ = pos.getZ() + 0.5D;

		GeneralConfig.INSTANCE.cavebornItems.getItems().forEach(stack -> world.addEntity(new ItemEntity(world, posX, posY, posZ, stack)));

		player.setSpawnDimenion(dim);
		player.setSpawnPoint(pos, true, false, dim);
		player.addPotionEffect(new EffectInstance(Effects.NAUSEA, 120, 0, false, false));

		world.playSound(null, posX, posY, posZ, SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.7F, 0.8F + world.rand.nextFloat() * 0.2F);
	}

	public static boolean placeEntity(final IWorld world, final BlockPos originPos, final Entity entity)
	{
		final BlockPos.Mutable pos = new BlockPos.Mutable();

		BlockPos resultPos = BlockPosHelper.findPos(world, originPos, 64, o ->
		{
			if (world.isAirBlock(pos.setPos(o)) && world.isAirBlock(pos.move(Direction.UP)))
			{
				return world.getBlockState(pos.setPos(o).move(Direction.DOWN)).isSolid();
			}

			return false;
		});

		if (resultPos == null)
		{
			return false;
		}

		entity.setMotion(Vec3d.ZERO);

		double posX = resultPos.getX() + 0.5D;
		double posY = resultPos.getY();
		double posZ = resultPos.getZ() + 0.5D;

		if (entity instanceof ServerPlayerEntity)
		{
			((ServerPlayerEntity)entity).connection.setPlayerLocation(posX, posY, posZ, entity.rotationYaw, entity.rotationPitch);
		}
		else
		{
			entity.setLocationAndAngles(posX, posY, posZ, entity.rotationYaw, entity.rotationPitch);
		}

		return true;
	}
}