package cavern.miner.world;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.Event.Result;

public class CaveMobSpawner
{
	private final Map<ChunkPos, BlockPos> eligibleChunksForSpawning = Maps.newHashMap();
	private final SpawnerProvider provider;

	public CaveMobSpawner()
	{
		this(null);
	}

	public CaveMobSpawner(@Nullable SpawnerProvider spawner)
	{
		this.provider = spawner;
	}

	public int findChunksForSpawning(ServerWorld world, boolean spawnHostileMobs, boolean spawnPeacefulMobs)
	{
		if (!spawnHostileMobs && !spawnPeacefulMobs)
		{
			return 0;
		}

		eligibleChunksForSpawning.clear();

		for (PlayerEntity player : world.getPlayers(o -> !o.isSpectator()))
		{
			int x = MathHelper.floor(player.getPosX() / 16.0D);
			int z = MathHelper.floor(player.getPosZ() / 16.0D);
			int range = 6;

			for (int rx = -range; rx <= range; ++rx)
			{
				for (int rz = -range; rz <= range; ++rz)
				{
					boolean flag = rx == -range || rx == range || rz == -range || rz == range;
					ChunkPos pos = new ChunkPos(rx + x, rz + z);

					if (!eligibleChunksForSpawning.containsKey(pos))
					{
						if (!flag && world.getWorldBorder().contains(pos))
						{
							eligibleChunksForSpawning.put(pos, player.getPosition());
						}
					}
				}
			}
		}

		int totalCount = 0;

		for (EntityClassification type : EntityClassification.values())
		{
			if (type.getAnimal() || type.getPeacefulCreature())
			{
				continue;
			}

			int maxNumber = getMaxNumberOfCreature(world, spawnHostileMobs, spawnPeacefulMobs, type);
			double range = getSpawnRange(world, spawnHostileMobs, spawnPeacefulMobs, type);

			if (maxNumber <= 0 || !canSpawnCreature(world, spawnHostileMobs, spawnPeacefulMobs, type))
			{
				continue;
			}

			if (world.countEntities().getInt(type) > maxNumber)
			{
				continue;
			}

			List<ChunkPos> shuffled = Lists.newArrayList(eligibleChunksForSpawning.keySet());
			Collections.shuffle(shuffled);

			BlockPos.Mutable pos = new BlockPos.Mutable();

			outside: for (ChunkPos chunkpos : shuffled)
			{
				BlockPos blockpos = getRandomPosition(world, chunkpos);
				int mobCount = 0;

				for (int i = 0; i < 3; ++i)
				{
					int x = blockpos.getX();
					int y = blockpos.getY();
					int z = blockpos.getZ();
					int n = 6;
					Biome.SpawnListEntry entry = null;
					ILivingEntityData data = null;

					for (int j = 0, chance = MathHelper.ceil(Math.random() * 4.0D); j < chance; ++j)
					{
						x += world.rand.nextInt(n) - world.rand.nextInt(n);
						y += world.rand.nextInt(1) - world.rand.nextInt(1);
						z += world.rand.nextInt(n) - world.rand.nextInt(n);
						pos.setPos(x, y, z);

						float posX = x + 0.5F;
						float posZ = z + 0.5F;

						if (world.isPlayerWithin(posX, y, posZ, range))
						{
							continue;
						}

						if (entry == null)
						{
							entry = getSpawnListEntryForTypeAt(world, type, pos);

							if (entry == null)
							{
								continue;
							}
						}

						if (!canCreatureTypeSpawnHere(world, type, entry, pos))
						{
							continue;
						}

						if (!WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementType(entry.entityType), world, pos, entry.entityType))
						{
							continue;
						}

						MobEntity entity = createSpawnCreature(world, type, pos, entry);

						if (entity == null)
						{
							continue;
						}

						entity.setLocationAndAngles(posX, y, posZ, world.rand.nextFloat() * 360.0F, 0.0F);

						Result canSpawn = ForgeEventFactory.canEntitySpawn(entity, world, posX, y, posZ, null, SpawnReason.NATURAL);

						if (canSpawn == Result.DENY)
						{
							continue;
						}

						if (entity.canSpawn(world, SpawnReason.NATURAL) && entity.isNotColliding(world))
						{
							if (!ForgeEventFactory.doSpecialSpawn(entity, world, posX, y, posZ, null, SpawnReason.NATURAL))
							{
								data = entity.onInitialSpawn(world, world.getDifficultyForLocation(entity.getPosition()), SpawnReason.NATURAL, data, null);
							}

							if (entity.isNotColliding(world))
							{
								++mobCount;

								world.addEntity(entity);
							}
							else
							{
								entity.remove();
							}

							if (mobCount >= ForgeEventFactory.getMaxSpawnPackSize(entity))
							{
								continue outside;
							}

							totalCount += mobCount;
						}
					}
				}
			}
		}

		return totalCount;
	}

	protected boolean canSpawnCreature(ServerWorld world, boolean spawnHostileMobs, boolean spawnPeacefulMobs, EntityClassification type)
	{
		if (provider != null)
		{
			Boolean ret = provider.canSpawnCreature(world, spawnHostileMobs, spawnPeacefulMobs, type);

			if (ret != null)
			{
				return ret;
			}
		}

		return (!type.getPeacefulCreature() || spawnPeacefulMobs) && (type.getPeacefulCreature() || spawnHostileMobs);
	}

	protected int getMaxNumberOfCreature(ServerWorld world, boolean spawnHostileMobs, boolean spawnPeacefulMobs, EntityClassification type)
	{
		if (provider != null)
		{
			Integer ret = provider.getMaxNumberOfCreature(world, spawnHostileMobs, spawnPeacefulMobs, type);

			if (ret != null)
			{
				return ret;
			}
		}

		return type.getMaxNumberOfCreature();
	}

	protected double getSpawnRange(ServerWorld world, boolean spawnHostileMobs, boolean spawnPeacefulMobs, EntityClassification type)
	{
		if (provider != null)
		{
			Double ret = provider.getSpawnRange(world, spawnHostileMobs, spawnPeacefulMobs, type);

			if (ret != null)
			{
				return ret;
			}
		}

		return 16.0D;
	}

	protected BlockPos getRandomPosition(World world, ChunkPos pos)
	{
		BlockPos blockpos = eligibleChunksForSpawning.get(pos);
		int y = 0;

		if (blockpos != null)
		{
			y = blockpos.getY();
		}

		int posX = pos.getXStart() + world.rand.nextInt(16);
		int posZ = pos.getZStart() + world.rand.nextInt(16);
		int posY;

		if (y > 0)
		{
			int max = world.getActualHeight() - 1;
			int range = 50;

			posY = MathHelper.nextInt(world.rand, Math.max(y - range, 1), Math.min(y + range, max));
		}
		else
		{
			posY = MathHelper.nextInt(world.rand, 1, world.getActualHeight() - 1);
		}

		return new BlockPos(posX, posY, posZ);
	}

	@Nullable
	protected Biome.SpawnListEntry getSpawnListEntryForTypeAt(ServerWorld world, EntityClassification type, BlockPos pos)
	{
		List<Biome.SpawnListEntry> list = world.getBiome(pos).getSpawns(type);

		list = ForgeEventFactory.getPotentialSpawns(world, type, pos, list);

		return list != null && !list.isEmpty() ? WeightedRandom.getRandomItem(world.rand, list) : null;
	}

	protected boolean canCreatureTypeSpawnHere(ServerWorld world, EntityClassification type, Biome.SpawnListEntry spawnListEntry, BlockPos pos)
	{
		List<Biome.SpawnListEntry> list = world.getBiome(pos).getSpawns(type);

		list = ForgeEventFactory.getPotentialSpawns(world, type, pos, list);

		return list != null && !list.isEmpty() ? list.contains(spawnListEntry) : false;
	}

	@Nullable
	protected MobEntity createSpawnCreature(ServerWorld world, EntityClassification type, BlockPos pos, Biome.SpawnListEntry entry)
	{
		if (provider != null)
		{
			MobEntity entity = provider.createSpawnCreature(world, type, pos, entry);

			if (entity != null)
			{
				return entity;
			}
		}

		Entity entity = entry.entityType.create(world);

		if (entity != null && entity instanceof MobEntity)
		{
			return (MobEntity)entity;
		}

		return null;
	}

	public interface SpawnerProvider
	{
		@Nullable
		default Boolean canSpawnCreature(ServerWorld world, boolean spawnHostileMobs, boolean spawnPeacefulMobs, EntityClassification type)
		{
			return null;
		}

		@Nullable
		Integer getMaxNumberOfCreature(ServerWorld world, boolean spawnHostileMobs, boolean spawnPeacefulMobs, EntityClassification type);

		@Nullable
		default Double getSpawnRange(ServerWorld world, boolean spawnHostileMobs, boolean spawnPeacefulMobs, EntityClassification type)
		{
			return null;
		}

		@Nullable
		default MobEntity createSpawnCreature(ServerWorld world, EntityClassification type, BlockPos pos, Biome.SpawnListEntry entry)
		{
			return null;
		}
	}
}