package cavern.miner.world;

import cavern.miner.client.CaveMusics;
import cavern.miner.config.CavelandConfig;
import cavern.miner.data.WorldData;
import cavern.miner.entity.CaveEntityRegistry;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderCaveland extends WorldProviderCavern
{
	@Override
	public IChunkGenerator createChunkGenerator()
	{
		return new ChunkGeneratorCaveland(world);
	}

	@Override
	public DimensionType getDimensionType()
	{
		return CaveDimensions.CAVELAND;
	}

	@Override
	public int getActualHeight()
	{
		return CavelandConfig.halfHeight ? 128 : 256;
	}

	@Override
	public int getMonsterSpawn()
	{
		return CavelandConfig.monsterSpawn;
	}

	@Override
	public double getBrightness()
	{
		return CavelandConfig.caveBrightness;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public MusicType getMusicType()
	{
		return CaveMusics.CAVELAND;
	}

	@Override
	public int getAverageGroundLevel()
	{
		return CavelandConfig.groundLevel;
	}

	@Override
	public boolean canDropChunk(int x, int z)
	{
		return CavelandConfig.keepPortalChunk && !WorldData.get(world).hasPortal(x, z);
	}

	@Override
	public EntityLiving createSpawnCreature(WorldServer world, EnumCreatureType type, BlockPos pos, Biome.SpawnListEntry entry)
	{
		if (world.rand.nextInt(20) == 0)
		{
			Biome.SpawnListEntry spawnEntry = WeightedRandom.getRandomItem(world.rand, CaveEntityRegistry.ANIMAL_SPAWNS);

			try
			{
				return spawnEntry.newInstance(world);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return super.createSpawnCreature(world, type, pos, entry);
	}
}