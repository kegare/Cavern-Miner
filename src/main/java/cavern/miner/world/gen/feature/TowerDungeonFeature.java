package cavern.miner.world.gen.feature;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import cavern.miner.CavernMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.common.DungeonHooks;

public class TowerDungeonFeature extends Feature<NoFeatureConfig>
{
	private int maxFloor;
	private int roomSize;
	private int roomHeight;

	public TowerDungeonFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> factory)
	{
		super(factory);
	}

	@Override
	public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config)
	{
		System.out.println(pos.toString());

		setDungeonSize(world, rand, pos);

		while (pos.getY() > 1 && world.isAirBlock(pos))
		{
			pos = pos.down();
		}

		setAirs(world, rand, pos);
		setWalls(world, rand, pos);
		setFloors(world, rand, pos);
		setPillars(world, rand, pos);
		setCeiling(world, rand, pos);
		setLadders(world, rand, pos);
		setChests(world, rand, pos);
		setSpawners(world, rand, pos);

		return true;
	}

	protected void setDungeonSize(IWorld world, Random rand, BlockPos pos)
	{
		maxFloor = rand.nextInt(4) + 5;
		roomSize = rand.nextInt(4) + 4;
		roomHeight = rand.nextInt(4) + 5;
	}

	protected void setAirs(IWorld world, Random rand, BlockPos pos)
	{
		int y = roomHeight * maxFloor;
		int ceilSize = roomSize - 1;
		BlockPos from = pos.add(ceilSize, 0, ceilSize);
		BlockPos to = pos.add(-ceilSize, y, -ceilSize);

		BlockPos.getAllInBoxMutable(from, to).forEach(blockPos -> world.setBlockState(blockPos, Blocks.CAVE_AIR.getDefaultState(), 2));
	}

	protected void setWalls(IWorld world, Random rand, BlockPos pos)
	{
		for (Direction facing : Direction.Plane.HORIZONTAL)
		{
			BlockPos center = pos.offset(facing, roomSize);
			BlockPos from = center.offset(facing.rotateY(), roomSize);
			BlockPos to = center.offset(facing.rotateYCCW(), roomSize).up(roomHeight * maxFloor);

			BlockPos.getAllInBoxMutable(from, to).forEach(blockPos -> world.setBlockState(blockPos, getWallBlock(world, rand, blockPos), 2));
		}
	}

	protected BlockState getWallBlock(IWorld world, Random rand, BlockPos pos)
	{
		return rand.nextDouble() < 0.7D ? Blocks.MOSSY_STONE_BRICKS.getDefaultState() : Blocks.STONE_BRICKS.getDefaultState();
	}

	protected void setFloors(IWorld world, Random rand, BlockPos pos)
	{
		int floorSize = roomSize - 2;

		for (int i = 0; i < maxFloor; ++i)
		{
			int y = roomHeight * i;
			BlockPos from = pos.add(floorSize, y, floorSize);
			BlockPos to = pos.add(-floorSize, y, -floorSize);

			for (BlockPos blockPos : BlockPos.getAllInBoxMutable(from, to))
			{
				world.setBlockState(blockPos, getFloorBlock(world, rand, i + 1, blockPos), 2);
			}
		}

		int ceilSize = roomSize - 1;

		for (Direction facing : Direction.Plane.HORIZONTAL)
		{
			BlockPos center = pos.offset(facing, ceilSize);
			BlockPos from = center.offset(facing.rotateY(), ceilSize);
			BlockPos to = center.offset(facing.rotateYCCW(), ceilSize);

			BlockPos.getAllInBoxMutable(from, to).forEach(blockPos -> world.setBlockState(blockPos, getFirstFloorLiquid(world, rand, blockPos), 2));

			world.setBlockState(center, getFootholdBlock(world, rand, center), 2);
		}
	}

	protected BlockState getFloorBlock(IWorld world, Random rand, int floor, BlockPos pos)
	{
		if (floor <= 1)
		{
			return getWallBlock(world, rand, pos);
		}

		return rand.nextDouble() < 0.7D ? Blocks.CRACKED_STONE_BRICKS.getDefaultState() : Blocks.STONE_BRICKS.getDefaultState();
	}

	protected BlockState getFirstFloorLiquid(IWorld world, Random rand, BlockPos pos)
	{
		return Blocks.LAVA.getDefaultState();
	}

	protected BlockState getFootholdBlock(IWorld world, Random rand, BlockPos pos)
	{
		return Blocks.CHISELED_STONE_BRICKS.getDefaultState();
	}

	protected void setPillars(IWorld world, Random rand, BlockPos pos)
	{
		int floorSize = roomSize - 2;

		for (Direction facing : Direction.Plane.HORIZONTAL)
		{
			BlockPos center = pos.offset(facing, floorSize);
			BlockPos from = center.offset(facing.rotateY(), floorSize);
			BlockPos to = from.up(roomHeight * maxFloor - 1);

			BlockPos.getAllInBoxMutable(from, to).forEach(blockPos -> world.setBlockState(blockPos, getPillarBlock(world, rand, blockPos), 2));
		}
	}

	protected BlockState getPillarBlock(IWorld world, Random rand, BlockPos pos)
	{
		return Blocks.CHISELED_STONE_BRICKS.getDefaultState();
	}

	protected void setCeiling(IWorld world, Random rand, BlockPos pos)
	{
		int y = roomHeight * maxFloor;
		int ceilSize = roomSize - 1;
		BlockPos from = pos.add(ceilSize, y, ceilSize);
		BlockPos to = pos.add(-ceilSize, y, -ceilSize);

		BlockPos.getAllInBoxMutable(from, to).forEach(blockPos -> world.setBlockState(blockPos, getCeilingBlock(world, rand, blockPos), 2));
	}

	protected BlockState getCeilingBlock(IWorld world, Random rand, BlockPos pos)
	{
		return Blocks.CHISELED_STONE_BRICKS.getDefaultState();
	}

	protected void setLadders(IWorld world, Random rand, BlockPos pos)
	{
		int ceilSize = roomSize - 1;
		int ladderHeight = roomHeight * (maxFloor - 1);
		int wallHeight = roomHeight * maxFloor - 1;

		for (Direction facing : Direction.Plane.HORIZONTAL)
		{
			BlockPos center = pos.offset(facing, ceilSize);
			BlockPos from = center.up();
			BlockPos to = center.up(ladderHeight);

			BlockPos.getAllInBoxMutable(from, to).forEach(blockPos -> world.setBlockState(blockPos, Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, facing.getOpposite()), 2));

			from = center.offset(facing.rotateY());
			to = from.up(wallHeight);

			BlockPos.getAllInBoxMutable(from, to).forEach(blockPos -> world.setBlockState(blockPos, getLadderCoverBlock(world, rand, blockPos), 2));

			from = center.offset(facing.rotateYCCW());
			to = from.up(wallHeight);

			BlockPos.getAllInBoxMutable(from, to).forEach(blockPos -> world.setBlockState(blockPos, getLadderCoverBlock(world, rand, blockPos), 2));
		}
	}

	protected BlockState getLadderCoverBlock(IWorld world, Random rand, BlockPos pos)
	{
		return Blocks.CRACKED_STONE_BRICKS.getDefaultState();
	}

	protected void setChests(IWorld world, Random rand, BlockPos pos)
	{
		int floorSize = roomSize - 2;

		for (Direction facing : Direction.Plane.HORIZONTAL)
		{
			BlockPos center = pos.offset(facing, floorSize);
			BlockPos chestPos = center.offset(facing.rotateYCCW(), floorSize - 2);

			for (int i = 1; i < maxFloor; ++i)
			{
				if (rand.nextDouble() > 0.7D)
				{
					continue;
				}

				int y = roomHeight * i + 1;
				BlockPos blockPos = chestPos.up(y);

				world.setBlockState(blockPos, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, facing.getOpposite()), 2);

				LockableLootTileEntity.setLootTable(world, rand, blockPos, LootTables.CHESTS_SIMPLE_DUNGEON);
			}
		}
	}

	protected void setSpawners(IWorld world, Random rand, BlockPos pos)
	{
		for (int i = 0; i < maxFloor; ++i)
		{
			int y = roomHeight * i + 1;
			BlockPos blockPos = pos.up(y);

			world.setBlockState(blockPos, Blocks.SPAWNER.getDefaultState(), 2);

			TileEntity tile = world.getTileEntity(blockPos);

			if (tile != null && tile instanceof MobSpawnerTileEntity)
			{
				((MobSpawnerTileEntity)tile).getSpawnerBaseLogic().setEntityType(getRandomDungeonMob(rand, i + 1));
			}
			else
			{
				CavernMod.LOG.error("Failed to fetch mob spawner entity at ({}, {}, {})", pos.getX(), pos.getY(), pos.getZ());
			}
		}
	}

	private EntityType<?> getRandomDungeonMob(Random rand, int floor)
	{
		return DungeonHooks.getRandomDungeonMob(rand);
	}
}