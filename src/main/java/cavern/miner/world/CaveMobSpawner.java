package cavern.miner.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

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
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.Event.Result;

public class CaveMobSpawner
{
	private final ServerWorld world;
	private final Random rand = new Random();

	private final Map<ChunkPos, BlockPos> eligibleChunksForSpawning = Maps.newHashMap();

	public CaveMobSpawner(ServerWorld world)
	{
		this.world = world;
	}

	public int getMaxCount(EntityClassification type)
	{
		return 150;
	}

	public double getSafeRange(EntityClassification type)
	{
		return 16.0D;
	}

	public void spawnMobs()
	{
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

		for (EntityClassification type : EntityClassification.values())
		{
			if (type.getAnimal() || type.getPeacefulCreature())
			{
				continue;
			}

			if (world.countEntities().getInt(type) > getMaxCount(type))
			{
				continue;
			}

			List<ChunkPos> shuffled = new ArrayList<>(eligibleChunksForSpawning.keySet());
			Collections.shuffle(shuffled, rand);

			BlockPos.Mutable pos = new BlockPos.Mutable();

			outside: for (ChunkPos chunkpos : shuffled)
			{
				findRandomPosition(pos, chunkpos);

				int mobCount = 0;

				for (int i = 0; i < 3; ++i)
				{
					int n = 6;
					Biome.SpawnListEntry entry = null;
					ILivingEntityData data = null;

					for (int j = 0, chance = MathHelper.ceil(Math.random() * 4.0D); j < chance; ++j)
					{
						int mx = rand.nextInt(n) - rand.nextInt(n);
						int my = rand.nextInt(1) - rand.nextInt(1);
						int mz = rand.nextInt(n) - rand.nextInt(n);

						pos.setPos(pos.getX() + mx, pos.getY() + my, pos.getZ() + mz);

						float posX = pos.getX() + 0.5F;
						float posZ = pos.getZ() + 0.5F;

						if (world.isPlayerWithin(posX, pos.getY(), posZ, getSafeRange(type)))
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

	protected void findRandomPosition(BlockPos.Mutable pos, ChunkPos chunkPos)
	{
		BlockPos blockpos = eligibleChunksForSpawning.get(chunkPos);
		int y = 0;

		if (blockpos != null)
		{
			y = blockpos.getY();
		}

		int posX = chunkPos.getXStart() + rand.nextInt(16);
		int posZ = chunkPos.getZStart() + rand.nextInt(16);
		int posY;

		if (y > 0)
		{
			int max = world.getActualHeight() - 1;
			int range = 50;

			posY = MathHelper.nextInt(rand, Math.max(y - range, 1), Math.min(y + range, max));
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