package cavern.miner.block;

import javax.annotation.Nullable;

import com.google.common.cache.LoadingCache;

import cavern.miner.config.GeneralConfig;
import cavern.miner.config.dimension.CavernConfig;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveDimensions;
import cavern.miner.storage.Miner;
import cavern.miner.storage.TeleporterCache;
import cavern.miner.util.BlockStateHelper;
import cavern.miner.util.BlockStateTagList;
import cavern.miner.util.ItemStackTagList;
import cavern.miner.world.CavernTeleporter;
import cavern.miner.world.dimension.CavernDimension;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
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

	@Nullable
	public DimensionType getDimension()
	{
		return CaveDimensions.CAVERN_TYPE;
	}

	public ItemStackTagList getTriggerItems()
	{
		return CavernConfig.INSTANCE.portal.getTriggerItems();
	}

	public BlockStateTagList getFrameBlocks()
	{
		return CavernConfig.INSTANCE.portal.getFrameBlocks();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context)
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
				switch (state.get(AXIS))
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

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader reader, BlockPos pos, PlayerEntity player)
	{
		return ItemStack.EMPTY;
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
		Size size = new Size(world, pos, Direction.Axis.X);

		if (size.isValid() && size.portalBlockCount == 0)
		{
			return size;
		}

		size = new Size(world, pos, Direction.Axis.Z);

		return size.isValid() && size.portalBlockCount == 0 ? size : null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos)
	{
		Direction.Axis axis = facing.getAxis();
		Direction.Axis stateAxis = state.get(AXIS);
		boolean flag = stateAxis != axis && axis.isHorizontal();

		if (!flag && facingState.getBlock() != this && !new Size(world, currentPos, stateAxis).isAlreadyValid())
		{
			return Blocks.AIR.getDefaultState();
		}

		return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
	{
		if (!world.isRemote && world.getDimension() instanceof CavernDimension)
		{
			player.getCapability(CaveCapabilities.MINER).ifPresent(Miner::displayRecord);
		}

		return ActionResultType.PASS;
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity)
	{
		if (getDimension() == null)
		{
			return;
		}

		if (!entity.isAlive() || entity.isPassenger() || entity.isBeingRidden() || !entity.isNonBoss() || entity instanceof IProjectile)
		{
			return;
		}

		int cooldown = Math.max(entity.getPortalCooldown(), 50);

		if (entity.timeUntilPortal > 0)
		{
			entity.timeUntilPortal = cooldown;

			return;
		}

		entity.timeUntilPortal = cooldown;

		if (world.isRemote)
		{
			return;
		}

		MinecraftServer server = entity.getServer();

		if (server == null)
		{
			return;
		}

		DimensionType currentDim = world.getDimension().getType();
		DimensionType destDim = currentDim != getDimension() ? getDimension() : DimensionType.OVERWORLD;

		TeleporterCache cache = entity.getCapability(CaveCapabilities.TELEPORTER_CACHE).orElse(null);
		BlockPos destPos = null;

		if (cache != null)
		{
			ResourceLocation key = getRegistryName();
			DimensionType prevDim = destDim;

			destDim = cache.getLastDim(key).orElse(destDim);

			if (destDim == currentDim)
			{
				destDim = prevDim;
			}

			destPos = cache.getLastPos(key, destDim).orElse(null);

			cache.setLastDim(key, currentDim);
			cache.setLastPos(key, currentDim, pos);
		}

		if (destPos == null || !GeneralConfig.INSTANCE.posCache.get())
		{
			destPos = entity.getPosition();
		}

		world.getCapability(CaveCapabilities.CAVE_PORTAL_LIST).ifPresent(o -> o.addPortal(this, pos));

		BlockPos.Mutable floorPos = new BlockPos.Mutable(pos);

		while (world.getBlockState(floorPos).getBlock() == this)
		{
			floorPos.move(Direction.DOWN);
		}

		entity.changeDimension(destDim, createTeleporter(world, pos, entity, world.getBlockState(floorPos)).setDestPos(destPos));
	}

	public CavernTeleporter createTeleporter(IWorld world, BlockPos pos, Entity entity, @Nullable BlockState frame)
	{
		if (frame == null || !getFrameBlocks().contains(frame))
		{
			frame = getFrameBlocks().getAll().get(0);
		}

		BlockPattern.PatternHelper pattern = createPatternHelper(world, pos);
		double d = pattern.getForwards().getAxis() == Direction.Axis.X ? (double)pattern.getFrontTopLeft().getZ() : (double)pattern.getFrontTopLeft().getX();
		double horizontalOffset = Math.abs(MathHelper.pct((pattern.getForwards().getAxis() == Direction.Axis.X ? entity.getPosZ() : entity.getPosX()) - (pattern.getForwards().rotateY().getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 1 : 0), d, d - pattern.getWidth()));
		double verticalOffset = MathHelper.pct(entity.getPosY() - 1.0D, pattern.getFrontTopLeft().getY(), pattern.getFrontTopLeft().getY() - pattern.getHeight());

		return new CavernTeleporter(this, frame).setPortalInfo(new Vec3d(horizontalOffset, verticalOffset, 0.0D), pattern.getForwards());
	}

	public BlockPattern.PatternHelper createPatternHelper(IWorld world, BlockPos pos)
	{
		Direction.Axis axis = Direction.Axis.Z;
		Size size = new Size(world, pos, Direction.Axis.X);
		LoadingCache<BlockPos, CachedBlockInfo> cache = BlockPattern.createLoadingCache(world, true);

		if (!size.isValid())
		{
			axis = Direction.Axis.X;
			size = new Size(world, pos, Direction.Axis.Z);
		}

		if (!size.isValid())
		{
			return new BlockPattern.PatternHelper(pos, Direction.NORTH, Direction.UP, cache, 1, 1, 1);
		}
		else
		{
			int[] aint = new int[Direction.AxisDirection.values().length];
			Direction direction = size.rightDir.rotateYCCW();
			BlockPos originPos = size.bottomLeft.up(size.height - 1);
			BlockPos.Mutable blockPos = new BlockPos.Mutable();

			for (Direction.AxisDirection d : Direction.AxisDirection.values())
			{
				blockPos.setPos(originPos);

				if (direction.getAxisDirection() != d)
				{
					blockPos.move(size.rightDir, size.width - 1);
				}

				BlockPattern.PatternHelper helper = new BlockPattern.PatternHelper(blockPos, Direction.getFacingFromAxis(d, axis), Direction.UP, cache, size.width, size.height, 1);

				for (int i = 0; i < size.width; ++i)
				{
					for (int j = 0; j < size.height; ++j)
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

			blockPos.setPos(originPos);

			if (direction.getAxisDirection() != ax)
			{
				blockPos.move(size.rightDir, size.width - 1);
			}

			return new BlockPattern.PatternHelper(blockPos, Direction.getFacingFromAxis(ax, axis), Direction.UP, cache, size.width, size.height, 1);
		}
	}

	public class Size
	{
		private final IWorld world;
		private final Direction.Axis axis;
		private final Direction rightDir;
		private final Direction leftDir;

		private int portalBlockCount;
		private BlockPos bottomLeft;
		private int height;
		private int width;
		private BlockState portalFrame;

		public Size(IWorld world, BlockPos pos, Direction.Axis axis)
		{
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

			BlockPos.Mutable checkPos = new BlockPos.Mutable(pos);

			while (checkPos.getY() > 0 && checkPos.getY() > pos.getY() - 21)
			{
				if (!isEmptyBlock(checkPos.move(Direction.DOWN)))
				{
					break;
				}
			}

			pos = checkPos.up();

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

		protected int getDistanceUntilEdge(BlockPos pos, Direction facing)
		{
			int i;

			BlockPos.Mutable checkPos = new BlockPos.Mutable();

			for (i = 0; i < 22; ++i)
			{
				if (!isEmptyBlock(checkPos.setPos(pos).move(facing, i)) || !isFrameBlock(checkPos.move(Direction.DOWN)))
				{
					break;
				}
			}

			return isFrameBlock(pos.offset(facing, i)) ? i : 0;
		}

		protected int calculatePortalHeight()
		{
			BlockPos.Mutable pos = new BlockPos.Mutable();

			outside: for (height = 0; height < 21; ++height)
			{
				for (int i = 0; i < width; ++i)
				{
					if (!isEmptyBlock(pos.setPos(bottomLeft).move(rightDir, i).move(Direction.UP, height)))
					{
						break outside;
					}

					if (world.getBlockState(pos).getBlock() == CavernPortalBlock.this)
					{
						++portalBlockCount;
					}

					if (i == 0)
					{
						if (!isFrameBlock(pos.move(leftDir)))
						{
							break outside;
						}
					}
					else if (i == width - 1)
					{
						if (!isFrameBlock(pos.move(rightDir)))
						{
							break outside;
						}
					}
				}
			}

			for (int i = 0; i < width; ++i)
			{
				if (!isFrameBlock(pos.setPos(bottomLeft).move(rightDir, i).move(Direction.UP, height)))
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

			return state.isAir(world, pos) || state.getBlock() == CavernPortalBlock.this;
		}

		protected boolean isFrameBlock(BlockPos pos)
		{
			BlockState state = world.getBlockState(pos);

			if (portalFrame == null)
			{
				if (CavernPortalBlock.this.getFrameBlocks().contains(state))
				{
					portalFrame = state;
				}
			}

			return BlockStateHelper.equals(portalFrame, state);
		}

		public boolean isValid()
		{
			return bottomLeft != null && width >= 2 && width <= 21 && height >= 3 && height <= 21;
		}

		public void placePortalBlocks()
		{
			BlockPos.Mutable pos = new BlockPos.Mutable();

			for (int i = 0; i < width; ++i)
			{
				for (int j = 0; j < height; ++j)
				{
					world.setBlockState(pos.setPos(bottomLeft).move(rightDir, i).move(Direction.UP, j), CavernPortalBlock.this.getDefaultState().with(AXIS, axis), 18);
				}
			}
		}

		public boolean isPortalAlreadyExisted()
		{
			return portalBlockCount >= width * height;
		}

		public boolean isAlreadyValid()
		{
			return isValid() && isPortalAlreadyExisted();
		}
	}
}