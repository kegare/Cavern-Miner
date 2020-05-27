package cavern.miner.block;

import java.util.Random;

import cavern.miner.core.CavernMod;
import cavern.miner.plugin.HaCPlugin;
import defeatedcrow.hac.api.climate.DCAirflow;
import defeatedcrow.hac.api.climate.IAirflowTile;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional.Interface;

@Interface(iface = "defeatedcrow.hac.api.climate.IAirflowTile", modid = HaCPlugin.LIB_MODID, striprefs = true)
public class BlockPervertedLeaves extends BlockOldLeaf implements IAirflowTile
{
	public BlockPervertedLeaves()
	{
		super();
		this.setUnlocalizedName("pervertedLeaves");
		this.setHardness(0.05F);
		this.setCreativeTab(CavernMod.TAB_CAVERN);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return Item.getItemFromBlock(CaveBlocks.PERVERTED_SAPLING);
	}

	@Override
	protected void dropApple(World worldIn, BlockPos pos, IBlockState state, int chance) {}

	@Override
	public DCAirflow getAirflow(World world, BlockPos target, BlockPos pos)
	{
		return DCAirflow.TIGHT;
	}
}