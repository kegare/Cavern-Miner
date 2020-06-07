package cavern.miner.world;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.config.GeneralConfig;
import cavern.miner.init.CaveCapabilities;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.common.util.LazyOptional;

public class CavernTeleporter implements ITeleporter
{
	private static final BlockState AIR = Blocks.AIR.getDefaultState();
	private static final BlockState MOSSY_STONE = Blocks.MOSSY_COBBLESTONE.getDefaultState();

	private final CavernPortalBlock portalBlock;

	public CavernTeleporter(CavernPortalBlock portal)
	{
		this.portalBlock = portal;
	}

	@Override
	public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity)
	{
		Entity newEntity = repositionEntity.apply(false);
		BlockPos pos = newEntity.getPosition();
		int range = GeneralConfig.INSTANCE.findRange.get();

		if (GeneralConfig.INSTANCE.posCache.get() && entity.getCapability(CaveCapabilities.TELEPORTER_CACHE).map(o -> placeInCachedPortal(destWorld, newEntity, yaw, range, o)).orElse(false))
		{
			return newEntity;
		}

		if (destWorld.getCapability(CaveCapabilities.CAVE_PORTAL_LIST).map(o -> placeInStoredPortal(destWorld, newEntity, yaw, range, pos, o)).orElse(false))
		{
			return newEntity;
		}

		if (!placeInPortal(destWorld, newEntity, yaw, range, pos))
		{
			placeInPortal(destWorld, newEntity, yaw, range, makePortal(destWorld, newEntity, range));
		}

		return newEntity;
	}

	public boolean placeInCachedPortal(ServerWorld world, Entity entity, float yaw, int checkRange, TeleporterCache cache)
	{
		ResourceLocation key = portalBlock.getRegistryName();
		DimensionType dim = world.getDimension().getType();
		BlockPos pos = cache.getLastPos(key, dim, null);

		if (pos == null)
		{
			return false;
		}

		return placeInPortal(world, entity, yaw, checkRange, pos);
	}

	public boolean placeInStoredPortal(ServerWorld world, Entity entity, float yaw, int checkRange, BlockPos checkPos, CavePortalList list)
	{
		List<BlockPos> positions = list.getPortalPositions(portalBlock).stream()
			.filter(o -> Math.sqrt(o.distanceSq(checkPos)) <= checkRange)
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

	public boolean placeInPortal(ServerWorld world, Entity entity, float yaw, int checkRange, BlockPos checkPos)
	{
		BlockPos pos = null;

		if (world.getBlockState(checkPos).getBlock() == portalBlock)
		{
			pos = checkPos;
		}
		else
		{
			int max = world.getActualHeight() - 1;
			BlockPos.Mutable findPos = new BlockPos.Mutable();

			outside: for (int range = 1; range <= checkRange; ++range)
			{
				for (int i = -range; i <= range; ++i)
				{
					for (int j = -range; j <= range; ++j)
					{
						if (Math.abs(i) < range && Math.abs(j) < range) continue;

						for (int y = checkPos.getY(); y < max; ++y)
						{
							if (world.getBlockState(findPos.setPos(checkPos.getX() + i, y, checkPos.getZ() + j)).getBlock() == portalBlock)
							{
								pos = findPos.toImmutable();

								break outside;
							}
						}

						for (int y = checkPos.getY(); y > 1; --y)
						{
							if (world.getBlockState(findPos.setPos(checkPos.getX() + i, y, checkPos.getZ() + j)).getBlock() == portalBlock)
							{
								pos = findPos.toImmutable();

								break outside;
							}
						}
					}
				}
			}

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

		LazyOptional<TeleporterCache> cache = entity.getCapability(CaveCapabilities.TELEPORTER_CACHE);
		Vec3d portalVec = ObjectUtils.defaultIfNull(cache.isPresent() ? cache.orElse(null).getLastPortalVec() : Vec3d.ZERO, Vec3d.ZERO);
		Direction teleportDirection = ObjectUtils.defaultIfNull(cache.isPresent() ? cache.orElse(null).getTeleportDirection() : Direction.NORTH, Direction.NORTH);
		BlockPattern.PatternHelper pattern = CavernPortalBlock.createPatternHelper(portalBlock, world, portalPos);
		BlockPattern.PortalInfo portalInfo = pattern.getPortalInfo(teleportDirection, portalPos, portalVec.y, entity.getMotion(), portalVec.z);

		if (portalInfo == null)
		{
			return false;
		}

		entity.setMotion(portalInfo.motion);
		entity.rotationYaw = yaw + portalInfo.rotation;

		if (world.getBlockState(new BlockPos(portalInfo.pos)).getBlock() == portalBlock)
		{
			entity.moveForced(portalInfo.pos.x, portalInfo.pos.y + 0.5D, portalInfo.pos.z);
		}
		else
		{
			entity.moveForced(portalPos.getX() + 0.5D, portalInfo.pos.y + 0.5D, portalPos.getZ() + 0.5D);
		}

		return true;
	}

	@Nullable
	public BlockPos makePortal(ServerWorld world, Entity entity, int findRange)
	{
		double portalDist = -1.0D;
		int max = world.getActualHeight() - 1;
		int x = MathHelper.floor(entity.getPosX());
		int y = MathHelper.floor(entity.getPosY());
		int z = MathHelper.floor(entity.getPosZ());
		int x1 = x;
		int y1 = y;
		int z1 = z;
		int i = 0;
		int j = world.rand.nextInt(4);
		BlockPos.Mutable pos = new BlockPos.Mutable();

		for (int range = 1; range <= findRange; ++range)
		{
			for (int ix = -range; ix <= range; ++ix)
			{
				for (int iz = -range; iz <= range; ++iz)
				{
					if (Math.abs(ix) < range && Math.abs(iz) < range) continue;

					int px = x + ix;
					int pz = z + iz;
					double xSize = px + 0.5D - entity.getPosX();
					double zSize = pz + 0.5D - entity.getPosZ();

					outside: for (int py = max; py > 1; --py)
					{
						if (world.isAirBlock(pos.setPos(px, py, pz)))
						{
							while (py > 0 && world.isAirBlock(pos.setPos(px, py - 1, pz)))
							{
								--py;
							}

							for (int k = j; k < j + 4; ++k)
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

											if (height < 0 && !world.getBlockState(pos).getMaterial().isSolid() || height >= 0 && !world.isAirBlock(pos))
											{
												continue outside;
											}
										}
									}
								}

								double ySize = py + 0.5D - entity.getPosY();
								double size = xSize * xSize + ySize * ySize + zSize * zSize;

								if (portalDist < 0.0D || size < portalDist)
								{
									portalDist = size;
									x1 = px;
									y1 = py;
									z1 = pz;
									i = k % 4;
								}
							}
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
			for (int range = 1; range <= findRange; ++range)
			{
				for (int ix = -range; ix <= range; ++ix)
				{
					for (int iz = -range; iz <= range; ++iz)
					{
						if (Math.abs(ix) < range && Math.abs(iz) < range) continue;

						int px = x + ix;
						int pz = z + iz;
						double xSize = px + 0.5D - entity.getPosX();
						double zSize = pz + 0.5D - entity.getPosZ();

						outside: for (int py = max; py > 1; --py)
						{
							if (world.isAirBlock(pos.setPos(px, py, pz)))
							{
								while (py > 0 && world.isAirBlock(pos.setPos(px, py - 1, pz)))
								{
									--py;
								}

								for (int k = j; k < j + 2; ++k)
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

											if (height < 0 && !world.getBlockState(pos).getMaterial().isSolid() || height >= 0 && !world.isAirBlock(pos))
											{
												continue outside;
											}
										}
									}

									double ySize = py + 0.5D - entity.getPosY();
									double size = xSize * xSize + ySize * ySize + zSize * zSize;

									if (portalDist < 0.0D || size < portalDist)
									{
										portalDist = size;
										x1 = px;
										y1 = py;
										z1 = pz;
										i = k % 2;
									}
								}
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

		int x2 = x1;
		int y2 = y1;
		int z2 = z1;
		int i1 = i % 2;
		int j1 = 1 - i1;

		if (i % 4 >= 2)
		{
			i1 = -i1;
			j1 = -j1;
		}

		if (portalDist < 0.0D)
		{
			y1 = MathHelper.clamp(y1, 1, max - 10);
			y2 = y1;

			for (int size1 = -1; size1 <= 1; ++size1)
			{
				for (int size2 = 1; size2 < 3; ++size2)
				{
					for (int height = -1; height < 3; ++height)
					{
						int blockX = x2 + (size2 - 1) * i1 + size1 * j1;
						int blockY = y2 + height;
						int blockZ = z2 + (size2 - 1) * j1 - size1 * i1;
						boolean flag = height < 0;

						world.setBlockState(pos.setPos(blockX, blockY, blockZ), flag ? MOSSY_STONE : AIR);
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
					pos.setPos(x2 + width * i1, y2 + height, z2 + width * j1);

					world.setBlockState(pos, MOSSY_STONE);
				}
			}
		}

		BlockState portalState = portalBlock.getDefaultState().with(CavernPortalBlock.AXIS, i1 != 0 ? Direction.Axis.X : Direction.Axis.Z);
		BlockPos portalPos = null;

		for (int width = 0; width < 2; ++width)
		{
			for (int height = 0; height < 3; ++height)
			{
				world.setBlockState(pos.setPos(x2 + width * i1, y2 + height, z2 + width * j1), portalState, 18);

				if (width == 1 && height == 0)
				{
					portalPos = pos.toImmutable();
				}
			}
		}

		return portalPos;
	}
}