package cavern.miner.world;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.config.GeneralConfig;
import cavern.miner.handler.CavebornEventHandler;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveNetworkConstants;
import cavern.miner.network.LoadingScreenMessage;
import cavern.miner.storage.CavePortalList;
import cavern.miner.storage.TeleporterCache;
import cavern.miner.util.BlockPosHelper;
import cavern.miner.world.dimension.CavernDimension;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.network.PacketDistributor;

public class CavernTeleporter implements ITeleporter
{
	private final CavernPortalBlock portalBlock;
	private final BlockState portalFrameBlock;

	private BlockPos destPos;
	private Vec3d portalOffset;
	private Direction teleportDirection;

	public CavernTeleporter(CavernPortalBlock portal, BlockState frame)
	{
		this.portalBlock = portal;
		this.portalFrameBlock = frame;
	}

	public CavernTeleporter setDestPos(BlockPos pos)
	{
		destPos = pos;

		return this;
	}

	public CavernTeleporter setPortalInfo(Vec3d offset, Direction direction)
	{
		portalOffset = offset;
		teleportDirection = direction;

		return this;
	}

	@Override
	public Entity placeEntity(final Entity entity, final ServerWorld currentWorld, final ServerWorld destWorld, final float yaw, final Function<Boolean, Entity> repositionEntity)
	{
		final Entity transported = repositionEntity.apply(false);

		if (transported == null)
		{
			return entity;
		}

		final int radius = GeneralConfig.INSTANCE.findRadius.get();

		if (destPos != null && !destPos.equals(transported.getPosition()))
		{
			transported.moveToBlockPosAndAngles(destPos, yaw, transported.rotationPitch);
		}

		final BlockPos originPos = destPos == null ? transported.getPosition() : destPos;

		boolean placed = false;

		if (GeneralConfig.INSTANCE.posCache.get())
		{
			placed = transported.getCapability(CaveCapabilities.TELEPORTER_CACHE).map(o -> placeInCachedPortal(destWorld, transported, yaw, radius, o)).orElse(false);
		}

		if (!placed)
		{
			placed = destWorld.getCapability(CaveCapabilities.CAVE_PORTAL_LIST).map(o -> placeInStoredPortal(destWorld, transported, yaw, radius, originPos, o)).orElse(false);
		}

		boolean toCave = destWorld.getDimension() instanceof CavernDimension;
		boolean isPlayer = transported instanceof ServerPlayerEntity;
		boolean loading = false;

		if (!placed)
		{
			if (toCave && isPlayer)
			{
				CaveNetworkConstants.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)transported), LoadingScreenMessage.Stage.LOAD.create());

				loading = true;
			}

			placed = placeInPortal(destWorld, transported, yaw, radius, originPos);

			if (!placed)
			{
				BlockPos pos = makePortal(destWorld, originPos, radius);

				if (pos != null)
				{
					placed = placeInPortal(destWorld, transported, yaw, radius, pos);
				}
			}
		}

		if (!placed)
		{
			placed = CavebornEventHandler.placeEntity(destWorld, originPos, transported);
		}

		if (loading || toCave && isPlayer && destWorld.getServer().isSinglePlayer())
		{
			CaveNetworkConstants.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)transported), LoadingScreenMessage.Stage.DONE.create());
		}

		return transported;
	}

	public boolean placeInCachedPortal(final ServerWorld world, final Entity entity, final float yaw, final int radius, final TeleporterCache cache)
	{
		ResourceLocation key = portalBlock.getRegistryName();
		DimensionType dim = world.getDimension().getType();
		BlockPos pos = cache.getLastPos(key, dim).orElse(null);

		if (pos == null)
		{
			return false;
		}

		if (placeInPortal(world, entity, yaw, radius, pos))
		{
			return true;
		}

		cache.setLastPos(key, dim, null);

		return false;
	}

	public boolean placeInStoredPortal(final ServerWorld world, final Entity entity, final float yaw, final int radius, final BlockPos checkPos, final CavePortalList list)
	{
		List<BlockPos> positions = list.getPortalPositions(portalBlock).stream()
			.filter(o -> new BlockPos(o.getX(), 0, o.getZ()).withinDistance(new BlockPos(checkPos.getX(), 0, checkPos.getZ()), radius))
			.sorted(Comparator.comparingDouble(o -> o.distanceSq(checkPos))).collect(Collectors.toList());

		for (BlockPos pos : positions)
		{
			if (placeInPortal(world, entity, yaw, 8, pos))
			{
				return true;
			}

			list.removePortal(portalBlock, pos);
		}

		return false;
	}

	public boolean placeInPortal(final ServerWorld world, final Entity entity, final float yaw, final int radius, final BlockPos checkPos)
	{
		final BlockPos pos;

		if (world.getBlockState(checkPos).getBlock() == portalBlock)
		{
			pos = checkPos;
		}
		else
		{
			pos = BlockPosHelper.findPos(world, checkPos, radius, o -> world.getBlockState(o).getBlock() == portalBlock);

			if (pos == null)
			{
				return false;
			}
		}

		world.getCapability(CaveCapabilities.CAVE_PORTAL_LIST).ifPresent(o -> o.addPortal(portalBlock, pos));

		if (portalOffset == null)
		{
			portalOffset = Vec3d.ZERO;
		}

		if (teleportDirection == null)
		{
			teleportDirection = Direction.NORTH;
		}

		BlockPattern.PortalInfo portalInfo = portalBlock.createPatternHelper(world, pos).getPortalInfo(teleportDirection, pos, portalOffset.y, entity.getMotion(), portalOffset.x);

		entity.setMotion(portalInfo.motion);
		entity.rotationYaw = yaw + portalInfo.rotation;

		double posX = portalInfo.pos.x;
		double posY = portalInfo.pos.y;
		double posZ = portalInfo.pos.z;

		if (entity instanceof ServerPlayerEntity)
		{
			((ServerPlayerEntity)entity).connection.setPlayerLocation(posX, posY, posZ, entity.rotationYaw, entity.rotationPitch);
		}
		else
		{
			entity.setLocationAndAngles(posX, posY, posZ, entity.rotationYaw, entity.rotationPitch);
		}

		return true;
	}

	private void loadSurroundingArea(final IWorld world, final BlockPos pos, final int radius)
	{
		ChunkPos.getAllInBox(new ChunkPos(pos), Math.max(radius, 1)).filter(o -> !world.getWorldBorder().contains(o)).forEach(o -> world.getChunk(o.x, o.z));
	}

	@Nullable
	public BlockPos makePortal(final ServerWorld world, final BlockPos originPos, final int radius)
	{
		loadSurroundingArea(world, originPos, radius >> 4);

		final int min = 10;
		final int max = world.getActualHeight() - 10;
		final BlockPos.Mutable pos = new BlockPos.Mutable();

		int i = 0;
		int j = world.rand.nextInt(4);
		BlockPos resultPos;

		resultPos = BlockPosHelper.findPos(world, originPos, radius, min, max, o ->
		{
			if (!world.isAirBlock(pos.setPos(o)) || !world.getBlockState(pos.move(Direction.DOWN)).isSolid())
			{
				return false;
			}

			int i1 = j % 2;
			int i2 = 1 - i1;

			if (j % 4 >= 2)
			{
				i1 = -i1;
				i2 = -i2;
			}

			for (int width1 = 0; width1 < 3; ++width1)
			{
				for (int width2 = 0; width2 < 4; ++width2)
				{
					for (int height = -1; height < 4; ++height)
					{
						pos.setPos(o.getX() + (width2 - 1) * i1 + width1 * i2, o.getY() + height, o.getZ() + (width2 - 1) * i2 - width1 * i1);

						if (height < 0 && !world.getBlockState(pos).isSolid() || height >= 0 && !world.isAirBlock(pos))
						{
							return false;
						}
					}
				}
			}

			return true;
		});

		if (resultPos != null)
		{
			i = j % 4;
		}
		else
		{
			resultPos = BlockPosHelper.findPos(world, originPos, radius, min, max, o ->
			{
				if (!world.isAirBlock(pos.setPos(o)) || !world.getBlockState(pos.move(Direction.DOWN)).isSolid())
				{
					return false;
				}

				int i1 = j % 2;
				int i2 = 1 - i1;

				for (int width = 0; width < 4; ++width)
				{
					for (int height = -1; height < 4; ++height)
					{
						pos.setPos(o.getX() + (width - 1) * i1, o.getY() + height, o.getZ() + (width - 1) * i2);

						if (height < 0 && !world.getBlockState(pos).isSolid() || height >= 0 && !world.isAirBlock(pos))
						{
							return false;
						}
					}
				}

				return true;
			});

			if (resultPos != null)
			{
				i = j % 2;
			}
		}

		int i1 = i % 2;
		int i2 = 1 - i1;

		if (i % 4 >= 2)
		{
			i1 = -i1;
			i2 = -i2;
		}

		if (resultPos == null)
		{
			resultPos = originPos;

			for (int width1 = -1; width1 <= 1; ++width1)
			{
				for (int width2 = 1; width2 < 3; ++width2)
				{
					int x = resultPos.getX() + (width2 - 1) * i1 + width1 * i2;
					int z = resultPos.getZ() + (width2 - 1) * i2 - width1 * i1;

					for (int height = -1; height < 3; ++height)
					{
						world.setBlockState(pos.setPos(x, resultPos.getY() + height, z), height < 0 ? portalFrameBlock : Blocks.AIR.getDefaultState());
					}
				}
			}
		}

		for (int width = -1; width < 3; ++width)
		{
			int x = resultPos.getX() + width * i1;
			int z = resultPos.getZ() + width * i2;

			for (int height = -1; height < 4; ++height)
			{
				if (width == -1 || width == 2 || height == -1 || height == 3)
				{
					world.setBlockState(pos.setPos(x, resultPos.getY() + height, z), portalFrameBlock);
				}
			}
		}

		BlockState state = portalBlock.getDefaultState().with(CavernPortalBlock.AXIS, i1 != 0 ? Direction.Axis.X : Direction.Axis.Z);

		for (int width = 0; width < 2; ++width)
		{
			int x = resultPos.getX() + width * i1;
			int z = resultPos.getZ() + width * i2;

			for (int height = 0; height < 3; ++height)
			{
				world.setBlockState(pos.setPos(x, resultPos.getY() + height, z), state, 18);
			}
		}

		return resultPos;
	}
}