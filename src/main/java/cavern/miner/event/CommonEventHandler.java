package cavern.miner.event;

import org.apache.commons.lang3.ObjectUtils;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.init.CaveBlocks;
import cavern.miner.item.CaveItemTier;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.ToolItem;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "cavern")
public class CommonEventHandler
{
	@SubscribeEvent
	public static void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event)
	{
		ItemStack stack = event.getItemStack();

		if (stack.isEmpty())
		{
			return;
		}

		World world = event.getWorld();
		BlockPos pos = event.getPos();
		BlockState state = world.getBlockState(pos);

		for (CavernPortalBlock portal : CaveBlocks.CAVE_PORTALS.get())
		{
			if (!portal.getTriggerItems().contains(stack))
			{
				continue;
			}

			if (!portal.getFrameBlocks().contains(state))
			{
				continue;
			}

			PlayerEntity player = event.getPlayer();
			Direction face = ObjectUtils.defaultIfNull(event.getFace(), Direction.UP);
			Hand hand = event.getHand();
			ItemStack prevItem = stack.copy();
			ItemUseContext context = new ItemUseContext(player, hand, BlockRayTraceResult.createMiss(new Vec3d(pos.offset(face)), face, pos));
			ActionResultType result = portal.asItem().onItemUse(context);

			if (result == ActionResultType.SUCCESS)
			{
				if (player.isCreative())
				{
					player.setHeldItem(hand, prevItem);
				}

				event.setCancellationResult(result);
				event.setCanceled(true);

				break;
			}
		}
	}

	@SubscribeEvent
	public static void onBreakSpeed(final BreakSpeed event)
	{
		PlayerEntity player = event.getPlayer();
		ItemStack stack = player.getHeldItemMainhand();

		if (stack.isEmpty())
		{
			return;
		}

		if (player.isWet() && stack.getItem() instanceof ToolItem)
		{
			ToolItem tool = (ToolItem)stack.getItem();

			if (tool.getTier() == CaveItemTier.AQUAMARINE)
			{
				event.setNewSpeed(event.getOriginalSpeed() * 1.1F);
			}
		}
	}
}