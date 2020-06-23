package cavern.miner.world;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.config.GeneralConfig;
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
import net.minecraft.util.math.MathHelper;
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
				BlockPos pos = makePortal(destWorld, newEntity, radius);

				if (pos != null)
				{
					placed = placeInPortal(destWorld, newEntity, yaw, radius, pos);
				}
			}
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
		BlockPos pos = cache.getLastPos(key, dim, null);

		if (pos == null)
		{
			return false;
		}

		return placeInPortal(world, entity, yaw, radius, pos);
	}

	public boolean placeInStoredPortal(ServerWorld world, Entity entity, float yaw, int radius, BlockPos checkPos, CavePortalList list)
	{
		List<BlockPos> positions = list.getPortalPositions(portalBlock).stream()
			.filter(o -> new BlockPos(o.getX(), 0, o.getZ()).withinDistance(new BlockPos(checkPos.getX(), 0, checkPos.getZ()), radius))
			.sorted((o1, o2) -> Double.compare(o1.distanceSq(checkPos), o2.distanceSq(checkPos))).collect(Collectors.toList());

		for (BlockPos portalPos : positions)
		{
			if (placeInPortal(world, entity, yaw, 8, portalPos))
			{
				return true;
			}

			list.removePortal(portalBlock, portalPos);
		}

		return false;
	}

	public boolean placeInPortal(ServerWorld world, Entity entity, float yaw, int radius, BlockPos checkPos)
	{
		BlockPos pos = null;

		if (world.getBlockState(checkPos).getBlock() == portalBlock)
		{
			pos = checkPos;
		}
		else
		{
			pos = BlockPosHelper.findPos(world, checkPos, radius, o -> world.getBlockState(o).getBlock() == portalBlock);

			if (pos == null)
			{
				pos = world.getSpawnPoint();
			}

			if (world.getBlockState(pos).getBlock() != portalBlock)
			{
				return false;
			}
		}

		final BlockPos portalPos = pos.toImmutable();

		world.getCapability(CaveCapabilities.CAVE_PORTAL_LIST).ifPresent(o -> o.addPortal(portalBlock, portalPos));

		if (portalVec == null)
		{
			portalVec = Vec3d.ZERO;
		}

		if (teleportDirection == null)
		{
			teleportDirection = Direction.NORTH;
		}

		BlockPattern.PatternHelper pattern = portalBlock.createPatternHelper(world, portalPos);
		BlockPattern.PortalInfo portalInfo = pattern.getPortalInfo(teleportDirection, portalPos, portalVec.y, entity.getMotion(), portalVec.x);

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
	public BlockPos makePortal(ServerWorld world, Entity entity, int radius)
	{
		int originX = MathHelper.floor(entity.getPosX());
		int originY = MathHelper.floor(entity.getPosY());
		int originZ = MathHelper.floor(entity.getPosZ());
		int min = 10;
		int max = world.getActualHeight() - 10;
		int x = originX;
		int y = originY;
		int z = originZ;
		int i = 0;
		int j = world.rand.nextInt(4);
		BlockPos.Mutable pos = new BlockPos.Mutable();
		double portalDist = -1.0D;

		for (int r = 1; r <= radius; ++r)
		{
			for (int rx = -r; rx <= r; ++rx)
			{
				for (int rz = -r; rz <= r; ++rz)
				{
					if (Math.abs(rx) < r && Math.abs(rz) < r) continue;

					int px = originX + rx;
					int py = min;
					int pz = originZ + rz;

					finder: while (true)
					{
						for (py = originY; py <= max; ++py)
						{
							if (world.isAirBlock(pos.setPos(px, py, pz)) && world.getBlockState(pos.move(Direction.DOWN)).isNormalCube(world, pos))
							{
								break finder;
							}
						}

						for (py = originY; py >= min; --py)
						{
							if (world.isAirBlock(pos.setPos(px, py, pz)) && world.getBlockState(pos.move(Direction.DOWN)).isNormalCube(world, pos))
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

					double xSize = px + 0.5D - entity.getPosX();
					double zSize = pz + 0.5D - entity.getPosZ();

					outside: for (int k = j; k < j + 4; ++k)
					{
						int i1 = k % 2;
						int j1 = 1 - i1;

						if (k % 4 >= 2)
						{
							i1 = -i1;
							j1 = -j1;
						}

						for (int size1 = 0; size1 < 3; ++size1)
						{
							for (int size2 = 0; size2 < 4; ++size2)
							{
								for (int height = -1; height < 4; ++height)
								{
									int checkX = px + (size2 - 1) * i1 + size1 * j1;
									int checkY = py + height;
									int checkZ = pz + (size2 - 1) * j1 - size1 * i1;

									pos.setPos(checkX, checkY, checkZ);

									if (height < 0 && !world.getBlockState(pos).isSolid() || height >= 0 && !world.isAirBlock(pos))
									{
										break outside;
									}
								}
							}
						}

						double ySize = py + 0.5D - entity.getPosY();
						double size = xSize * xSize + ySize * ySize + zSize * zSize;

						if (portalDist < 0.0D || size < portalDist)
						{
							portalDist = size;
							x = px;
							y = py;
							z = pz;
							i = k % 4;
						}
					}
				}
			}

			if (portalDist >= 0.0D)
			{
				break;
			}
		}

		if (portalDist < 0.0D)
		{
			for (int r = 1; r <= radius; ++r)
			{
				for (int rx = -r; rx <= r; ++rx)
				{
					for (int rz = -r; rz <= r; ++rz)
					{
						if (Math.abs(rx) < r && Math.abs(rz) < r) continue;

						int px = originX + rx;
						int py = min;
						int pz = originZ + rz;

						finder: while (true)
						{
							for (py = originY; py <= max; ++py)
							{
								if (world.isAirBlock(pos.setPos(px, py, pz)) && world.getBlockState(pos.move(Direction.DOWN)).isNormalCube(world, pos))
								{
									break finder;
								}
							}

							for (py = originY; py >= min; --py)
							{
								if (world.isAirBlock(pos.setPos(px, py, pz)) && world.getBlockState(pos.move(Direction.DOWN)).isNormalCube(world, pos))
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

						double xSize = px + 0.5D - entity.getPosX();
						double zSize = pz + 0.5D - entity.getPosZ();

						outside: for (int k = j; k < j + 2; ++k)
						{
							int i1 = k % 2;
							int j1 = 1 - i1;

							for (int width = 0; width < 4; ++width)
							{
								for (int height = -1; height < 4; ++height)
								{
									int px1 = px + (width - 1) * i1;
									int py1 = py + height;
									int pz1 = pz + (width - 1) * j1;

									pos.setPos(px1, py1, pz1);

									if (height < 0 && !world.getBlockState(pos).isSolid() || height >= 0 && !world.isAirBlock(pos))
									{
										break outside;
									}
								}
							}

							double ySize = py + 0.5D - entity.getPosY();
							double size = xSize * xSize + ySize * ySize + zSize * zSize;

							if (portalDist < 0.0D || size < portalDist)
							{
								portalDist = size;
								x = px;
								y = py;
								z = pz;
								i = k % 2;
							}
						}
					}
				}

				if (portalDist >= 0.0D)
				{
					break;
				}
			}
		}

		int x1 = x;
		int y1 = y;
		int z1 = z;
		int i1 = i % 2;
		int j1 = 1 - i1;

		if (i % 4 >= 2)
		{
			i1 = -i1;
			j1 = -j1;
		}

		if (portalDist < 0.0D)
		{
			y = MathHelper.clamp(y, min, max);
			y1 = y;

			for (int size1 = -1; size1 <= 1; ++size1)
			{
				for (int size2 = 1; size2 < 3; ++size2)
				{
					for (int height = -1; height < 3; ++height)
					{
						int blockX = x1 + (size2 - 1) * i1 + size1 * j1;
						int blockY = y1 + height;
						int blockZ = z1 + (size2 - 1) * j1 - size1 * i1;
						boolean flag = height < 0;

						world.setBlockState(pos.setPos(blockX, blockY, blockZ), flag ? portalFrameBlock : Blocks.AIR.getDefaultState());
					}
				}
			}
		}

		for (int width = -1; width < 3; ++width)
		{
			for (int height = -1; height < 4; ++height)
			{
				if (width == -1 || width == 2 || height == -1 || height == 3)
				{
					pos.setPos(x1 + width * i1, y1 + height, z1 + width * j1);

					world.setBlockState(pos, portalFrameBlock);
				}
			}
		}

		BlockState portalState = portalBlock.getDefaultState().with(CavernPortalBlock.AXIS, i1 != 0 ? Direction.Axis.X : Direction.Axis.Z);
		BlockPos portalPos = null;

		for (int width = 0; width < 2; ++width)
		{
			for (int height = 0; height < 3; ++height)
			{
				world.setBlockState(pos.setPos(x1 + width * i1, y1 + height, z1 + width * j1), portalState, 18);

				if (width == 1 && height == 0)
				{
					portalPos = pos.toImmutable();
				}
			}
		}

		return portalPos;
	}
}