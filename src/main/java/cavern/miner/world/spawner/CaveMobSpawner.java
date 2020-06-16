package cavern.miner.world.spawner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import cavern.miner.init.CaveCapabilities;
import cavern.miner.storage.CavePortalList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.Event.Result;

public class CaveMobSpawner
{
	private final ServerWorld world;
	private final Random rand = new Random();

	private final Map<ChunkPos, BlockPos> eligibleChunksForSpawning = new HashMap<>();

	public CaveMobSpawner(ServerWorld world)
	{
		this.world = world;
	}

	public int getChunkRadius(PlayerEntity player)
	{
		return 6;
	}

	public int getHeightRadius(EntityClassification type)
	{
		return 50;
	}

	public int getMaxCount(EntityClassification type)
	{
		return type.getAnimal() ? 0 : type.getMaxNumberOfCreature();
	}

	public int getSafeDistance(EntityClassification type)
	{
		return 16;
	}

	public boolean canSpawnChunk(ChunkPos pos)
	{
		if (!world.getWorldBorder().contains(pos) || !world.getChunkProvider().isChunkLoaded(pos))
		{
			return false;
		}

		CavePortalList portalList = world.getCapability(CaveCapabilities.CAVE_PORTAL_LIST).orElse(null);

		if (portalList != null)
		{
			BlockPos centerPos = pos.getBlock(8, 0, 8);

			for (BlockPos portalPos : portalList.getPortalPositions())
			{
				if (centerPos.withinDistance(new BlockPos(portalPos.getX(), 0, portalPos.getZ()), 32.0D))
				{
					return false;
				}
			}
		}

		return true;
	}

	public boolean findEligibleChunks()
	{
		eligibleChunksForSpawning.clear();

		ServerPlayerEntity player = world.getRandomPlayer();

		if (player == null || player.isSpectator())
		{
			return false;
		}

		BlockPos pos = player.getPosition();

		ChunkPos.getAllInBox(new ChunkPos(pos), getChunkRadius(player)).filter(this::canSpawnChunk).forEach(o -> eligibleChunksForSpawning.put(o, pos));

		return !eligibleChunksForSpawning.isEmpty();
	}

	public void spawnMobs()
	{
		if (world.getGameTime() % 20L == 0L)
		{
			eligibleChunksForSpawning.clear();
		}

		for (EntityClassification type : EntityClassification.values())
		{
			int maxCount = getMaxCount(type);

			if (maxCount <= 0 || world.countEntities().getInt(type) >= maxCount)
			{
				continue;
			}

			if (eligibleChunksForSpawning.isEmpty() && !findEligibleChunks())
			{
				break;
			}

			List<ChunkPos> shuffled = new ArrayList<>(eligibleChunksForSpawning.keySet());
			Collections.shuffle(shuffled, rand);

			BlockPos.Mutable pos = new BlockPos.Mutable();

			outside: for (ChunkPos chunkPos : shuffled)
			{
				findRandomPosition(pos, type, chunkPos);

				if (world.getBlockState(pos).isNormalCube(world, pos))
				{
					continue;
				}

				int mobCount = 0;

				for (int i = 0; i < 3; ++i)
				{
					int leftRight = 6;
					Biome.SpawnListEntry entry = null;
					ILivingEntityData data = null;

					for (int j = 0, chance = MathHelper.ceil(Math.random() * 4.0D); j < chance; ++j)
					{
						int mx = rand.nextInt(leftRight) - rand.nextInt(leftRight);
						int my = rand.nextInt(1) - rand.nextInt(1);
						int mz = rand.nextInt(leftRight) - rand.nextInt(leftRight);

						pos.setPos(pos.getX() + mx, pos.getY() + my, pos.getZ() + mz);

						float posX = pos.getX() + 0.5F;
						float posZ = pos.getZ() + 0.5F;
						double safeDistance = getSafeDistance(type);

						if (world.isPlayerWithin(posX, pos.getY(), posZ, safeDistance))
						{
							continue;
						}

						if (entry == null)
						{
							entry = getSpawnListEntryForTypeAt(type, pos);

							if (entry == null)
							{
								continue;
							}
						}

						if (!entry.entityType.isSummonable() || !canCreatureTypeSpawnHere(type, entry, pos))
						{
							continue;
						}

						if (!WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementType(entry.entityType), world, pos, entry.entityType))
						{
							continue;
						}

						if (!EntitySpawnPlacementRegistry.func_223515_a(entry.entityType, world, SpawnReason.NATURAL, pos, rand))
						{
							continue;
						}

						if (!world.hasNoCollisions(entry.entityType.getBoundingBoxWithSizeApplied(posX, pos.getY(), posZ)))
						{
							continue;
						}

						Entity entity = entry.entityType.create(world);

						if (entity == null || !(entity instanceof MobEntity))
						{
							continue;
						}

						MobEntity mobEntity = (MobEntity)entity;

						mobEntity.setLocationAndAngles(posX, pos.getY(), posZ, rand.nextFloat() * 360.0F, 0.0F);

						Result canSpawn = ForgeEventFactory.canEntitySpawn(mobEntity, world, posX, pos.getY(), posZ, null, SpawnReason.NATURAL);

						if (canSpawn == Result.DENY)
						{
							continue;
						}

						if (mobEntity.canSpawn(world, SpawnReason.NATURAL) && mobEntity.isNotColliding(world))
						{
							if (!ForgeEventFactory.doSpecialSpawn(mobEntity, world, posX, pos.getY(), posZ, null, SpawnReason.NATURAL))
							{
								data = mobEntity.onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.NATURAL, data, null);
							}

							if (mobEntity.isNotColliding(world) && world.addEntity(mobEntity))
							{
								++mobCount;
							}
							else
							{
								mobEntity.remove();
							}

							if (mobCount >= ForgeEventFactory.getMaxSpawnPackSize(mobEntity))
							{
								continue outside;
							}
						}
					}
				}
			}
		}
	}

	protected void findRandomPosition(BlockPos.Mutable pos, EntityClassification type, ChunkPos chunkPos)
	{
		BlockPos playerPos = eligibleChunksForSpawning.get(chunkPos);
		int y = 0;

		if (playerPos != null)
		{
			y = playerPos.getY();
		}

		int posX = chunkPos.getXStart() + rand.nextInt(16);
		int posZ = chunkPos.getZStart() + rand.nextInt(16);
		int posY;

		if (y > 0)
		{
			int max = world.getActualHeight() - 1;
			int radius = getHeightRadius(type);

			posY = MathHelper.nextInt(rand, Math.max(y - radius, 1), Math.min(y + radius, max));
		}
		else
		{
			posY = MathHelper.nextInt(rand, 1, world.getActualHeight() - 1);
		}

		pos.setPos(posX, posY, posZ);
	}

	@Nullable
	protected Biome.SpawnListEntry getSpawnListEntryForTypeAt(EntityClassification type, BlockPos pos)
	{
		List<Biome.SpawnListEntry> list = world.getBiome(pos).getSpawns(type);

		list = ForgeEventFactory.getPotentialSpawns(world, type, pos, list);

		return list != null && !list.isEmpty() ? WeightedRandom.getRandomItem(rand, list) : null;
	}

	protected boolean canCreatureTypeSpawnHere(EntityClassification type, Biome.SpawnListEntry spawnListEntry, BlockPos pos)
	{
		List<Biome.SpawnListEntry> list = world.getBiome(pos).getSpawns(type);

		list = ForgeEventFactory.getPotentialSpawns(world, type, pos, list);

		return list != null && !list.isEmpty() ? list.contains(spawnListEntry) : false;
	}
}