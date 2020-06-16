package cavern.miner.handler;

import javax.annotation.Nullable;

import cavern.miner.enchantment.EnchantmentMiner;
import cavern.miner.enchantment.MinerSnapshot;
import cavern.miner.enchantment.MinerUnit;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveEnchantments;
import cavern.miner.storage.Miner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
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

		mining.setBreaking(true);

		for (BlockPos target : snapshot.getTargets())
		{
			if (snapshot.validTarget(target) && !harvestBlock(im, target))
			{
				break;
			}
		}

		mining.setBreaking(false);
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
					world.playEvent(Constants.WorldEvents.BREAK_BLOCK_EFFECTS, pos, Block.getStateId(state));
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
}