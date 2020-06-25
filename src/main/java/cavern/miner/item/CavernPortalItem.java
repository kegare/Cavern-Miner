package cavern.miner.item;

import cavern.miner.block.CavernPortalBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CavernPortalItem extends BlockItem
{
	private final CavernPortalBlock portalBlock;

	public CavernPortalItem(CavernPortalBlock block, Properties builder)
	{
		super(block, builder);
		this.portalBlock = block;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		if (!tryCreatePortal(new BlockItemUseContext(context)).isSuccess())
		{
			return ActionResultType.FAIL;
		}

		if (context.getPlayer() == null || !context.getPlayer().isCreative())
		{
			context.getItem().shrink(1);
		}

		return ActionResultType.SUCCESS;
	}

	public ActionResultType tryCreatePortal(BlockItemUseContext context)
	{
		World world = context.getWorld();
		BlockPos pos = context.getPos();

		if (!portalBlock.trySpawnPortal(world, pos))
		{
			return ActionResultType.FAIL;
		}

		PlayerEntity player = context.getPlayer();
		BlockState state = world.getBlockState(pos);
		SoundType soundType = state.getSoundType(world, pos, player);

		world.playSound(null, pos, getPlaceSound(state, world, pos, player), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

		return ActionResultType.SUCCESS;
	}
}