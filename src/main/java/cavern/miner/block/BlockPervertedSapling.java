package cavern.miner.block;

import java.util.Random;

import cavern.miner.core.CavernMod;
import cavern.miner.world.gen.WorldGenPervertedBirchTree;
import cavern.miner.world.gen.WorldGenPervertedSpruceTree;
import cavern.miner.world.gen.WorldGenPervertedTrees;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.TerrainGen;

public class BlockPervertedSapling extends BlockSapling
{
	public BlockPervertedSapling()
	{
		super();
		this.setUnlocalizedName("pervertedSapling");
		this.setHardness(0.0F);
		this.setSoundType(SoundType.PLANT);
		this.setCreativeTab(CavernMod.TAB_CAVERN);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		if (!world.isRemote)
		{
			checkAndDropBlock(world, pos, state);

			grow(world, pos, state, rand);
		}
	}

	@Override
	public void generateTree(World world, BlockPos pos, IBlockState state, Random rand)
	{
		if (!TerrainGen.saplingGrowTree(world, rand, pos))
		{
			return;
		}

		WorldGenerator treeGen;

		switch (state.getValue(TYPE))
		{
			case SPRUCE:
				treeGen = new WorldGenPervertedSpruceTree(true);
				break;
			case BIRCH:
				treeGen = new WorldGenPervertedBirchTree(true, false);
				break;
			case JUNGLE:
				treeGen = new WorldGenPervertedTrees(true, 3 + rand.nextInt(6), BlockPlanks.EnumType.JUNGLE, false);
				break;
			default:
				treeGen = new WorldGenPervertedTrees(true, 3, BlockPlanks.EnumType.OAK, false);
				break;
		}

		world.setBlockState(pos, Blocks.AIR.getDefaultState(), 4);

		if (!treeGen.generate(world, rand, pos))
		{
			world.setBlockState(pos, state, 4);
		}
	}

	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state)
	{
		return true;
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		for (EnumType type : BlockOldLog.VARIANT.getAllowedValues())
		{
			list.add(new ItemStack(this, 1, type.getMetadata()));
		}
	}
}