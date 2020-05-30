package cavern.miner.handler;

import java.util.Set;

import com.google.common.collect.Sets;

import cavern.miner.block.BlockCavernPortal;
import cavern.miner.config.GeneralConfig;
import cavern.miner.config.property.ConfigCaveborn;
import cavern.miner.data.PortalCache;
import cavern.miner.util.ItemMeta;
import cavern.miner.util.PlayerUtils;
import cavern.miner.world.CaveDimensions;
import cavern.miner.world.TeleporterCavern;
import net.minecraft.block.state.pattern.BlockPattern.PatternHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public final class CavebornEventHooks
{
	private final Set<String> firstPlayers = Sets.newHashSet();

	@SubscribeEvent
	public void onPlayerLoadFromFile(PlayerEvent.LoadFromFile event)
	{
		String uuid = event.getPlayerUUID();

		for (String str : event.getPlayerDirectory().list())
		{
			if (str.startsWith(uuid))
			{
				return;
			}
		}

		ConfigCaveborn.Type caveborn = GeneralConfig.caveborn.getType();

		if (caveborn == ConfigCaveborn.Type.DISABLED)
		{
			return;
		}

		BlockCavernPortal portal = caveborn.getPortalBlock();

		if (portal == null || portal.getDimension() == null)
		{
			return;
		}

		EntityPlayer player = event.getEntityPlayer();
		int dimension = portal.getDimension().getId();

		player.dimension = dimension;
		player.setSpawnDimension(dimension);

		firstPlayers.add(uuid);
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event)
	{
		if (!(event.player instanceof EntityPlayerMP))
		{
			return;
		}

		EntityPlayerMP player = (EntityPlayerMP)event.player;

		if (!firstPlayers.contains(player.getCachedUniqueIdString()))
		{
			return;
		}

		WorldServer world = player.getServerWorld();
		ConfigCaveborn.Type caveborn = GeneralConfig.caveborn.getType();
		BlockCavernPortal portal = caveborn.getPortalBlock();
		BlockPos pos = player.getPosition();
		PortalCache cache = PortalCache.get(player);
		PatternHelper pattern = portal.createPatternHelper(world, pos);
		double d0 = pattern.getForwards().getAxis() == EnumFacing.Axis.X ? (double)pattern.getFrontTopLeft().getZ() : (double)pattern.getFrontTopLeft().getX();
		double d1 = pattern.getForwards().getAxis() == EnumFacing.Axis.X ? player.posZ : player.posX;
		d1 = Math.abs(MathHelper.pct(d1 - (pattern.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 1 : 0), d0, d0 - pattern.getWidth()));
		double d2 = MathHelper.pct(player.posY - 1.0D, pattern.getFrontTopLeft().getY(), pattern.getFrontTopLeft().getY() - pattern.getHeight());

		cache.setLastPortalVec(new Vec3d(d1, d2, 0.0D));
		cache.setTeleportDirection(pattern.getForwards());

		player.timeUntilPortal = 200;

		new TeleporterCavern(world, portal).placeEntity(world, player, player.rotationYaw);

		firstPlayers.remove(event.player.getCachedUniqueIdString());

		DimensionType type = portal.getDimension();

		PlayerUtils.grantCriterion(player, "root", "entered_cavern");

		if (type != CaveDimensions.CAVERN)
		{
			String name = type.getName();

			PlayerUtils.grantCriterion(player, "enter_the_" + name, "entered_" + name);
		}

		pos = player.getPosition();

		for (BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1), pos.add(1, 1, 1)))
		{
			if (world.getBlockState(blockpos).getBlock() == portal)
			{
				world.setBlockToAir(blockpos);

				break;
			}
		}

		player.setSpawnChunk(pos, true, player.dimension);

		double x = player.posX;
		double y = player.posY + 0.25D;
		double z = player.posZ;

		world.playSound(null, x, y, z, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1.0F, 0.65F);

		for (ItemMeta itemMeta : GeneralConfig.cavebornBonusItems.getItems())
		{
			ItemStack stack = itemMeta.getItemStack();

			if (stack.isStackable())
			{
				stack = itemMeta.getItemStack(MathHelper.getInt(CaveEventHooks.RANDOM, 4, 16));
			}

			InventoryHelper.spawnItemStack(world, x, y, z, stack);
		}
	}
}