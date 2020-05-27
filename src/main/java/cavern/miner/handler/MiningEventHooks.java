package cavern.miner.handler;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import cavern.miner.config.MiningConfig;
import cavern.miner.core.CavernMod;
import cavern.miner.enchantment.CaveEnchantments;
import cavern.miner.enchantment.EnchantmentMiner;
import cavern.miner.enchantment.MiningSnapshot;
import cavern.miner.enchantment.MiningUnit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class MiningEventHooks
{
	@SubscribeEvent
	public void onBlockBreak(BreakEvent event)
	{
		World world = event.getWorld();

		if (world.isRemote)
		{
			return;
		}

		EntityPlayer player = event.getPlayer();

		if (player == null || player instanceof FakePlayer)
		{
			return;
		}

		BlockPos pos = event.getPos();
		MiningUnit mining = MiningUnit.get(player);

		if (mining.addExperience(pos, event.getExpToDrop()))
		{
			event.setExpToDrop(0);
		}

		if (mining.isBreaking())
		{
			return;
		}

		if (!(player instanceof EntityPlayerMP))
		{
			return;
		}

		ItemStack held = player.getHeldItemMainhand();
		IBlockState state = event.getState();
		EnchantmentMiner miner = CaveEnchantments.getMinerEnchantment(held);

		if (miner == null || !miner.isEffectiveTarget(held, state))
		{
			return;
		}

		MiningSnapshot snapshot = mining.getSnapshot(miner, pos, state);

		if (snapshot == null || snapshot.isEmpty())
		{
			return;
		}

		PlayerInteractionManager im = ((EntityPlayerMP)player).interactionManager;

		mining.captureDrops(MiningConfig.collectDrops);
		mining.captureExperiences(MiningConfig.collectExps);

		mining.setBreaking(true);

		for (BlockPos target : snapshot.getTargets())
		{
			if (snapshot.validTarget(target) && !harvestBlock(im, target))
			{
				break;
			}
		}

		mining.setBreaking(false);

		Map<BlockPos, NonNullList<ItemStack>> drops = mining.captureDrops(false);

		if (drops != null && !drops.isEmpty())
		{
			for (NonNullList<ItemStack> items : drops.values())
			{
				for (ItemStack stack : items)
				{
					Block.spawnAsEntity(world, pos, stack);
				}
			}
		}

		Map<BlockPos, Integer> experiences = mining.captureExperiences(false);

		if (experiences != null && !experiences.isEmpty() && !im.isCreative() && world.getGameRules().getBoolean("doTileDrops"))
		{
			int exp = experiences.values().stream().mapToInt(Integer::intValue).sum();

			while (exp > 0)
			{
				int i = EntityXPOrb.getXPSplit(exp);
				exp -= i;

				world.spawnEntity(new EntityXPOrb(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, i));
			}
		}
	}

	private boolean harvestBlock(PlayerInteractionManager interact, @Nullable BlockPos pos)
	{
		if (pos == null)
		{
			return false;
		}

		if (CavernMod.proxy.isSinglePlayer())
		{
			World world = interact.world;
			IBlockState state = world.getBlockState(pos);

			if (interact.tryHarvestBlock(pos))
			{
				if (!interact.isCreative())
				{
					world.playEvent(2001, pos, Block.getStateId(state));
				}

				return true;
			}
		}
		else if (interact.tryHarvestBlock(pos))
		{
			return true;
		}

		return false;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onHarvestDrops(HarvestDropsEvent event)
	{
		World world = event.getWorld();

		if (world.isRemote)
		{
			return;
		}

		EntityPlayer player = event.getHarvester();

		if (player == null || player instanceof FakePlayer)
		{
			return;
		}

		BlockPos pos = event.getPos();
		MiningUnit mining = MiningUnit.get(player);

		if (!mining.getCaptureDrops())
		{
			return;
		}

		NonNullList<ItemStack> items = NonNullList.create();
		List<ItemStack> drops = event.getDrops();
		float chance = event.getDropChance();

		for (ItemStack stack : drops)
		{
			if (CaveEventHooks.RANDOM.nextFloat() < chance)
			{
				items.add(stack);
			}
		}

		drops.clear();

		mining.addDrops(pos, items);
	}
}