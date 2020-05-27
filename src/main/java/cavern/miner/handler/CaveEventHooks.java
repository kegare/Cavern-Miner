package cavern.miner.handler;

import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.collect.Sets;

import cavern.miner.api.CavernAPI;
import cavern.miner.api.item.IAquaTool;
import cavern.miner.block.BlockCavernPortal;
import cavern.miner.block.BlockPervertedLeaves;
import cavern.miner.block.BlockPervertedLog;
import cavern.miner.block.BlockPervertedSapling;
import cavern.miner.block.CaveBlocks;
import cavern.miner.config.GeneralConfig;
import cavern.miner.data.Miner;
import cavern.miner.data.PlayerData;
import cavern.miner.network.CaveNetworkRegistry;
import cavern.miner.network.client.CustomSeedMessage;
import cavern.miner.network.client.MiningRecordsMessage;
import cavern.miner.util.PlayerUtils;
import cavern.miner.world.CustomSeed;
import cavern.miner.world.CustomSeedProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.world.BlockEvent.PortalSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public final class CaveEventHooks
{
	public static final Random RANDOM = new Random();

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		sendDataToClient(event.player);
	}

	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event)
	{
		sendDataToClient(event.player);
	}

	private void sendDataToClient(EntityPlayer player)
	{
		if (!CavernAPI.dimension.isInCaverns(player) || player instanceof FakePlayer || !(player instanceof EntityPlayerMP))
		{
			return;
		}

		if (player.world.provider instanceof CustomSeedProvider)
		{
			CustomSeed seed = ((CustomSeedProvider)player.world.provider).getSeedData();

			if (seed != null && seed.getSeedValue() != 0)
			{
				CaveNetworkRegistry.sendTo(() -> new CustomSeedMessage(seed.getSeedValue()), player);
			}
		}

		Miner miner = Miner.get(player, true);

		if (miner != null)
		{
			miner.sendToClient();

			CaveNetworkRegistry.sendTo(() -> new MiningRecordsMessage(miner), player);
		}
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		EntityPlayer player = event.player;

		if (CavernAPI.dimension.isInCaverns(player))
		{
			player.timeUntilPortal = 100;
		}
	}

	@SubscribeEvent
	public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event)
	{
		ItemStack stack = event.getItemStack();

		if (stack.isEmpty())
		{
			return;
		}

		World world = event.getWorld();
		BlockPos pos = event.getPos();
		IBlockState state = world.getBlockState(pos);

		if (state.getBlock() != Blocks.MOSSY_COBBLESTONE && (state.getBlock() != Blocks.STONEBRICK || state.getBlock().getMetaFromState(state) != BlockStoneBrick.MOSSY_META))
		{
			return;
		}

		EntityPlayer player = event.getEntityPlayer();
		Set<BlockCavernPortal> portals = Sets.newHashSet();

		portals.add(CaveBlocks.CAVERN_PORTAL);
		portals.add(CaveBlocks.HUGE_CAVERN_PORTAL);
		portals.add(CaveBlocks.CAVELAND_PORTAL);

		Item portalItem = Items.AIR;

		for (BlockCavernPortal portal : portals)
		{
			if (portal.isTriggerItem(stack))
			{
				portalItem = Item.getItemFromBlock(portal);

				break;
			}
		}

		if (portalItem != Items.AIR)
		{
			EnumFacing facing = ObjectUtils.defaultIfNull(event.getFace(), EnumFacing.UP);
			Vec3d hit = event.getHitVec();
			EnumActionResult result = portalItem.onItemUse(player, world, pos, event.getHand(), facing, (float)hit.x, (float)hit.y, (float)hit.z);

			if (result == EnumActionResult.SUCCESS)
			{
				event.setCancellationResult(result);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onPortalSpawn(PortalSpawnEvent event)
	{
		World world = event.getWorld();

		if (CavernAPI.dimension.isCaverns(world.provider.getDimensionType()))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onPlayerSleepInBed(PlayerSleepInBedEvent event)
	{
		EntityPlayer player = event.getEntityPlayer();

		if (!CavernAPI.dimension.isInCaverns(player))
		{
			return;
		}

		SleepResult result = null;
		World world = player.world;

		if (!world.isRemote)
		{
			PlayerData data = PlayerData.get(player);
			long worldTime = world.getTotalWorldTime();
			long sleepTime = data.getLastSleepTime();
			long requireTime = GeneralConfig.sleepWaitTime * 20;

			if (sleepTime <= 0L)
			{
				sleepTime = worldTime;
				requireTime = 0L;

				data.setLastSleepTime(sleepTime);
			}

			if (requireTime > 0L && sleepTime + requireTime > worldTime)
			{
				result = SleepResult.OTHER_PROBLEM;

				long remainTime = requireTime - (worldTime - sleepTime);
				int min = MathHelper.ceil(remainTime / 20 / 60 + 1);

				player.sendStatusMessage(new TextComponentTranslation("cavern.message.sleep.still", min), true);
			}
		}

		if (result == null)
		{
			result = PlayerUtils.trySleep(player, event.getPos());
		}

		if (!world.isRemote && result == SleepResult.OK)
		{
			PlayerData.get(player).setLastSleepTime(world.getTotalWorldTime());
		}

		event.setResult(result);
	}

	@SubscribeEvent
	public void onPlayerWakeUp(PlayerWakeUpEvent event)
	{
		if (!GeneralConfig.sleepRefresh)
		{
			return;
		}

		EntityPlayer player = event.getEntityPlayer();
		World world = player.world;

		if (world.isRemote || !player.shouldHeal())
		{
			return;
		}

		if (CavernAPI.dimension.isInCaverns(player))
		{
			player.heal(player.getMaxHealth() * 0.25F);
		}
	}

	@SubscribeEvent
	public void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event)
	{
		ItemStack stack = event.getItemStack();
		Block block = Block.getBlockFromItem(stack.getItem());

		if (block == null)
		{
			return;
		}

		if (block instanceof BlockPervertedLog)
		{
			event.setBurnTime(100);
		}
		else if (block instanceof BlockPervertedLeaves || block instanceof BlockPervertedSapling)
		{
			event.setBurnTime(35);
		}
	}

	@SubscribeEvent
	public void onBreakSpeed(BreakSpeed event)
	{
		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand();

		if (stack.isEmpty())
		{
			return;
		}

		float original = event.getOriginalSpeed();

		if (player.isInWater() && stack.getItem() instanceof IAquaTool)
		{
			IAquaTool tool = (IAquaTool)stack.getItem();
			float speed = tool.getAquaBreakSpeed(stack, player, event.getPos(), event.getState(), original);

			if (EnchantmentHelper.getAquaAffinityModifier(player))
			{
				speed *= 0.5F;
			}

			event.setNewSpeed(speed);
		}
	}
}