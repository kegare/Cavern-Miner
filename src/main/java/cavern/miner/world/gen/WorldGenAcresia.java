package cavern.miner.world.gen;

import java.util.Random;

import cavern.miner.block.CaveBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenAcresia extends WorldGenerator
{
	@Override
	public boolean generate(World world, Random rand, BlockPos position)
	{
		MutableBlockPos pos = new MutableBlockPos();

		for (int i = 0; i < 64; ++i)
		{
			pos.setPos(position.getX() + rand.nextInt(8) - rand.nextInt(8), position.getY() + rand.nextInt(4) - rand.nextInt(4), position.getZ() + rand.nextInt(8) - rand.nextInt(8));

			if (world.isAirBlock(pos) && pos.getY() < world.getActualHeight() - 1)
			{
				int age;

				if (pos.getY() >= world.getActualHeight() / 2)
				{
					age = 3 + rand.nextInt(2);
				}
				else
				{
					age = 2 + rand.nextInt(3);
				}

				IBlockState state = CaveBlocks.ACRESIA.withAge(age);

				if (CaveBlocks.ACRESIA.canBlockStay(world, pos, state))
				{
					Material material = world.getBlockState(pos.down()).getMaterial();

					if (material == Material.GRASS || material == Material.GROUND)
					{
						world.setBlockState(pos, state, 2);
					}
				}
			}
		}

		return true;
	}
}