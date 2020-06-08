package cavern.miner.handler;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import cavern.miner.config.GeneralConfig;
import cavern.miner.enchantment.EnchantmentMiner;
import cavern.miner.enchantment.MinerSnapshot;
import cavern.miner.enchantment.MinerUnit;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveEnchantments;
import cavern.miner.storage.Miner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "cavern")
public class MinerUnitEventHandler
{
	@SubscribeEvent
	public static void onBlockBreak(final BreakEvent event)
	{
		if (event.getWorld().isRemote() || !(event.getWorld() instanceof World))
		{
			return;
		}

		World world = (World)event.getWorld();
		PlayerEntity player = event.getPlayer();

		if (player == null || player instanceof FakePlayer || !(player instanceof ServerPlayerEntity))
		{
			return;
		}

		MinerUnit mining = player.getCapability(CaveCapabilities.MINER).map(Miner::getUnit).orElse(null);

		if (mining == null)
		{
			return;
		}

		BlockPos pos = event.getPos();

		if (mining.addExperience(pos, event.getExpToDrop()))
		{
			event.setExpToDrop(0);
		}

		if (mining.isBreaking())
		{
			return;
		}

		ItemStack held = player.getHeldItemMainhand();
		BlockState state = event.getState();
		EnchantmentMiner miner = CaveEnchantments.getMinerEnchantment(held);

		if (miner == null || !miner.isEffectiveTarget(held, state))
		{
			return;
		}

		MinerSnapshot snapshot = mining.getSnapshot(miner, pos, state);

		if (snapshot == null || snapshot.isEmpty())
		{
			return;
		}

		PlayerInteractionManager im = ((ServerPlayerEntity)player).interactionManager;

		mining.captureDrops(GeneralConfig.INSTANCE.collectDrops.get());
		mining.captureExperiences(GeneralConfig.INSTANCE.collectExps.get());

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

		if (experiences != null && !experiences.isEmpty() && !im.isCreative() && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
		{
			int exp = experiences.values().stream().mapToInt(Integer::intValue).sum();

			while (exp > 0)
			{
				int i = ExperienceOrbEntity.getXPSplit(exp);
				exp -= i;

				world.addEntity(new ExperienceOrbEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, i));
			}
		}
	}

	private static boolean harvestBlock(PlayerInteractionManager interact, @Nullable BlockPos pos)
	{
		if (pos == null)
		{
			return false;
		}

		ServerWorld world = interact.world;

		if (!world.getServer().isDedicatedServer())
		{
			BlockState state = world.getBlockState(pos);

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
	public static void onHarvestDrops(final HarvestDropsEvent event)
	{
		if (event.getWorld().isRemote() || !(event.getWorld() instanceof World))
		{
			return;
		}

		PlayerEntity player = event.getHarvester();

		if (player == null || player instanceof FakePlayer)
		{
			return;
		}

		MinerUnit mining = player.getCapability(CaveCapabilities.MINER).map(Miner::getUnit).orElse(null);

		if (mining == null)
		{
			return;
		}

		if (!mining.getCaptureDrops())
		{
			return;
		}

		NonNullList<ItemStack> items = NonNullList.create();
		List<ItemStack> drops = event.getDrops();
		float chance = event.getDropChance();

		for (ItemStack stack : drops)
		{
			if (player.getRNG().nextFloat() < chance)
			{
				items.add(stack);
			}
		}

		drops.clear();

		mining.addDrops(event.getPos(), items);
	}
}