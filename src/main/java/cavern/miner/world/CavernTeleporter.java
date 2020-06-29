package cavern.miner.world;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.config.GeneralConfig;
import cavern.miner.handler.CavebornEventHandler;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.network.CaveNetworkConstants;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.network.PacketDistributor;

public class CavernTeleporter implements ITeleporter
{
	private final CavernPortalBlock portalBlock;
	private final BlockState portalFrameBlock;

	private Vec3d portalVec;
	private Direction teleportDirection;

	public CavernTeleporter(CavernPortalBlock portal, BlockState frame)
	{
		this.portalBlock = portal;
		this.portalFrameBlock = frame;
	}

	public CavernTeleporter setPortalInfo(Vec3d vec, Direction direction)
	{
		portalVec = vec;
		teleportDirection = direction;

		return this;
	}

	@Override
	public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity)
	{
		final Entity newEntity = repositionEntity.apply(false);

		int radius = GeneralConfig.INSTANCE.findRadius.get();
		boolean placed = false;

		if (GeneralConfig.INSTANCE.posCache.get())
		{
			placed = newEntity.getCapability(CaveCapabilities.TELEPORTER_CACHE).map(o -> placeInCachedPortal(destWorld, newEntity, yaw, radius, o)).orElse(false);
		}

		final BlockPos originPos = newEntity.getPosition();

		if (!placed)
		{
			placed = destWorld.getCapability(CaveCapabilities.CAVE_PORTAL_LIST).map(o -> placeInStoredPortal(destWorld, newEntity, yaw, radius, originPos, o)).orElse(false);
		}

		boolean toCave = destWorld.getDimension() instanceof CavernDimension;
		boolean isPlayer = newEntity instanceof ServerPlayerEntity;
		boolean loading = false;

		if (!placed)
		{
			if (toCave && isPlayer)
			{
				CaveNetworkConstants.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)newEntity), new LoadingScreenMessage(LoadingScreenMessage.Stage.LOAD));

				loading = true;
			}

			placed = placeInPortal(destWorld, newEntity, yaw, radius, originPos);

			if (!placed)
			{
				BlockPos pos = makePortal(destWorld, originPos, radius);

				if (pos != null)
				{
					placed = placeInPortal(destWorld, newEntity, yaw, radius, pos);
				}
			}
		}

		if (!placed)
		{
			placed = CavebornEventHandler.placeEntity(destWorld, originPos, newEntity);
		}

		if (loading || toCave && isPlayer && destWorld.getServer().isSinglePlayer())
		{
			CaveNetworkConstants.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)newEntity), new LoadingScreenMessage(LoadingScreenMessage.Stage.DONE));
		}

		return newEntity;
	}

	public boolean placeInCachedPortal(ServerWorld world, Entity entity, float yaw, int radius, TeleporterCache cache)
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

	public boolean placeInStoredPortal(ServerWorld world, Entity entity, float yaw, int radius, BlockPos checkPos, CavePortalList list)
	{
		List<BlockPos> positions = list.getPortalPositions(portalBlock).stream()
			.filter(o -> new BlockPos(o.getX(), 0, o.getZ()).withinDistance(new BlockPos(checkPos.getX(), 0, checkPos.getZ()), radius))
			.sorted((o1, o2) -> Double.compare(o1.distanceSq(checkPos), o2.distanceSq(checkPos))).collect(Collectors.toList());

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

	public boolean placeInPortal(ServerWorld world, Entity entity, float yaw, int radius, BlockPos checkPos)
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

		if (portalVec == null)
		{
			portalVec = Vec3d.ZERO;
		}

		if (teleportDirection == null)
		{
			teleportDirection = Direction.NORTH;
		}

		BlockPattern.PatternHelper pattern = portalBlock.createPatternHelper(world, pos);
		BlockPattern.PortalInfo portalInfo = pattern.getPortalInfo(teleportDirection, pos, portalVec.y, entity.getMotion(), portalVec.x);

		if (portalInfo == null)
		{
			return false;
		}

		entity.setMotion(portalInfo.motion);
		entity.rotationYaw = yaw + portalInfo.rotation;
		entity.moveForced(portalInfo.pos.x, portalInfo.pos.y, portalInfo.pos.z);

		return true;
	}

	@Nullable
	public BlockPos makePortal(ServerWorld world, BlockPos originPos, int radius)
	{
		int min = 10;
		int max = world.getActualHeight() - 10;
		int i = 0;
		int j = world.rand.nextInt(4);
		BlockPos.Mutable pos = new BlockPos.Mutable();
		BlockPos resultPos = null;

		outside: for (int r = 1; r <= radius; ++r)
		{
			for (int rx = -r; rx <= r; ++rx)
			{
				for (int rz = -r; rz <= r; ++rz)
				{
					if (Math.abs(rx) < r && Math.abs(rz) < r) continue;

					int px = originPos.getX() + rx;
					int py = min;
					int pz = originPos.getZ() + rz;

					finder: while (true)
					{
						for (py = originPos.getY(); py <= max; ++py)
						{
							if (world.isAirBlock(pos.setPos(px, py, pz)) && world.getBlockState(pos.move(Direction.DOWN)).isSolid())
							{
								break finder;
							}
						}

						for (py = originPos.getY(); py >= min; --py)
						{
							if (world.isAirBlock(pos.setPos(px, py, pz)) && world.getBlockState(pos.move(Direction.DOWN)).isSolid())
							{
								break finder;
							}
						}

						py = 0;

						break;
					}

					if (py < min || py > max)
					{
						continue;
					}

					int i1 = j % 2;
					int i2 = 1 - i1;

					if (j % 4 >= 2)
					{
						i1 = -i1;
						i2 = -i2;
					}

					boolean hasSpace = true;

					space: for (int size1 = 0; size1 < 3; ++size1)
					{
						for (int size2 = 0; size2 < 4; ++size2)
						{
							for (int height = -1; height < 4; ++height)
							{
								pos.setPos(px + (size2 - 1) * i1 + size1 * i2, py + height, pz + (size2 - 1) * i2 - size1 * i1);

								if (height < 0 && !world.getBlockState(pos).isSolid() || height >= 0 && !world.isAirBlock(pos))
								{
									hasSpace = false;

									break space;
								}
							}
						}
					}

					if (hasSpace)
					{
						i = j % 4;
						resultPos = new BlockPos(px, py, pz);

						break outside;
					}
				}
			}
		}

		if (resultPos == null)
		{
			outside: for (int r = 1; r <= radius; ++r)
			{
				for (int rx = -r; rx <= r; ++rx)
				{
					for (int rz = -r; rz <= r; ++rz)
					{
						if (Math.abs(rx) < r && Math.abs(rz) < r) continue;

						int px = originPos.getX() + rx;
						int py = min;
						int pz = originPos.getZ() + rz;

						finder: while (true)
						{
							for (py = originPos.getY(); py <= max; ++py)
							{
								if (world.isAirBlock(pos.setPos(px, py, pz)) && world.getBlockState(pos.move(Direction.DOWN)).isSolid())
								{
									break finder;
								}
							}

							for (py = originPos.getY(); py >= min; --py)
							{
								if (world.isAirBlock(pos.setPos(px, py, pz)) && world.getBlockState(pos.move(Direction.DOWN)).isSolid())
								{
									break finder;
								}
							}

							py = 0;

							break;
						}

						if (py < min || py > max)
						{
							continue;
						}

						int i1 = j % 2;
						int i2 = 1 - i1;
						boolean hasSpace = true;

						space: for (int width = 0; width < 4; ++width)
						{
							for (int height = -1; height < 4; ++height)
							{
								pos.setPos(px + (width - 1) * i1, py + height, pz + (width - 1) * i2);

								if (height < 0 && !world.getBlockState(pos).isSolid() || height >= 0 && !world.isAirBlock(pos))
								{
									hasSpace = false;

									break space;
								}
							}
						}

						if (hasSpace)
						{
							i = j % 2;
							resultPos = new BlockPos(px, py, pz);

							break outside;
						}
					}
				}
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

			for (int size1 = -1; size1 <= 1; ++size1)
			{
				for (int size2 = 1; size2 < 3; ++size2)
				{
					int x = resultPos.getX() + (size2 - 1) * i1 + size1 * i2;
					int z = resultPos.getZ() + (size2 - 1) * i2 - size1 * i1;

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