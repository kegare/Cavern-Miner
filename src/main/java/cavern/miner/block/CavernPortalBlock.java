package cavern.miner.block;

import javax.annotation.Nullable;

import com.google.common.cache.LoadingCache;

import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveDimensions;
import cavern.miner.storage.TeleporterCache;
import cavern.miner.world.CavernTeleporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class CavernPortalBlock extends Block
{
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

	protected static final VoxelShape X_AABB = Block.makeCuboidShape(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
	protected static final VoxelShape Z_AABB = Block.makeCuboidShape(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

	public CavernPortalBlock(Block.Properties properties)
	{
		super(properties);
		this.setDefaultState(stateContainer.getBaseState().with(AXIS, Direction.Axis.X));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context)
	{
		switch (state.get(AXIS))
		{
			case Z:
				return Z_AABB;
			case X:
			default:
				return X_AABB;
		}
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		switch (rot)
		{
			case COUNTERCLOCKWISE_90:
			case CLOCKWISE_90:
				switch(state.get(AXIS))
				{
					case Z:
						return state.with(AXIS, Direction.Axis.X);
					case X:
						return state.with(AXIS, Direction.Axis.Z);
					default:
						return state;
				}
			default:
				return state;
		}
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(AXIS);
	}

	public boolean trySpawnPortal(IWorld world, BlockPos pos)
	{
		Size size = isPortal(world, pos);

		if (size != null)
		{
			size.placePortalBlocks();

			return true;
		}

		return false;
	}

	@Nullable
	public Size isPortal(IWorld world, BlockPos pos)
	{
		Size sizeX = new Size(this, world, pos, Direction.Axis.X);

		if (sizeX.isValid() && sizeX.portalBlockCount == 0)
		{
			return sizeX;
		}
		else
		{
			Size sizeZ = new Size(this, world, pos, Direction.Axis.Z);

			return sizeZ.isValid() && sizeZ.portalBlockCount == 0 ? sizeZ : null;
		}
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
	{
		Direction.Axis axis = state.get(AXIS);

		if (axis == Direction.Axis.X)
		{
			Size size = new Size(this, world, pos, Direction.Axis.X);

			if (!size.isValid() || size.portalBlockCount < size.width * size.height)
			{
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
		}
		else if (axis == Direction.Axis.Z)
		{
			Size size = new Size(this, world, pos, Direction.Axis.Z);

			if (!size.isValid() || size.portalBlockCount < size.width * size.height)
			{
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
		}
	}

	public DimensionType getDimension()
	{
		return CaveDimensions.CAVERN_TYPE;
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity)
	{
		if (world.isRemote)
		{
			return;
		}

		if (!entity.isAlive() || entity.isCrouching() || entity.isPassenger() || entity.isBeingRidden() || !entity.isNonBoss() || entity instanceof IProjectile)
		{
			return;
		}

		int cd = Math.max(entity.getPortalCooldown(), 50);

		if (entity.timeUntilPortal > 0)
		{
			entity.timeUntilPortal = cd;

			return;
		}

		DimensionType dimOld = world.getDimension().getType();
		DimensionType dimNew = dimOld != getDimension() ? getDimension() : DimensionType.OVERWORLD;

		entity.timeUntilPortal = cd;

		TeleporterCache cache = entity.getCapability(CaveCapabilities.TELEPORTER_CACHE).orElse(null);

		if (cache != null)
		{
			ResourceLocation key = getRegistryName();

			if (dimOld != getDimension())
			{
				dimNew = cache.getLastDim(key, dimNew);
			}

			cache.setLastDim(key, dimOld);
			cache.setLastPos(key, dimOld, entity.getPosition());

			BlockPattern.PatternHelper pattern = createPatternHelper(this, world, pos);
			double d0 = pattern.getForwards().getAxis() == Direction.Axis.X ? (double)pattern.getFrontTopLeft().getZ() : (double)pattern.getFrontTopLeft().getX();
			double d1 = pattern.getForwards().getAxis() == Direction.Axis.X ? entity.getPosZ() : entity.getPosX();
			d1 = Math.abs(MathHelper.pct(d1 - (pattern.getForwards().rotateY().getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 1 : 0), d0, d0 - pattern.getWidth()));
			double d2 = MathHelper.pct(entity.getPosY() - 1.0D, pattern.getFrontTopLeft().getY(), pattern.getFrontTopLeft().getY() - pattern.getHeight());

			cache.setLastPortalVec(new Vec3d(d1, d2, 0.0D));
			cache.setTeleportDirection(pattern.getForwards());
		}

		world.getCapability(CaveCapabilities.CAVE_PORTAL_LIST).ifPresent(o -> o.addPortal(this, pos));

		entity.changeDimension(dimNew, new CavernTeleporter(this));
	}

	public static BlockPattern.PatternHelper createPatternHelper(Block portal, IWorld world, BlockPos pos)
	{
		Direction.Axis axis = Direction.Axis.Z;
		Size size = new Size(portal, world, pos, Direction.Axis.X);
		LoadingCache<BlockPos, CachedBlockInfo> cache = BlockPattern.createLoadingCache(world, true);

		if (!size.isValid())
		{
			axis = Direction.Axis.X;
			size = new Size(portal, world, pos, Direction.Axis.Z);
		}

		if (!size.isValid())
		{
			return new BlockPattern.PatternHelper(pos, Direction.NORTH, Direction.UP, cache, 1, 1, 1);
		}
		else
		{
			int[] aint = new int[Direction.AxisDirection.values().length];
			Direction direction = size.rightDir.rotateYCCW();
			BlockPos blockpos = size.bottomLeft.up(size.getHeight() - 1);

			for (Direction.AxisDirection d : Direction.AxisDirection.values())
			{
				BlockPattern.PatternHelper helper = new BlockPattern.PatternHelper(direction.getAxisDirection() == d ? blockpos : blockpos.offset(size.rightDir, size.getWidth() - 1), Direction.getFacingFromAxis(d, axis), Direction.UP, cache, size.getWidth(), size.getHeight(), 1);

				for (int i = 0; i < size.getWidth(); ++i)
				{
					for (int j = 0; j < size.getHeight(); ++j)
					{
						CachedBlockInfo cachedInfo = helper.translateOffset(i, j, 1);

						if (!cachedInfo.getWorld().isAirBlock(cachedInfo.getPos()))
						{
							++aint[d.ordinal()];
						}
					}
				}
			}

			Direction.AxisDirection ax = Direction.AxisDirection.POSITIVE;

			for (Direction.AxisDirection d : Direction.AxisDirection.values())
			{
				if (aint[d.ordinal()] < aint[ax.ordinal()])
				{
					ax = d;
				}
			}

			return new BlockPattern.PatternHelper(direction.getAxisDirection() == ax ? blockpos : blockpos.offset(size.rightDir, size.getWidth() - 1), Direction.getFacingFromAxis(ax, axis), Direction.UP, cache, size.getWidth(), size.getHeight(), 1);
		}
	}

	public static class Size
	{
		private final Block portalBlock;
		private final IWorld world;
		private final Direction.Axis axis;
		private final Direction rightDir;
		private final Direction leftDir;

		private int portalBlockCount;
		private BlockPos bottomLeft;
		private int height;
		private int width;
		private Block portalFrame;

		public Size(Block portal, IWorld world, BlockPos pos, Direction.Axis axis)
		{
			this.portalBlock = portal;
			this.world = world;
			this.axis = axis;

			if (axis == Direction.Axis.X)
			{
				this.leftDir = Direction.EAST;
				this.rightDir = Direction.WEST;
			}
			else
			{
				this.leftDir = Direction.NORTH;
				this.rightDir = Direction.SOUTH;
			}

			for (BlockPos blockpos = pos; pos.getY() > blockpos.getY() - 21 && pos.getY() > 0 && isEmptyBlock(pos.down()); pos = pos.down())
			{
				;
			}

			int i = getDistanceUntilEdge(pos, leftDir) - 1;

			if (i >= 0)
			{
				this.bottomLeft = pos.offset(leftDir, i);
				this.width = getDistanceUntilEdge(bottomLeft, rightDir);

				if (width < 2 || width > 21)
				{
					this.bottomLeft = null;
					this.width = 0;
				}
			}

			if (bottomLeft != null)
			{
				this.height = calculatePortalHeight();
			}
		}

		protected int getDistanceUntilEdge(BlockPos pos, Direction face)
		{
			int i;

			for (i = 0; i < 22; ++i)
			{
				BlockPos pos1 = pos.offset(face, i);

				if (!isEmptyBlock(pos1) || !isFrameBlock(pos1.down()))
				{
					break;
				}
			}

			return isFrameBlock(pos.offset(face, i)) ? i : 0;
		}

		public int getHeight()
		{
			return height;
		}

		public int getWidth()
		{
			return width;
		}

		protected int calculatePortalHeight()
		{
			outside: for (height = 0; height < 21; ++height)
			{
				for (int i = 0; i < width; ++i)
				{
					BlockPos pos = bottomLeft.offset(rightDir, i).up(height);

					if (!isEmptyBlock(pos))
					{
						break outside;
					}

					if (world.getBlockState(pos).getBlock() == portalBlock)
					{
						++portalBlockCount;
					}

					if (i == 0)
					{
						if (!isFrameBlock(pos.offset(leftDir)))
						{
							break outside;
						}
					}
					else if (i == width - 1)
					{
						if (!isFrameBlock(pos.offset(rightDir)))
						{
							break outside;
						}
					}
				}
			}

			for (int i = 0; i < width; ++i)
			{
				if (!isFrameBlock(bottomLeft.offset(rightDir, i).up(height)))
				{
					height = 0;

					break;
				}
			}

			if (height <= 21 && height >= 3)
			{
				return height;
			}
			else
			{
				bottomLeft = null;
				width = 0;
				height = 0;

				return 0;
			}
		}

		protected boolean isEmptyBlock(BlockPos pos)
		{
			BlockState state = world.getBlockState(pos);

			return state.isAir(world, pos) || state.getBlock() == portalBlock;
		}

		protected boolean isFrameBlock(BlockPos pos)
		{
			BlockState state = world.getBlockState(pos);

			if (portalFrame == null)
			{
				if (state.getBlock() == Blocks.MOSSY_COBBLESTONE)
				{
					portalFrame = state.getBlock();
				}
				else if (state.getBlock() == Blocks.MOSSY_STONE_BRICKS)
				{
					portalFrame = state.getBlock();
				}
			}

			return portalFrame != null && portalFrame == state.getBlock();
		}

		public boolean isValid()
		{
			return bottomLeft != null && width >= 2 && width <= 21 && height >= 3 && height <= 21;
		}

		public void placePortalBlocks()
		{
			for (int i = 0; i < width; ++i)
			{
				BlockPos pos = bottomLeft.offset(rightDir, i);

				for (int j = 0; j < height; ++j)
				{
					world.setBlockState(pos.up(j), portalBlock.getDefaultState().with(AXIS, axis), 18);
				}
			}
		}
	}
}